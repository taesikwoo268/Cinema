# M12 Acceptance

Verified on 2026-10-10 on `feature/nghia/m12-delivery`; this does not assert a merge or production deployment.

## Delivery

- Docker image builds and runs as a non-root user.
- Docker Compose starts the application and MySQL.
- MySQL data is stored in a persistent Docker volume.
- OpenAPI validation passed: 31 operations match controllers and DTOs.

## Verification

- Full Maven suite against MySQL 8.4:
  787 tests, 0 failures, 0 errors, 8 skipped.
- Benchmark completed.
- Database consistency checks passed.
- HTTP security and restart-persistence smoke checks passed.
- Sanitized benchmark report generated without tokens or passwords.

## Evidence

- [Benchmark report](benchmark-baseline.md)
- [Baseline data](baseline.json)
- [Docker guide](docker.md)
- [API guide](api.md)
- [Phase 1 Kaggle CPU verification](../phase1/kaggle-results.md) — separate Java/MySQL runtime and dataset; includes the measured latency threshold breach at 50 users.

## Limitations

- Payment uses FakePaymentGateway.
- Gateway success and database commit are not atomic.
- Benchmark results describe the local test environment,
  not production capacity.
- Existing skipped tests remain skipped.
- The benchmark database retains an earlier run; catalog reads include that data.
- Fixture tokens expire after one hour; generated passwords are not retained. Complete verification promptly after seeding.
