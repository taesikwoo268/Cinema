# M5 Implementation

## Domain

`Cart` và `CartItem` giữ business state của giỏ.

Domain không phụ thuộc Spring, Web hoặc Infrastructure.

## Application

Bốn use case đã triển khai:

- `AddSeatToCartCommandHandler`
- `RemoveSeatFromCartCommandHandler`
- `ClearCartCommandHandler`
- `GetMyCartQueryHandler`

Application chịu trách nhiệm authentication/ownership, lookup các resource,
seat availability và orchestration qua các port.

## Ports

M5 sử dụng:

- `CartRepository`
- `SeatHoldService`
- `SeatReservationRepository`
- `CurrentUser`
- `CustomerRepository`
- `ScreeningRepository`
- `SeatRepository`

## Infrastructure

Các adapter V1:

- `InMemoryCartRepository`
- `SimpleSeatHoldService`
- `InMemorySeatReservationRepository`

Các adapter in-memory được dùng để hoàn thiện business flow trước khi triển khai
JPA/Redis ở milestone sau.

## Configuration

`CartConfiguration` cung cấp các adapter M5 trong profile `venue-dev`.

`UseCaseConfiguration` inject dependency vào các Cart handlers.

## Web

`CartController` chỉ chuyển đổi HTTP thành command/query.

Endpoints:

- `POST /carts/seats`
- `DELETE /carts/seats`
- `DELETE /carts`
- `GET /carts/me`

`VenueDevSecurityConfig` mở đúng các route trên trong profile development.

## Transaction Limitation

Các repository/service V1 là in-memory và không cung cấp transaction thực sự.

Các handler Remove/Clear có compensation logic cho hold khi thao tác sau đó thất bại,
nhưng transaction database hoàn chỉnh được deferred.