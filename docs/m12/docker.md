# M12 — Docker

The multi-stage Dockerfile uses Java 25 and the repository Maven Wrapper. Image packaging skips tests; `mvn clean test` and the isolated container verification must pass separately. Runtime UID/GID is 10001, not root. `curl` supplies a readiness check through `/movies`, exercising both HTTP and JPA without adding a public health endpoint.

## Local setup

Install Docker Desktop with Linux containers. In Windows CMD at the repository root:

```bat
copy .env.example .env
```

Edit `.env`: set application and root database passwords, leave APP_PORT=8081 (or another free loopback port).

```bat
docker compose -p cinema-local config --quiet
docker compose -p cinema-local up --build -d --wait
docker compose -p cinema-local ps
docker compose -p cinema-local logs -f app
```

MySQL 8.4 is reachable as `db:3306` inside the Compose network; its port is not published on the host. The app is bound to `127.0.0.1:8081`. Existing host MySQL on 3306 is untouched. Flyway applies V1–V3 and Hibernate validates; the application DB user is separate from root. Compose waits for a successful authenticated MySQL query before starting the app. Only local development is configured; deploy HTTPS/reverse proxy and operational secrets separately.

## Lifecycle

`docker compose -p cinema-local down` removes containers/network and retains the project-scoped `mysql-data` volume. Starting the same project again reuses data. Do not use `down -v` unless you explicitly intend to destroy that project's database. Changing MYSQL_PASSWORD in the env file does not rotate credentials already stored in an initialized MySQL volume; perform an intentional DB password rotation instead.

`.env` is not committed or copied into the image. Credentials are environment variables for local Compose, not a production secrets platform. Never publish `docker compose config` output containing resolved passwords; use `config --quiet` for validation. Base images use version tags; verification records the resolved digests in the baseline report.

See [benchmark instructions](../../benchmarks/README.md) for the separate `cinema-bench` environment and containerized H2/MySQL regression runner. [Acceptance](acceptance.md) records actual outcomes.
