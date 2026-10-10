# M12 benchmark and verification tools

Run commands from the repository root. Requires Docker Desktop (Linux containers), Node 24+, and npm. k6 runs as the pinned Docker image `grafana/k6:1.0.0`; it is not an application dependency. OpenAPI validators are pinned dev dependencies in this directory.

## Isolated environment (Windows CMD)

Copy `.env.example` to `.env.m12`, set **APP_PORT=8082**, and enter nonempty, distinct local passwords. Never use your working database or expose the MySQL service port. Start only this project:

```bat
docker compose --env-file .env.m12 -p cinema-bench up --build -d --wait
npm --prefix benchmarks ci
npm --prefix benchmarks run validate:api
set "CONFIRM_BENCHMARK=cinema-bench"
node benchmarks/backend-tests.js
node benchmarks/seed.js
node benchmarks/run.js
node benchmarks/verify.js
node benchmarks/smoke.js
node benchmarks/report.js
```

PowerShell uses `$env:CONFIRM_BENCHMARK='cinema-bench'`. `.env.m12`, fixtures and raw results are ignored by Git. The seeder/runner check the fixed Compose project and loopback port 8082; no user-supplied production DB URL is accepted. Run `backend-tests.js` before measuring load, not concurrently: it uses a separate `cinema_m12_test` schema and `auth_upgrade_*` schemas in this isolated instance. Schema-creation grants are test-only.

`seed.js` creates a fresh catalog via authenticated ADMIN APIs and 30 separate booking customers/seats, plus 10 customers sharing one seat on another screening. Only the generated seed account is promoted via SQL in the isolated database. `BOOKING_COUNT` optionally accepts 1–200. Tokens expire after one hour; seed close to execution. Tokens/passwords are never included in the published baseline. Fixture JSON contains tokens and must not be committed or shared.

## Workloads

- Catalog: movies + screenings; 15-second warm-up excluded, then 60 seconds each at 1/5/10 VUs.
- Availability: fixed seat map; separate 15-second warm-up excluded, then 60 seconds each at 1/5/10 VUs.
- Booking: finite unique fixtures, up to 5 VUs, add → checkout → fake pay → confirmed booking → matching ticket.
- Seat contention: 10 VUs, one attempt each on the same screening/seat. Exactly one winner and nine HTTP 409 conflicts are required. Only this request treats 409 as expected; HTTP 5xx, other status codes, mismatched JSON or dropped iterations fail the run.

Read workloads are closed-loop and unpaced (each VU sends requests as fast as responses arrive); they are not an arrival-rate model or production capacity estimate. Finite booking/contention percentiles come from small samples. Container/host metrics and image digests accompany the baseline. No latency target is invented before measurement.

`verify.js` independently queries MySQL for confirmed bookings, permanent reservations, tickets and successful payments on both fixture screenings. `smoke.js` verifies authentication/role denial, recreates app/database containers with `down` **without `-v`**, and checks all persisted counts and an existing token after restart. Do not run it while benchmark traffic is active.

## Results and reruns

Raw summaries/logs go to `benchmarks/results/`; `baseline.json` excludes warm-ups. `consistency.json` and `smoke.json` provide correctness evidence. Each run consumes seats. For another run, archive the fixture and results directories outside their current paths, seed fresh IDs, and rerun; the scripts refuse to overwrite an existing current fixture/baseline. Do not reuse booked seats. Persistent old catalog data changes read payload sizes: for comparable measurements use a new isolated volume and document the dataset.

Stop the benchmark stack with `docker compose --env-file .env.m12 -p cinema-bench down`; the named volume remains. Deleting that project's volume is destructive and optional, never part of normal startup. The local/demo project should use a different name (`cinema-local`) and port (`8081`). No default/admin credentials are distributed.
