# M3 Acceptance

## Automated Verification

Final clean checkpoint đã xác minh:

```text
Tests run: 299
Failures: 0
Errors: 0
Skipped: 8
BUILD SUCCESS
```

8 tests skipped thuộc `BookingWorkflowTest` hiện có.

Architecture checkpoint:

```text
6 tests
0 failures
0 errors
0 skipped
```

Screening regression:

```text
69 tests
0 failures
0 errors
0 skipped
```

`git diff --check`: **PASS**.

## Screening HTTP Acceptance

`ScreeningHttpAcceptanceTest` dùng `SpringBootTest` với RANDOM_PORT, `TestRestTemplate`, profile `venue-dev` và security thật. Prerequisite được tạo qua HTTP thật: Movie, Cinema và Hall.

Các scenario đã xác minh:

- create Screening hợp lệ;
- get one Screening;
- get list theo movie;
- cancel Screening;
- GET sau cancel vẫn trả resource với status `CANCELLED`;
- blank movie ID hoặc Hall ID;
- thời gian không hợp lệ;
- base price âm;
- Movie không tồn tại;
- Hall không tồn tại;
- overlap cùng Hall trả `409 CONFLICT`;
- touching boundary được tạo thành công;
- GET Screening không tồn tại;
- cancel Screening không tồn tại;
- cancel lần hai trả `409 CONFLICT`;
- conflict response có code `CONFLICT` và message;
- resource vẫn ở status `CANCELLED` sau cancel lần hai thất bại;
- malformed JSON/wrong field type trả validation response đã sanitize;
- unsupported routes/methods bị deny.

Các scenario trên được nhóm trong các test method hiện có; không phải mỗi scenario là một JUnit test method riêng.

## Architecture Acceptance

Web không phụ thuộc trực tiếp Domain. `DomainException` từ `Screening.cancel()` được dịch tại `CancelScreeningCommandHandler` thành `ConflictException`, sau đó Web dùng mapping Application exception hiện có để trả `409`.

`CleanArchitectureTest` pass 6/6.

## Exit Criteria

M3 được xem là hoàn thành trong phạm vi:

- Screening Domain;
- Application handlers;
- ScreeningRepository port;
- in-memory Infrastructure;
- venue-dev configuration;
- Screening Web API;
- Security boundary;
- unit, integration và HTTP acceptance tests.

Các chức năng deferred không block M3:

- GetAvailableSeats;
- SeatHold;
- Booking/Checkout;
- Payment thật;
- JPA Screening persistence;
- complete Screening transition.
