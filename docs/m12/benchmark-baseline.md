# M12 — Benchmark baseline

Measured at 2026-10-10T03:02:54.023Z (UTC); local verification date 2026-10-10, Asia/Saigon. This is a development baseline, not a production capacity promise.

## Reproduction context

- Backend source base: `4e608596f258824f73eeec2a55a065e7bb347136`; M12 working tree adds packaging/tools/docs only, with no Java changes.
- App image: `sha256:93024bcc9e00cbf634807eb1cabe9ec7509ee1311af5b9c6dcc12d8463467c0e`.
- Host: win32, 13th Gen Intel(R) Core(TM) i7-13650HX, 20 logical CPUs, 15.71 GiB RAM.
- Docker: 29.7.2, Docker Desktop, 20 CPUs, 7.61 GiB available to engine. No additional per-container CPU/memory limits.
- Java: openjdk 25.0.4.1 2026-08-18 LTS; MySQL: mysql  Ver 8.4.11 for Linux on x86_64 (MySQL Community Server - GPL).
- k6: grafana/k6:1.0.0. Resolved image digests and raw aggregate metrics are in [baseline.json](baseline.json).
- Dataset: one movie, one hall, 30 physical seats, two screenings, 30 unique booking customers and 10 contention customers, plus seed admin. This fixture was seeded into the isolated benchmark database with an earlier run retained; catalog read payloads include retained data. Regression used separate schemas. See persisted totals below.
- Network: k6, app and MySQL in one local Docker network; no host-3306 data. Authentication/seed and Maven tests excluded from measurement.

## Results

Read runs use 15-second warm-ups (excluded), then independent 60-second closed-loop runs at 1/5/10 VUs without think time. Catalog iteration makes two GET requests. Availability iteration makes one GET. Booking performs five HTTP requests per successful workflow; its finite sample and the ten-user contention sample are too small for stable tail-latency conclusions.

All latency columns are milliseconds. HTTP columns aggregate the requests in each named workload; iteration p95 measures the entire script iteration, not a single endpoint. Request throughput is not bookings/second.

| Workload | VUs | Requests | Requests/s | HTTP p50 | HTTP p95 | HTTP p99 | Iteration p95 | Unexpected HTTP failures |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| catalog-1 | 1 | 70714 | 1178.62 | 0.70 | 1.08 | 1.52 | 2.34 | 0.00% |
| catalog-5 | 5 | 294312 | 4905.42 | 0.88 | 1.24 | 1.69 | 2.68 | 0.00% |
| catalog-10 | 10 | 395116 | 6585.34 | 1.32 | 1.89 | 2.67 | 4.08 | 0.00% |
| availability-1 | 1 | 1654 | 27.57 | 34.66 | 44.58 | 58.03 | 44.86 | 0.00% |
| availability-5 | 5 | 2497 | 41.58 | 113.89 | 150.57 | 183.76 | 150.77 | 0.00% |
| availability-10 | 10 | 2430 | 40.32 | 238.20 | 309.53 | 348.59 | 309.74 | 0.00% |
| booking | 5 | 150 | 208.99 | 19.83 | 58.53 | 71.46 | 163.65 | 0.00% |
| seat-contention | 10 | 14 | 199.88 | 24.92 | 36.82 | 38.51 | 56.93 | 0.00% |

Contention requires exactly one winner and nine HTTP 409 conflicts; those nine are expected business conflicts, not hidden system failures. All functional checks and finite-workload count thresholds passed. No latency SLO was chosen to make this baseline pass.

## Correctness and persistence

- Regular booking: 30 confirmed bookings, 30 permanent reservations, 30 tickets, 30 successful payments.
- Contended seat: 1 confirmed booking, 1 reservation, 1 ticket, 1 successful payment. No duplicate winner or paid loser.
- HTTP smoke checks passed: anonymous/invalid token rejected, real token accepted, CUSTOMER denied catalog creation.
- App and DB containers were removed/recreated without deleting the named volume. Counts before/after matched and an existing Bearer token still worked. Order of counts (users/customers/tickets/bookings/tokens): 82/82/62/62/82.

## Limits

Single local machine, small dataset, fake local payment and coarse database locks. This measures the tested image/environment only; do not extrapolate to real payment providers, WAN latency or sustained production traffic. Existing payment-success/database-rollback failure window and missing hold TTL/rate limiting remain. See [reproduction instructions](../../benchmarks/README.md) and [acceptance](acceptance.md).
