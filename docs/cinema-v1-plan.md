# Kế hoạch xây dựng Cinema V1

## Nguyên tắc triển khai

> **Nghiệp vụ tương đối đầy đủ, công nghệ triển khai cố tình đơn giản.**

Hệ thống gồm bốn tầng Domain, Application, Infrastructure và Web. Web phụ thuộc Application; Application phụ thuộc Domain; Infrastructure triển khai các port của Application. V1 bao gồm booking, payment, ticket, ownership và seat hold. JWT, Redis, RabbitMQ, cổng thanh toán thật và cơ chế concurrency nâng cao thuộc Phase 2. Các abstraction được xác định từ V1 để hỗ trợ thay thế implementation.

## 1. Mục tiêu V1

V1 cần chạy được toàn bộ happy path:

```text
Admin
  ↓
Tạo Cinema → Hall → Seat
  ↓
Tạo Movie
  ↓
Tạo Screening

────────────────────────────

Customer
  ↓
Login
  ↓
Xem Movie
  ↓
Chọn Screening
  ↓
Xem Seat
  ↓
Add Seat To Cart
  ↓
Checkout
  ↓
Booking PENDING
  ↓
Pay
  ↓
Booking CONFIRMED
  ↓
Ticket
```

V1 **không cần giống hệ thống rạp phim production**. Điều quan trọng là nghiệp vụ và kiến trúc đúng để sau này thay implementation.

---

# 2. Nguyên tắc đơn giản hóa

Phân biệt yêu cầu nghiệp vụ và implementation V1:

```text
Cái phải làm đúng                 Implementation có thể giả

Domain rules                      Password check → true
Application workflow              Payment → luôn SUCCESS
Entity relationships              Seat hold → DB/in-memory đơn giản
Ownership                         CurrentUser → hard-code/mock ban đầu
Booking state                     Không Redis
Ticket generation                 Không RabbitMQ
Repository abstraction            Có thể H2 trước
REST API                          Không OAuth
```

Ví dụ:

```java
public boolean verifyPassword(String raw, String stored) {
    return true;
}
```

V1 chấp nhận được.

Nhưng Application vẫn gọi:

```text
IdentityService.authenticate(...)
```

để sau này chỉ thay:

```text
FakeIdentityService
        ↓
BCrypt + JWT IdentityService
```

mà không sửa use case.

Đây chính là giá trị lớn nhất của thiết kế hiện tại.

---

# 3. Bản đồ toàn hệ thống

```text
                         CINEMA
                            │
       ┌────────────────────┼────────────────────┐
       │                    │                    │
    IDENTITY             CATALOG              VENUE
       │                    │                    │
     User                 Movie          Cinema/Hall/Seat
       │                    │                    │
       └────────────────────┼────────────────────┘
                            ↓
                       SCREENING
                            ↓
                       SEAT HOLD
                            ↓
                          CART
                            ↓
                        BOOKING
                            ↓
                        PAYMENT
                            ↓
                         TICKET
```

Thứ tự triển khai chi tiết được quy định trong roadmap M0–M12.

---

# 4. Giai đoạn 0 — Hoàn thiện nền kiến trúc

Giữ cấu trúc bốn tầng của scaffold hiện có.

Giữ:

```text
cinema/
├── domain/
├── application/
├── infrastructure/
└── web/
```

Dependency:

```text
Web ──────────→ Application ─────────→ Domain
                       ↑
                       │ implements
                Infrastructure
```

Cần hoàn thiện các thành phần chung trước:

```text
Domain
├── BaseEntity
├── Money
└── DomainException

Application
├── UseCase
├── repositories
├── CurrentUser
├── IdentityService
├── PaymentGateway
└── SeatHoldService
```

**Điều kiện hoàn thành:** `DomainRulesTest` và architecture tests pass.

---

# 5. Giai đoạn 1 — Cinema / Hall / Seat

Đây là dữ liệu nền để tạo screening.

## Domain

```text
Cinema
├── id
├── name
└── address

Cinema
  │
  └── Hall
       │
       └── Seat
```

Hall:

```text
Hall
├── id
├── cinemaId
└── name
```

Seat:

```text
Seat
├── id
├── hallId
├── row
├── number
└── type
```

SeatType:

```text
NORMAL
VIP
COUPLE
```

### Domain rules

```text
Cinema.name != blank

Hall.name != blank

Seat.row != blank
Seat.number > 0

(row, number)
unique trong Hall
```

Không tạo:

```text
Seat.status
```

vì trạng thái ghế phụ thuộc Screening.

## Application

Commands:

```text
CreateCinemaCommand
CreateHallCommand
CreateSeatCommand
```

Queries:

```text
GetCinemasQuery
GetCinemaQuery

GetHallsQuery
GetHallQuery

GetSeatsQuery
```

Repositories:

```text
CinemaRepository
HallRepository
SeatRepository
```

## Web

Ví dụ:

```text
POST /cinemas
POST /halls
POST /seats

GET /cinemas
GET /halls/{id}
GET /halls/{id}/seats
```

V1 chưa cần permission admin thật.

---

# 6. Giai đoạn 2 — Movie

## Domain

```text
Movie
├── id
├── title
├── description
├── durationMinutes
├── ageRating
└── status
```

Có thể đơn giản:

```text
MovieStatus
ACTIVE
INACTIVE
```

Rules:

```text
title != blank

durationMinutes > 0
```

Chưa cần:

```text
poster upload
S3
actor
director
review
rating system
genre phức tạp
```

## Application

```text
CreateMovieCommand
DeleteMovieCommand

GetMoviesQuery
GetMovieQuery
```

Flow:

```text
POST /movies
      ↓
CreateMovieCommand
      ↓
Handler
      ↓
Movie.create()
      ↓
MovieRepository.save()
```

---

# 7. Giai đoạn 3 — Screening

Đây là lúc các domain bắt đầu liên kết.

```text
Movie
   │
   ▼
Screening
   │
   ▼
Hall
```

Screening:

```text
Screening
├── id
├── movieId
├── hallId
├── startTime
├── endTime
├── basePrice
└── status
```

Status:

```text
SCHEDULED
CANCELLED
COMPLETED
```

### Domain rules

```text
startTime < endTime

basePrice >= 0

CANCELLED
→ không book

COMPLETED
→ không book
```

### Application rule

Hai screening cùng Hall:

```text
Screening A
10:00 ───────── 12:00

Screening B
        11:00 ───────── 13:00

                 ❌
```

Handler:

```text
CreateScreeningCommand
        ↓
load Movie
        ↓
load Hall
        ↓
ScreeningRepository.hasOverlap(...)
        ↓
Screening.create(...)
        ↓
save
```

Điểm quan trọng:

> `hasOverlap()` là Application + Repository coordination, không bắt Domain tự query database.

---

# 8. Giai đoạn 4 — Identity / Customer

Giai đoạn này bổ sung danh tính đăng nhập và hồ sơ khách hàng.

Tách:

```text
ApplicationUser
      │
      │ userId
      ▼
Customer
```

### ApplicationUser

Chỉ phục vụ authentication:

```text
id
email
password
role
```

### Customer

Domain business:

```text
id
userId
displayName
```

## V1 authentication cực đơn giản

Register:

```text
email + password
      ↓
IdentityService.register()
      ↓
ApplicationUser
```

Login:

```text
email + password
      ↓
IdentityService.login()
      ↓
password check = TRUE
      ↓
return fake token / simple result
```

Có thể tạm:

```text
token = "fake-token-" + userId
```

Không cần BCrypt.

Không cần RSA.

Không cần OAuth.

JWT thật có thể để sau.

Nhưng **interface phải đúng ngay từ đầu**:

```text
IdentityService
CurrentUser
```

Vì Application không được biết đang fake authentication.

---

# 9. Giai đoạn 5 — Cart + Seat Hold

Cart quản lý ghế được chọn; SeatHold quản lý quyền giữ ghế theo suất chiếu.

Customer chọn:

```text
Screening #15
Seat A5
```

Cấu trúc dữ liệu:

```text
Cart
└── CartItem
      ├── screeningId
      ├── seatId
      └── unitPrice
```

Domain rules:

```text
Cart có owner

không duplicate:
(screeningId, seatId)

unitPrice >= 0
```

## AddSeatToCart

Use case AddSeatToCart thể hiện cách Application điều phối các port:

```text
AddSeatToCartCommand
        ↓
Handler
        ↓
CurrentUser
        ↓
CustomerRepository
        ↓
ScreeningRepository
        ↓
SeatRepository
        ↓
check screening bookable
        ↓
check seat belongs to Hall
        ↓
check seat isn't booked
        ↓
SeatHoldService.hold()
        ↓
Cart.addItem()
        ↓
CartRepository.save()
```

## Seat Hold V1

Không Redis.

Có thể implementation cực đơn giản:

```text
DatabaseSeatHoldService
```

hoặc thậm chí:

```java
Map<ScreeningSeat, CustomerId>
```

Interface vẫn là:

```text
SeatHoldService
        ↑
        │
SimpleSeatHoldService     V1
        │
        └────────→ RedisSeatHoldService     tương lai
```

Phase 2 có thể thay SimpleSeatHoldService bằng RedisSeatHoldService.

---

# 10. Giai đoạn 6 — Checkout / Booking

Đây là nghiệp vụ trung tâm.

```text
Cart
 ↓
Checkout
 ↓
Booking
```

Booking:

```text
Booking
├── id
├── customerId
├── screeningId
├── items[]
├── totalAmount
├── status
└── createdAt
```

BookingItem:

```text
seatId
price
```

Status:

```text
PENDING
CONFIRMED
CANCELLED
```

State machine:

```text
                  confirm()
PENDING ─────────────────────────→ CONFIRMED
   │
   │ cancel()
   ▼
CANCELLED
```

## Checkout

Nếu Cart có:

```text
Screening A
├── A1
└── A2

Screening B
└── C5
```

thì:

```text
Cart
 ↓ group by screening
 ├───────────────┐
 ↓               ↓
Booking #1      Booking #2
Screening A     Screening B
A1,A2           C5
```

Nguyên tắc:

> **Một Booking thuộc một Screening.**

Điều này làm domain đơn giản hơn đáng kể.

---

# 11. Giai đoạn 7 — Payment

V1 không cần cổng thanh toán thật.

Application định nghĩa:

```text
PaymentGateway
```

Infrastructure:

```text
FakePaymentGateway
```

Implementation V1:

```text
pay(...)
    ↓
return SUCCESS
```

Payment:

```text
Payment
├── id
├── bookingId
├── amount
├── status
└── providerReference
```

Status:

```text
PENDING
SUCCESS
FAILED
REFUNDED
```

Flow:

```text
PayBookingCommand
        ↓
load Booking
        ↓
check ownership
        ↓
Booking must PENDING
        ↓
PaymentGateway.pay()
        ↓
SUCCESS
        ↓
Payment SUCCESS
        ↓
Booking.confirm()
```

Sau này:

```text
FakePaymentGateway
        ↓
VNPayPaymentGateway
```

Application và Domain gần như không thay đổi.

---

# 12. Giai đoạn 8 — Ticket

Sau payment success:

```text
Booking
CONFIRMED
     ↓
for each BookingItem
     ↓
Ticket
```

Ví dụ:

```text
Booking #100

A1
A2
A3

        ↓

Ticket #501 → A1
Ticket #502 → A2
Ticket #503 → A3
```

Ticket:

```text
id
bookingId
screeningId
seatId
ticketCode
status
```

Status:

```text
VALID
USED
CANCELLED
```

V1 ticket code có thể cực đơn giản:

```text
TICKET-UUID
```

Chưa cần QR code.

---

# 13. Giai đoạn 9 — Ownership

Hoàn thiện kiểm tra quyền sở hữu tài nguyên tại Application.

Ví dụ:

```text
Customer A

GET /bookings/123
```

Handler:

```text
CurrentUser
 ↓
Customer A
 ↓
BookingRepository.findById(123)
 ↓
booking.customerId == A.id ?
       │
    ┌──┴──┐
   YES    NO
    │      │
 return   throw
```

Áp dụng cho:

```text
GetBooking
CancelBooking
PayBooking
GetTicket
```

Đây là **Application Rule**, không phải Controller rule.

---

# 14. Giai đoạn 10 — REST API hoàn chỉnh

Khi các use case hoạt động mới nối đầy đủ Web.

API V1 có thể gọn như sau:

```text
IDENTITY
POST   /identity/register
POST   /identity/login

MOVIES
GET    /movies
GET    /movies/{id}
POST   /movies
DELETE /movies/{id}

SCREENINGS
GET    /screenings
GET    /screenings/{id}
POST   /screenings
DELETE /screenings/{id}

SEATS
GET    /screenings/{id}/seats

CART
GET    /cart
POST   /cart/items
DELETE /cart/items/{id}
DELETE /cart

BOOKING
POST   /bookings/checkout
GET    /bookings
GET    /bookings/{id}
DELETE /bookings/{id}

PAYMENT
POST   /bookings/{id}/payment

TICKET
GET    /tickets
GET    /tickets/{id}
```

Controller chỉ:

```text
HTTP
 ↓
Request DTO
 ↓
Command / Query
 ↓
Sender
 ↓
Response DTO
 ↓
HTTP
```

---

# 15. Giai đoạn 11 — Persistence

Lúc đầu không cần database architecture phức tạp.

Giữ abstraction:

```text
Application
BookingRepository
        △
        │
Infrastructure
JpaBookingRepositoryAdapter
        ↓
Spring Data
        ↓
Database
```

Có thể dùng H2 cho phát triển/test trước.

Sau đó chuyển MySQL/PostgreSQL mà Domain không đổi.

JPA entity nằm:

```text
infrastructure/persistence/entities
```

Không:

```text
domain/Booking.java

@Entity       ❌
@Table        ❌
```

---

# 16. Giai đoạn 12 — Testing

Không cần viết hàng trăm test ngay.

Ưu tiên ba tầng.

### Domain test

Test các luật quan trọng:

```text
Cart cannot contain duplicate seat

Screening start < end

Booking cannot confirm after cancelled

Ticket cannot become VALID after USED

Money cannot be negative
```

### Application test

Mock repositories:

```text
AddSeatToCartHandlerTest

CheckoutCartHandlerTest

PayBookingHandlerTest

CancelBookingHandlerTest
```

### Integration test

Chỉ vài happy path:

```text
register → login

create movie → screening

add seat → cart

checkout → payment → ticket
```

Mục tiêu cuối cùng phải test được:

```text
Customer
 ↓
Movie
 ↓
Screening
 ↓
A5
 ↓
Cart
 ↓
Checkout
 ↓
Booking PENDING
 ↓
Payment SUCCESS
 ↓
Booking CONFIRMED
 ↓
Ticket VALID

              ✔
```

---

# 17. Những công nghệ cố tình CHƯA làm

Phạm vi công nghệ được phân chia giữa V1 và Phase 2 như sau.

V1:

```text
Password validation      → đơn giản
Authentication           → fake/simple
Payment                  → always SUCCESS
Seat hold                → simple DB/memory
Database                 → basic relational DB
Messaging                → none
Caching                  → none
Concurrency              → basic
Deployment               → Docker đơn giản
```

Sau khi hệ thống hoàn chỉnh và benchmark, Phase 2 mới nâng:

```text
V1                           Phase 2

Simple password       → BCrypt
Simple auth           → JWT/RSA
Simple seat hold      → Redis + TTL
Basic booking         → locking/transaction
Fake payment          → VNPay/Stripe
Synchronous           → RabbitMQ where justified
No cache              → Redis cache
Basic queries         → query optimization/indexes
Basic monitoring      → metrics/logging
```

Benchmark đặt ghế tập trung vào race condition, locking và transaction để đánh giá khả năng ngăn hai khách hàng đặt cùng một ghế trong một suất chiếu. ([GitHub][1])

---

# 18. Roadmap code cụ thể

Kế hoạch gồm **13 milestone (M0–M12)**. Mỗi milestone phải đạt tiêu chí hoàn thành trước khi chuyển sang bước tiếp theo:

```text
M0  Architecture foundation
 │
 ▼
M1  Cinema / Hall / Seat
 │
 ▼
M2  Movie
 │
 ▼
M3  Screening
 │
 ▼
M4  Identity + Customer
 │
 ▼
M5  Cart + simple SeatHold
 │
 ▼
M6  Checkout + Booking
 │
 ▼
M7  Fake Payment
 │
 ▼
M8  Ticket
 │
 ▼
M9  Ownership / Authorization
 │
 ▼
M10 REST Controllers + Exception handling
 │
 ▼
M11 Persistence + integration tests
 │
 ▼
M12 Docker + OpenAPI + Benchmark
```

Mỗi milestone dùng cùng một chu trình:

```text
Requirement
    ↓
Domain rule
    ↓
Entity / Value Object
    ↓
Repository Port
    ↓
Command / Query
    ↓
Handler
    ↓
Infrastructure Adapter
    ↓
Controller
    ↓
Tests
    ↓
Commit
```

Mỗi milestone được triển khai thành một commit hoặc một nhóm commit nhỏ để thuận tiện review, kiểm thử và theo dõi thay đổi.

## Mục tiêu cuối Phase 1

```text
                    ┌───────────┐
                    │   USER    │
                    └─────┬─────┘
                          ↓
                     REST / JSON
                          ↓
┌─────────────────────────────────────────────┐
│ WEB                                         │
│ Controller + simple authentication filter   │
└────────────────────┬────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│ APPLICATION                                 │
│                                             │
│ Command/Query → Handler                     │
│                                             │
│ Repository / CurrentUser / PaymentGateway   │
│ SeatHoldService / IdentityService           │
└──────────────┬──────────────────△───────────┘
               ↓                  │
┌─────────────────────────┐       │
│ DOMAIN                  │       │
│                         │       │
│ Cinema/Hall/Seat        │       │
│ Movie/Screening         │       │
│ Customer/Cart           │       │
│ Booking/Payment/Ticket  │       │
│                         │       │
│ REAL BUSINESS RULES     │       │
└─────────────────────────┘       │
                                  │
┌─────────────────────────────────┴───────────┐
│ INFRASTRUCTURE                              │
│                                             │
│ JPA              Simple Identity            │
│ DB               Fake Payment               │
│ Simple SeatHold                             │
└─────────────────────────────────────────────┘
```

V1 giữ các quy tắc nghiệp vụ trong Domain và sử dụng implementation đơn giản ở Infrastructure. Khi bổ sung Redis, JWT, RabbitMQ, thanh toán thật hoặc concurrency control, thay đổi tập trung ở Infrastructure adapter và cơ chế điều phối của Application.

[Cinema repository – refactor/clean-architecture](https://github.com/taesikwoo268/Cinema/tree/refactor/clean-architecture)

Thứ tự triển khai bắt đầu từ **M0 → M1** và tiếp tục theo roadmap. Mỗi milestone cần có đặc tả phạm vi, quy tắc nghiệp vụ, file liên quan và tiêu chí nghiệm thu. Trạng thái hiện tại được cập nhật trong [Tiến độ Cinema V1](cinema-v1-progress.md).

[1]: https://github.com/T-Lak/movie-booking-backend "GitHub - T-Lak/movie-booking-backend: Scalable Movie Booking System API handling real-time seat reservations, concurrency control, and atomic transactions. · GitHub"
