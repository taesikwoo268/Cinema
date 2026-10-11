# M12 — OpenAPI

The [Cinema V1 OpenAPI 3.0.3 contract](../openapi/cinema-v1.yaml) describes all 31 current controller operations and their request/response records. It targets the `mysql` profile. Import the file into Postman; the Docker local server is `http://localhost:8081`, JVM local server `http://localhost:8080`.

```bat
npm --prefix benchmarks ci
npm --prefix benchmarks run validate:api
```

Validation checks OpenAPI syntax/references, exact method/path coverage against Java controller mappings, and documented field names against Java records. These checks do not prove every business rule or HTTP response; integration tests and smoke tests supply runtime evidence. Validators are development tools, with a lockfile, not backend dependencies.

Registration returns a customer ID. Login returns an opaque Bearer token (not JWT), valid for one hour. In Postman select Bearer Token and paste `accessToken`. Missing/invalid authentication on protected endpoints returns 403, matching the existing contract. Catalog mutation requires ADMIN; owner checks still apply to ADMIN on booking/payment/ticket resources. There is no refresh/logout/refund API.

DELETE `/carts/seats` takes a JSON body. GET `/screenings` requires `movieId`. GET available-seats is a snapshot, not a hold guarantee. Checkout returns one booking per screening. HTTP 200 from payment requires checking `successful`; committed successful retries do not charge again, but gateway success before a database rollback remains a documented failure window.

The shared 400/403/404/409 response entries describe the error envelope; not every operation necessarily produces every listed code. Unexpected infrastructure failures may return 5xx. No change to backend REST contracts or runtime OpenAPI endpoint is introduced.
