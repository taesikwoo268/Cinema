# Cinema V1 MySQL profile

Package 2 starts from main `8f1b6f6`, which includes Package 1 commit `9a6be03`.

## Local setup

Use a dedicated MySQL 8.4+ database and an account restricted to that database. With your administrator session:

```sql
CREATE DATABASE cinema CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
```

Set environment variables outside Git (create/grant the account using your normal administration process):

- `CINEMA_DB_URL`: `jdbc:mysql://localhost:3306/cinema?connectionTimeZone=UTC`
- `CINEMA_DB_USER`: your database account
- `CINEMA_DB_PASSWORD`: its password

Run `mvn spring-boot:run -Dspring-boot.run.profiles=mysql`.
Flyway applies `V1__init_schema.sql` and `V2__retain_deleted_movie_references.sql`; Hibernate only validates the schema. The initial V1 file was an empty, inactive scaffold before this package. Start with a fresh database. No credentials or local data belong in Git.

`venue-dev` remains in-memory. `mysql` selects only persistent adapters, even if both profiles are supplied. With neither profile, HTTP remains deny-all. The mysql routes use the same CurrentUser/ownership boundaries as venue-dev. Accounts and password hashes persist, but existing login tokens still have no authentication filter; this package does not introduce production authentication.

## Transactions and consistency

Infrastructure wraps existing command-handler `handle` calls in READ COMMITTED transactions using Spring proxies. Application classes only lose `final` so they can be proxied; no Spring/JPA dependencies enter Application or Domain. Adapter calls also have transaction boundaries when used independently.

Customer row locks serialize cart creation, mutations and checkout for that customer. Booking row locks protect payment retries and cancellation. Screening row locks coordinate temporary holds and permanent reservations; hall row locks prevent overlapping screening inserts. These are database locks shared by independent application instances, not Java monitors. Deadlock/constraint conflicts at command boundaries return the existing application conflict response; callers may retry. Screening-level serialization is deliberately coarse for V1.

Database keys enforce one active cart/customer, one screening-seat hold and reservation, unique ticket IDs/codes and booking-seat tickets, and at most one successful payment per booking. Ordered CartItem/BookingItem collections retain price snapshots. Foreign keys protect historical relationships. Movie deletion is soft deletion so the existing delete/read API still works while historical screenings and tickets remain valid. Amounts outside DECIMAL(38,12) are rejected instead of silently rounded; dates use UTC with microsecond precision. Passwords use salted PBKDF2 hashes.

A payment transaction records the result, confirms the booking, reserves all seats and issues all tickets together. Failure rolls back all database changes. Retrying a committed success returns its original result without calling the gateway. Ticket state is not reset by retry.

**Gateway failure window:** the configured FakePaymentGateway is synchronous and local, so no external network call occurs inside the transaction. A gateway success followed by rollback/process failure before commit has no durable successful payment record; retry can call the gateway again. A test deliberately demonstrates this window. Database transactions do not make gateway calls atomic. Before introducing a real network provider, split claim/call/finalize and add provider-side idempotency/reconciliation; do not put network calls inside the present transaction boundary.

## Verification

`mvn clean test` runs the existing suite plus H2 persistence tests. For the same contract against a dedicated MySQL test database, set `CINEMA_TEST_MYSQL_URL`, `CINEMA_TEST_MYSQL_USER`, and `CINEMA_TEST_MYSQL_PASSWORD`, then run `mvn clean test`. Test fixtures use fresh IDs and leave data in that dedicated database. When the test URL is absent only the new MySQL contract is skipped; existing tests are unchanged.

The shared persistence contract covers round trips, rollback after a gateway success and partial ticket write, raw SQL uniqueness, independent application-context races, multiple-screening checkout, HTTP checkout/payment/tickets, and complete application-context restart. H2 uses the same migrations without MySQL table options; it is not a substitute for the MySQL run.

Verified locally against actual MySQL 9.4.0 as well as H2. The Spring Boot-managed Flyway 11.7.2 emits a warning that MySQL 9.4 is newer than its tested range; migrations, validation, constraints and the workflow contract are nevertheless exercised on that server. Other MySQL versions require their own verification before deployment.

Final verification: `mvn clean test` with MySQL enabled passed 758 tests, 0 failures, 0 errors, 8 pre-existing skipped tests; `git diff --check` passed. Both application restart and actual isolated MySQL server restart were verified: all 32 tickets and 19 confirmed bookings present before the server restart remained afterward, with migrations 1 and 2 intact.
