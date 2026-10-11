import { spawnSync } from 'node:child_process';
import { mkdirSync, readFileSync, writeFileSync, existsSync } from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { guard, compose } from './compose-tools.js';

guard();
const image = 'grafana/k6:1.0.0';
const out = 'benchmarks/results';
mkdirSync(out, {recursive: true});
if (existsSync(`${out}/baseline.json`)) throw new Error('Archive previous results first; do not overwrite evidence.');
const fixture = JSON.parse(readFileSync('benchmarks/fixtures/current.json', 'utf8'));
if ([...fixture.bookings, ...fixture.contention].some(x => Date.parse(x.expiresAt) < Date.now() + 15 * 60000)) throw new Error('Tokens have less than 15 minutes left; seed fresh fixtures.');
function command(args) {
  const r = spawnSync('docker', args, {encoding: 'utf8', maxBuffer: 16 * 1024 * 1024});
  if (r.status !== 0) throw new Error(`Docker ${args[0]} failed: ${r.stderr}`);
  return r.stdout.trim();
}
command(['pull', image]);
const samples = [];
function run(script, label, vus, duration) {
  const args = ['run', '--rm', '--network', 'cinema-bench_default', '-v', `${path.resolve('benchmarks')}:/work`,
    '-e', 'BASE_URL=http://app:8080', '-e', 'FIXTURE_FILE=/work/fixtures/current.json',
    '-e', `VUS=${vus}`, '-e', `DURATION=${duration}`, image, 'run', '--summary-export', `/work/results/${label}.json`, `/work/${script}.js`];
  console.log(`Running ${label}`);
  const r = spawnSync('docker', args, {encoding: 'utf8', maxBuffer: 16 * 1024 * 1024});
  writeFileSync(`${out}/${label}.log`, `${r.stdout ?? ''}\n${r.stderr ?? ''}`);
  if (r.status !== 0) throw new Error(`${label} failed; see ignored results log. No passing baseline will be written.`);
  const report = JSON.parse(readFileSync(`${out}/${label}.json`, 'utf8'));
  if (!label.includes('warmup')) samples.push({label, vus, duration, metrics: report.metrics});
}
for (const script of ['catalog', 'availability']) {
  run(script, `${script}-warmup`, 1, '15s');
  for (const vus of [1, 5, 10]) run(script, `${script}-${vus}`, vus, '60s');
}
run('booking', 'booking', 5, 'finite');
run('seat-contention', 'seat-contention', 10, 'finite');
const git = spawnSync('git', ['rev-parse', 'HEAD'], {encoding: 'utf8'}).stdout.trim();
const report = {recordedAt: new Date().toISOString(), sourceBaseCommit: git, runId: fixture.runId,
  fixtureBookings: fixture.bookings.length, fixtureContenders: fixture.contention.length,
  host: {platform: os.platform(), cpu: os.cpus()[0]?.model, logicalCpus: os.cpus().length, memoryBytes: os.totalmem()},
  docker: JSON.parse(command(['info', '--format', '{{json .}}']).replace(/^\uFEFF/, '')),
  images: JSON.parse(command(['image', 'inspect', 'cinema-bench-app', 'mysql:8.4', image, '--format', '{{json .RepoDigests}}'] ).split('\n').map(x => x.trim()).filter(Boolean).reduce((s,x,i)=>s+(i?',':'')+x,'[')+']'),
  appImageId: command(['image', 'inspect', 'cinema-bench-app', '--format', '{{.Id}}']),
  java: compose(['exec', '-T', 'app', 'java', '--version']),
  mysql: compose(['exec', '-T', 'db', 'mysql', '--version']),
  samples};
// Keep only relevant daemon facts, not the full host configuration.
report.docker = {serverVersion: report.docker.ServerVersion, operatingSystem: report.docker.OperatingSystem, cpus: report.docker.NCPU, memoryBytes: report.docker.MemTotal};
writeFileSync(`${out}/baseline.json`, JSON.stringify(report, null, 2));
console.log('Benchmark completed; run node benchmarks/verify.js for database consistency.');
