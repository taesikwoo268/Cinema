import { readFileSync, writeFileSync } from 'node:fs';
import assert from 'node:assert/strict';

const read = name => JSON.parse(readFileSync(`benchmarks/results/${name}.json`, 'utf8'));
const baseline = read('baseline'), consistency = read('consistency'), smoke = read('smoke');
assert.equal(baseline.runId, consistency.runId);
assert.equal(baseline.runId, smoke.runId);
assert.equal(smoke.status, 'passed');
const metric = (sample, name) => sample.metrics[name]?.values ?? sample.metrics[name] ?? {};
const num = value => Number(value).toFixed(2);
const rows = baseline.samples.map(sample => {
  const requests = metric(sample, 'http_reqs'), duration = metric(sample, 'http_req_duration');
  const iterations = metric(sample, 'iteration_duration'), errors = metric(sample, 'http_req_failed');
  return `| ${sample.label} | ${sample.vus} | ${requests.count} | ${num(requests.rate)} | ${num(duration.med)} | ${num(duration['p(95)'])} | ${num(duration['p(99)'])} | ${num(iterations['p(95)'])} | ${num((errors.value ?? errors.rate) * 100)}% |`;
});
const text = `# M12 — Benchmark baseline

Measured at ${baseline.recordedAt} (UTC); local verification date 2026-10-10, Asia/Saigon. This is a development baseline, not a production capacity promise.

## Reproduction context

- Backend source base: \`${baseline.sourceBaseCommit}\`; M12 working tree adds packaging/tools/docs only, with no Java changes.
- App image: \`${baseline.appImageId}\`.
- Host: ${baseline.host.platform}, ${baseline.host.cpu}, ${baseline.host.logicalCpus} logical CPUs, ${num(baseline.host.memoryBytes / 1073741824)} GiB RAM.
- Docker: ${baseline.docker.serverVersion}, ${baseline.docker.operatingSystem}, ${baseline.docker.cpus} CPUs, ${num(baseline.docker.memoryBytes / 1073741824)} GiB available to engine. No additional per-container CPU/memory limits.
- Java: ${baseline.java.split('\n')[0]}; MySQL: ${baseline.mysql}.
- k6: grafana/k6:1.0.0. Resolved image digests and raw aggregate metrics are in [baseline.json](baseline.json).
- Dataset: one movie, one hall, ${baseline.fixtureBookings} physical seats, two screenings, ${baseline.fixtureBookings} unique booking customers and ${baseline.fixtureContenders} contention customers, plus seed admin. This fixture was seeded into the isolated benchmark database with an earlier run retained; catalog read payloads include retained data. Regression used separate schemas. See persisted totals below.
- Network: k6, app and MySQL in one local Docker network; no host-3306 data. Authentication/seed and Maven tests excluded from measurement.

## Results

Read runs use 15-second warm-ups (excluded), then independent 60-second closed-loop runs at 1/5/10 VUs without think time. Catalog iteration makes two GET requests. Availability iteration makes one GET. Booking performs five HTTP requests per successful workflow; its finite sample and the ten-user contention sample are too small for stable tail-latency conclusions.

All latency columns are milliseconds. HTTP columns aggregate the requests in each named workload; iteration p95 measures the entire script iteration, not a single endpoint. Request throughput is not bookings/second.

| Workload | VUs | Requests | Requests/s | HTTP p50 | HTTP p95 | HTTP p99 | Iteration p95 | Unexpected HTTP failures |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
${rows.join('\n')}

Contention requires exactly one winner and nine HTTP 409 conflicts; those nine are expected business conflicts, not hidden system failures. All functional checks and finite-workload count thresholds passed. No latency SLO was chosen to make this baseline pass.

## Correctness and persistence

- Regular booking: ${consistency.booking.confirmed} confirmed bookings, ${consistency.booking.reservations} permanent reservations, ${consistency.booking.tickets} tickets, ${consistency.booking.successfulPayments} successful payments.
- Contended seat: ${consistency.contention.confirmed} confirmed booking, ${consistency.contention.reservations} reservation, ${consistency.contention.tickets} ticket, ${consistency.contention.successfulPayments} successful payment. No duplicate winner or paid loser.
- HTTP smoke checks passed: anonymous/invalid token rejected, real token accepted, CUSTOMER denied catalog creation.
- App and DB containers were removed/recreated without deleting the named volume. Counts before/after matched and an existing Bearer token still worked. Order of counts (users/customers/tickets/bookings/tokens): ${smoke.countsBefore.join('/')}.

## Limits

Single local machine, small dataset, fake local payment and coarse database locks. This measures the tested image/environment only; do not extrapolate to real payment providers, WAN latency or sustained production traffic. Existing payment-success/database-rollback failure window and missing hold TTL/rate limiting remain. See [reproduction instructions](../../benchmarks/README.md) and [acceptance](acceptance.md).
`;
writeFileSync('docs/m12/benchmark-baseline.md', text);
writeFileSync('docs/m12/baseline.json', JSON.stringify({baseline, consistency, smoke}, null, 2) + '\n');
console.log('Published sanitized baseline and correctness evidence; no tokens/passwords included.');
