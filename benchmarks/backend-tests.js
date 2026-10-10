import { spawnSync } from 'node:child_process';
import { mkdirSync, writeFileSync } from 'node:fs';
import { guard, compose } from './compose-tools.js';

guard();
// Credentials stay in memory; do not print resolved Compose configuration.
const config = JSON.parse(compose(['config', '--format', 'json']));
compose(['exec', '-T', 'db', 'sh', '-c', 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -u root'],
  "CREATE DATABASE IF NOT EXISTS cinema_m12_test CHARACTER SET utf8mb4 COLLATE utf8mb4_bin; GRANT ALL ON cinema_m12_test.* TO 'cinema'@'%'; GRANT ALL ON `auth\\_upgrade\\_%`.* TO 'cinema'@'%';");
const build = spawnSync('docker', ['build', '--target', 'build', '-t', 'cinema-m12-verify', '.'], {encoding: 'utf8', maxBuffer: 32 * 1024 * 1024});
if (build.status !== 0) throw new Error('Verification image build failed');
const result = spawnSync('docker', ['run', '--rm', '--network', 'cinema-bench_default',
  '-e', 'CINEMA_TEST_MYSQL_URL=jdbc:mysql://db:3306/cinema_m12_test?connectionTimeZone=UTC',
  '-e', 'CINEMA_TEST_MYSQL_USER=cinema', '-e', `CINEMA_TEST_MYSQL_PASSWORD=${config.services.db.environment.MYSQL_PASSWORD}`,
  'cinema-m12-verify', './mvnw', '-B', 'clean', 'test'], {encoding: 'utf8', maxBuffer: 64 * 1024 * 1024});
mkdirSync('benchmarks/results', {recursive: true});
writeFileSync('benchmarks/results/backend-tests.log', `${result.stdout ?? ''}\n${result.stderr ?? ''}`);
console.log((result.stdout ?? '').split('\n').filter(l => /Tests run: \d+, Failures: \d+, Errors: \d+, Skipped: \d+$|BUILD SUCCESS|BUILD FAILURE/.test(l.trim())).slice(-3).join('\n'));
process.exit(result.status ?? 1);
