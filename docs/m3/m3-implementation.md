# M3 Implementation

## Domain

`Screening` là domain entity thuần Java với identity, movie/Hall reference, khoảng thời gian, giá và status. Constructor bảo vệ các invariant về ID, movie/Hall ID, thời gian và giá. Constructor tạo mới mặc định status `SCHEDULED`.

`Screening.cancel()` cho phép `SCHEDULED -> CANCELLED` và từ chối cancel lại Screening `CANCELLED` hoặc `COMPLETED` bằng `DomainException`.

Domain không phụ thuộc Spring, JPA, Web hoặc Application.

## Application

Bốn handler đã triển khai:

- `CreateScreeningCommandHandler`: validate input, kiểm tra Movie và Hall, kiểm tra overlap, tạo Screening và lưu.
- `CancelScreeningCommandHandler`: validate, lookup Screening, gọi `Screening.cancel()` và lưu transition thành công.
- `GetScreeningQueryHandler`: lookup theo ID và map sang `ScreeningView`.
- `GetScreeningsQueryHandler`: lookup theo movie ID, giữ thứ tự repository và map từng Screening sang `ScreeningView`.

Riêng Cancel, transition domain được dịch tại Application boundary:

```text
DomainException -> ConflictException
```

Việc này chỉ áp dụng cho use case CancelScreening. Web chỉ phụ thuộc exception ở Application boundary và không biết `DomainException`.

## Port

`ScreeningRepository` có đúng contract:

- `findById`
- `findByMovieId`
- `hasOverlap`
- `save`

Không có `findAll()`.

## Infrastructure

`InMemoryScreeningRepository` dùng:

- `LinkedHashMap<String, Screening>`;
- các method `synchronized`;
- insertion order deterministic cho `findByMovieId`;
- list snapshot không cho caller sửa collection nội bộ;
- `save` hỗ trợ update aggregate hiện có, cần thiết cho flow cancel;
- overlap cùng Hall theo công thức `existing.start < newEnd && existing.end > newStart`.

Các interval chạm biên không overlap.

**Lưu ý:** adapter in-memory giữ mutable `Screening` references. Vì vậy, mutate aggregate lấy từ repository có thể được quan sát trong map trước khi gọi `save()`. Đây là behavior chấp nhận được của development adapter hiện tại, không phải transaction semantics của database.

## Configuration

Trong profile `venue-dev`:

```text
ScreeningConfiguration
-> ScreeningRepository
-> InMemoryScreeningRepository
```

`UseCaseConfiguration` inject repository cho bốn handler Screening: Create, Cancel, Get one và Get list.

`ScreeningController` cũng chỉ active trong `venue-dev`.

## Web

`CreateScreeningRequest` có sáu field:

- `movieId`
- `hallId`
- `startTime`
- `endTime`
- `basePrice`
- `currency`

Controller chỉ chuyển đổi HTTP request/path/query thành command/query và chuyển kết quả handler thành HTTP response.

Các endpoint:

- `POST /screenings`
- `GET /screenings/{id}`
- `GET /screenings?movieId=...`
- `POST /screenings/{id}/cancel`

## Security

Venue-dev cho phép đúng các matcher:

- `POST /screenings`
- `GET /screenings`
- `GET /screenings/{id}`
- `POST /screenings/{id}/cancel`

CSRF exemption chỉ áp dụng cho:

- `POST /screenings`
- `POST /screenings/{id}/cancel`

`.anyRequest().denyAll()` vẫn được giữ và không có wildcard `/screenings/**`.

## Deferred

`GetAvailableSeatsQueryHandler` vẫn ném `FeatureNotImplementedException` và không có endpoint. JPA Screening persistence cũng chưa hoàn thành.
