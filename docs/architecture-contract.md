# Cinema — Architecture & Domain Specification

Tài liệu mô tả kiến trúc, mô hình domain, quy tắc nghiệp vụ và các quyết định thiết kế của hệ thống Cinema. Mục 14 tổng hợp các hợp đồng và ràng buộc triển khai.

Đây là đặc tả thiết kế; trạng thái triển khai xem [README](../README.md). Bảng, khóa, ERD và transaction xem [Lược đồ quan hệ](relational-schema.md).

## 1. Bối cảnh hệ thống

```text
Cinema là hệ thống đặt vé xem phim trực tuyến.
Có hai nhóm người dùng chính:
CUSTOMER
   │
   ├── Xem danh sách phim
   ├── Xem lịch chiếu
   ├── Chọn suất chiếu
   ├── Chọn ghế
   ├── Thêm ghế vào giỏ
   ├── Checkout
   ├── Thanh toán
   ├── Nhận vé
   └── Xem / hủy booking

ADMIN
   │
   ├── Quản lý Movie
   ├── Quản lý Cinema
   ├── Quản lý Hall / Seat
   ├── Tạo Screening
   └── Quản lý lịch chiếu
Luồng nghiệp vụ trung tâm:
Movie
  ↓
Screening
  ↓
Select Seat
  ↓
Seat Hold
  ↓
Cart
  ↓
Checkout
  ↓
Booking (PENDING)
  ↓
Payment
  ↓
Booking (CONFIRMED)
  ↓
Ticket
Đây chính là “xương sống” để suy ra Domain Model và Application Use Cases.
```

## 2. Yêu cầu chức năng

```text
Có thể chia thành các nhóm chức năng.
Feature	Chức năng chính
Identity	Register, Login, xác định current user
Movie	Xem/tạo/xóa phim
Cinema	Cinema, Hall, Seat
Screening	Tạo lịch chiếu, xem lịch chiếu, hủy lịch
Cart	Thêm/xóa ghế, xem giỏ
Booking	Checkout, xem booking, hủy booking
Payment	Thanh toán booking
Ticket	Phát hành và xem vé
Từ đây có thể xác định một nguyên tắc:
Feature ≠ Entity

Feature mô tả khả năng của hệ thống.
Entity mô tả khái niệm nghiệp vụ.
Ví dụ Checkout là một chức năng nhưng không cần Checkout entity.
```

## 3. Phân loại luật

```text
Các quy tắc được phân thành ba nhóm theo trách nhiệm và vị trí trong kiến trúc.
┌─────────────────────────────────────┐
│          DOMAIN RULES               │
│    Luật của nghiệp vụ Cinema        │
├─────────────────────────────────────┤
│        APPLICATION RULES            │
│  Luật của một workflow/use case     │
├─────────────────────────────────────┤
│       EXTERNAL / TECHNICAL          │
│ DB, JWT, HTTP, Redis, Payment API   │
└─────────────────────────────────────┘
3.1 Domain Rules
Đây là những luật vẫn tồn tại kể cả khi bỏ Spring Boot, MySQL, REST API và JWT.
Screening
startTime < endTime

basePrice >= 0

CANCELLED screening
→ không được đặt vé

COMPLETED screening
→ không được đặt vé
Hai suất chiếu trong cùng Hall không được chồng thời gian.
Tuy nhiên việc tìm các screening khác trong database không phải trách nhiệm của Domain.
Seat
Seat là ghế vật lý:
Seat
├── id
├── hallId
├── row
├── number
└── type
Không nên:
Seat.status = BOOKED
bởi cùng một ghế:
A5

10:00 → BOOKED
13:00 → AVAILABLE
16:00 → HELD
Trạng thái đặt ghế phụ thuộc:
(Screening, Seat)
chứ không phụ thuộc Seat một mình.
Cart
Cart
├── customerId
└── CartItem[]
Domain invariants:
Cart phải có owner

Không được:
(screeningId=X, seatId=A5)
xuất hiện 2 lần

CartItem phải hợp lệ

Total = Σ unitPrice
Booking
Một Booking thuộc một Screening.
Booking
├── customerId
├── screeningId
├── BookingItem[]
├── totalAmount
└── status
Trạng thái:
               confirm()
PENDING ─────────────────→ CONFIRMED
   │
   │ cancel()
   ↓
CANCELLED
Không được:
CANCELLED → CONFIRMED
CANCELLED → PENDING
Booking phải chứa ít nhất một BookingItem.
Ticket
Ticket chỉ được phát hành sau khi Booking được xác nhận.
VALID ──use()──→ USED

VALID ──cancel()──→ CANCELLED
Không được:
USED → VALID
CANCELLED → VALID
```

## 4. Application Rules

```text
Application trả lời câu hỏi:
Người dùng đang muốn làm gì?
Ví dụ:
"Thêm ghế A5 vào giỏ"
Domain Cart không thể tự biết:
User hiện tại là ai?
Screening có tồn tại không?
Seat A5 có tồn tại không?
Seat có thuộc Hall đó không?
Ghế đã được người khác đặt chưa?
Ghế đang bị người khác hold không?
Cart của user nằm ở DB nào?
Đây là Application orchestration.
Ví dụ:
AddSeatToCart
        │
        ▼
CurrentUser
        │
        ▼
CustomerRepository
        │
        ▼
ScreeningRepository
        │
        ▼
SeatRepository
        │
        ▼
kiểm tra availability
        │
        ▼
SeatHoldService.hold()
        │
        ▼
Cart.addItem()
        │
        ▼
CartRepository.save()
Trong đó:
Cart.addItem()
     ↑
DOMAIN RULE

load Screening
check ownership
query booked seats
SeatHoldService
repository.save()
     ↑
APPLICATION RULE
```

## 5. External / Infrastructure Rules

```text
Đây là các chi tiết hệ thống cần có nhưng không phải nghiệp vụ Cinema.
Ví dụ:
Authentication
JWT

Spring Security

HTTP / REST

MySQL

JPA / Hibernate

Redis

RabbitMQ

VNPay / Stripe

Docker
Ví dụ đăng nhập:
POST /identity/login
        ↓
LoginCommand
        ↓
LoginCommandHandler
        ↓
IdentityService
        ↓
Infrastructure
        ↓
JWT implementation
Request tiếp theo:
HTTP
 │
 │ Authorization: Bearer JWT
 ↓
JwtAuthenticationFilter
 ↓
Spring Security Context
 ↓
SpringCurrentUser
 ↓
CurrentUser interface
 ↓
Application Handler
Application chỉ cần biết:
CurrentUser
chứ không cần biết JWT hoạt động thế nào.
```

## 6. Domain Model

```text
Từ các yêu cầu và luật trên mới suy ra Entity.
                         CUSTOMER
                            │
                            │ owns
                            ▼
                          CART
                            │
                            │ contains
                            ▼
                        CART ITEM
                            │
             ┌──────────────┴─────────────┐
             │                            │
             ▼                            ▼
        SCREENING                        SEAT
             │                            │
             │                            ▼
             │                           HALL
             │                            │
             ▼                            ▼
           MOVIE                       CINEMA


Cart
 │
 │ checkout
 ▼
BOOKING
 │
 ├─────────────→ BOOKING ITEM
 │
 │
 ▼
PAYMENT
 │
 │ success
 ▼
TICKET
Các entity chính:
Customer
Movie

Cinema
Hall
Seat

Screening

Cart
CartItem

Booking
BookingItem

Payment
Ticket
Value Object quan trọng:
Money
Enums:
SeatType

ScreeningStatus
BookingStatus
PaymentStatus
TicketStatus
```

## 7. Từ yêu cầu → Command / Query

```text
Application điều phối các bước của use case.
Command
Command = yêu cầu thay đổi trạng thái hệ thống.
CreateMovieCommand
DeleteMovieCommand

CreateScreeningCommand
CancelScreeningCommand

AddSeatToCartCommand
RemoveSeatFromCartCommand
ClearCartCommand

CheckoutCartCommand
CancelBookingCommand

PayBookingCommand

RegisterCommand
LoginCommand
Query
Query = đọc dữ liệu, không thay đổi business state.
GetMoviesQuery
GetMovieQuery

GetScreeningsQuery
GetScreeningQuery
GetAvailableSeatsQuery

GetMyCartQuery

GetMyBookingsQuery
GetBookingQuery

GetMyTicketsQuery
GetTicketQuery
```

## 8. Command → Handler

```text
Mỗi use case có request riêng và handler riêng.
Ví dụ:
AddSeatToCartCommand
        │
        ▼
AddSeatToCartCommandHandler
        │
        ├── CustomerRepository
        ├── ScreeningRepository
        ├── SeatRepository
        ├── CartRepository
        └── SeatHoldService
Handler chịu trách nhiệm:
LOAD
 ↓
CHECK APPLICATION RULES
 ↓
EXECUTE DOMAIN
 ↓
SAVE
Hay công thức ngắn:
Handler =
orchestration
+
Domain invocation
+
persistence coordination
Handler không phải Entity và cũng không phải Controller.
```

## 9. Repository / Port

```text
Handler không được biết JPA.
Vì vậy Application định nghĩa:
Application
│
├── CustomerRepository
├── MovieRepository
├── ScreeningRepository
├── CartRepository
├── BookingRepository
├── PaymentRepository
├── TicketRepository
│
├── CurrentUser
├── IdentityService
├── PaymentGateway
└── SeatHoldService
Đây là ports/contracts.
Ví dụ:
Application

BookingRepository
       △
       │ implements
       │
Infrastructure

JpaBookingRepositoryAdapter
       │
       ▼
Spring Data JpaRepository
       │
       ▼
Hibernate
       │
       ▼
Database
Điều này phù hợp với nguyên tắc Clean Architecture rằng dependency hướng vào trong và các chi tiết bên ngoài thực thi contract của lớp bên trong. (GitHub)
```

## 10. Infrastructure

```text
Infrastructure chứa implementation cụ thể.
Infrastructure
│
├── persistence
│   ├── JPA entities
│   ├── Spring Data repositories
│   ├── adapters
│   └── mappers
│
├── identity
│   └── JWT implementation
│
├── payment
│   └── PaymentGateway implementation
│
├── seat_hold
│   ├── DatabaseSeatHoldService
│   └── RedisSeatHoldService
│
└── messaging
    └── RabbitMQ
Ví dụ nâng cấp trong Phase 2:
Application

SeatHoldService
       △
       │
 ┌─────┴──────────┐
 │                │
Database       Redis
Phase 1        Phase 2
Application không cần thay đổi khi cơ chế hold chuyển từ DB sang Redis.
```

## 11. Web Layer

```text
Web chỉ làm nhiệm vụ chuyển đổi:
HTTP
 ↕
Application
Ví dụ:
POST /cart/seats
       │
       ▼
CartController
       │
       │ create
       ▼
AddSeatToCartCommand
       │
       ▼
Sender / Dispatcher
       │
       ▼
AddSeatToCartCommandHandler
Controller không nên chứa:
if seat booked...
if screening cancelled...
calculate price...
check booking status...
Những luật đó phải đi vào Application/Domain thích hợp.
```

## 12. Lược đồ kiến trúc tổng thể

```text
Sơ đồ tổng quan các tầng và hướng phụ thuộc:
                         USER
                           │
                    HTTP / REST
                           │
                           ▼
┌─────────────────────────────────────────────────────┐
│                       WEB                           │
│                                                     │
│ Controller   Security Filter   Exception Handler    │
└─────────────────────────┬───────────────────────────┘
                          │
                    Command / Query
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                   APPLICATION                       │
│                                                     │
│ Command → Handler                                   │
│ Query   → Handler                                   │
│                                                     │
│ Ports:                                              │
│ Repository │ CurrentUser │ PaymentGateway           │
│ IdentityService │ SeatHoldService                   │
└───────────────┬───────────────────△─────────────────┘
                │                   │ implements
                │ uses              │
                ▼                   │
┌───────────────────────────┐       │
│          DOMAIN           │       │
│                           │       │
│ Movie                     │       │
│ Screening                 │       │
│ Seat                      │       │
│ Cart                      │       │
│ Booking                   │       │
│ Payment                   │       │
│ Ticket                    │       │
│                           │       │
│ Business Rules            │       │
└───────────────────────────┘       │
                                    │
                    ┌───────────────┴──────────────┐
                    │       INFRASTRUCTURE          │
                    │                               │
                    │ JPA / Hibernate               │
                    │ MySQL                         │
                    │ Redis                         │
                    │ JWT                           │
                    │ RabbitMQ                      │
                    │ Payment Provider              │
                    └───────────────────────────────┘
Dependency rule:
Web ─────────────→ Application ─────────────→ Domain
                         ↑
                         │
                  Infrastructure
Domain không biết bất kỳ layer nào bên ngoài.
```

## 13. Lược đồ đầy đủ từ nghiệp vụ đến code

```text
Đây có thể là sơ đồ quan trọng nhất trong tài liệu:
┌──────────────────────┐
│ USER CONTEXT         │
│ "Yêu cầu đặt vé"     │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ FUNCTIONAL REQUIREMENT│
│ Add seat to cart     │
│ Checkout             │
│ Pay booking          │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ BUSINESS RULES       │
│                      │
│ seat available       │
│ screening bookable   │
│ no duplicate seat    │
│ valid transitions    │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ DOMAIN MODEL         │
│                      │
│ Screening            │
│ Seat                 │
│ Cart                 │
│ Booking              │
│ Payment              │
│ Ticket               │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ USE CASES            │
│                      │
│ Command / Query      │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ HANDLERS             │
│ Application workflow │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ PORTS                │
│ Repository           │
│ PaymentGateway       │
│ SeatHoldService      │
│ CurrentUser          │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ ADAPTERS             │
│                      │
│ JPA / JWT / Redis    │
│ RabbitMQ / Payment   │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ WEB                  │
│ REST Controller      │
│ Security Filter      │
└──────────────────────┘
Quy trình thiết kế chức năng mới:
Bối cảnh người dùng → yêu cầu chức năng → luật → Domain Model → Use Case → Command/Query → Handler → Port → Infrastructure Adapter → Controller.
```

## 14. Quyết định bổ sung và đối chiếu với code

Phần này giữ các nội dung riêng từ tài liệu kiến trúc cũ và làm rõ những ví dụ trong bản tổng quan.

### Ranh giới kiến trúc

- Domain dùng Java thuần, không import Spring/JPA/HTTP/JSON/JWT và không tự query database.
- Application không phụ thuộc Spring Web, Spring Security, JPA/Hibernate, Redis hoặc RabbitMQ; giao tiếp bên ngoài qua ports.
- Infrastructure phụ thuộc Application và Domain. `UseCaseConfiguration` nối implementation qua component scan; Web không cần import Infrastructure.
- Không gom mọi use case vào MovieService, BookingService, CartService hoặc PaymentService lớn.
- Validation đầu vào thuộc Application/validator; Domain bảo vệ invariant kể cả khi dữ liệu không đến từ HTTP. Web ánh xạ exception sang HTTP.
- Cart quản lý CartItem, Booking quản lý BookingItem. Không tạo aggregate khổng lồ từ Cinema đến Ticket; ranh giới aggregate Cinema/Hall/Seat còn cần chốt theo nhu cầu cập nhật.

### Domain và policy

- Customer liên kết account qua `userId`; credential thuộc Infrastructure Identity. Một account khách hàng tương ứng tối đa một Customer; Application kiểm tra uniqueness và ownership.
- Movie có title không blank, `durationMinutes > 0`. Money có amount không âm và currency hợp lệ.
- Seat thuộc một Hall; `(hallId, row, number)` không trùng. Cinema/Hall/Seat tham chiếu bằng ID.
- Mỗi Customer có một giỏ hiện tại. **Cart được chứa nhiều Screening; mỗi Booking chỉ thuộc một Screening.**
- BookingItem là snapshot riêng, không tham chiếu CartItem; clear giỏ không làm mất lịch sử. Domain BookingItem hiện chỉ có seatId và price; snapshot nhãn/số/loại ghế trong lược đồ là bổ sung đề xuất.
- Một BookingItem tạo đúng một Ticket, chỉ khi Booking CONFIRMED **và Payment SUCCESS**. `use()` ở sơ đồ tương ứng `markUsed()` trong code.
- Hủy Booking CONFIRMED/hoàn tiền, thời hạn hold/booking và currency còn cần chốt policy. Ví dụ hold 5 phút trong tài liệu cũ không phải cấu hình đã chốt.
- Movie có thể bổ sung genre, releaseDate, posterUrl sau; chưa coi là thuộc tính hiện có.

| Enum | Giá trị trong code |
|---|---|
| MovieStatus | COMING_SOON, SHOWING, ARCHIVED — policy còn cần chốt |
| SeatType | NORMAL, VIP, COUPLE |
| ScreeningStatus | SCHEDULED, CANCELLED, COMPLETED |
| BookingStatus | PENDING, CONFIRMED, CANCELLED |
| PaymentStatus | PENDING, SUCCESS, FAILED, REFUNDED |
| TicketStatus | VALID, USED, CANCELLED |

Chi tiết thuộc tính model và ánh xạ bảng được tập trung trong [lược đồ quan hệ](relational-schema.md), tránh duy trì thêm một bản danh sách riêng.

### Checkout, payment và ownership

```text
CheckoutCart:
CurrentUser → Customer → Cart → validate holds
    → group items by Screening → tạo/lưu Booking PENDING → clear Cart
Checkout chưa gọi payment.

PayBooking:
CurrentUser → load Booking → kiểm tra ownership và Booking PENDING
    → PaymentGateway.pay() → ghi nhận Payment
    → SUCCESS: confirm Booking, tạo Ticket, mark seats booked,
               release holds và save
    → FAILED: ghi nhận thất bại, không confirm/phát vé
```

Nếu lưu Cart sau khi acquire hold thất bại, cần rollback/bù hold. Chữ ký port hiện tại chưa giải quyết đầy đủ cạnh tranh đặt ghế. Luồng payment trên là logic nghiệp vụ, không bảo đảm nguyên tử giữa gateway và database; transaction, idempotency, retry và đối soát cần được triển khai theo [lược đồ quan hệ](relational-schema.md).

GetBooking, CancelBooking, PayBooking và GetTicket phải kiểm tra ownership. Với Ticket, xác định Customer qua Booking. Lấy danh tính từ CurrentUser, không dùng customerId trong request thay cho danh tính đăng nhập. Application ném exception; Web quyết định HTTP 403/404.

### Ports và implementation

- Bổ sung vào danh sách mục 9: SeatRepository, SeatReservationRepository, TokenVerifier, Clock, InputValidator và UseCase.
- SeatReservationRepository kiểm tra/ghi nhận ghế booked theo suất. TokenVerifier tách JWT filter khỏi JwtService implementation. Clock phục vụ test thời gian/deadline.
- Identity có thêm `GetCurrentUserQuery`. PaymentGateway có contract `pay` và `refund`; chữ ký chính xác xem source trong `application/common/interfaces`.
- Sender/Dispatcher ở mục 11 là phương án tùy chọn; code hiện inject handler trực tiếp. `POST /cart/seats` là ví dụ, chưa phải endpoint hoạt động; CartController hiện có tiền tố `/carts`.
- MySQL, Redis, RabbitMQ, gateway thật và Docker trong tổng quan là công nghệ minh họa/hướng mở rộng, không phải tất cả đã được tích hợp. DatabaseSeatHoldService và FakePaymentGateway hiện là skeleton.
- Port giúp thay implementation hold, nhưng chuyển DB sang Redis vẫn phải thiết kế tính nhất quán, transaction và xử lý lỗi; không mặc định chỉ đổi class là đủ.
- Quản trị Cinema/Hall/Seat và phân quyền ADMIN là yêu cầu, chưa có đầy đủ use case trong skeleton.

### Tổ chức package

```text
src/main/java/cinema/
├── domain/          common, customer, movie, cinema, screening, cart, booking, payment, ticket
├── application/
│   ├── common/      interfaces, exceptions, models
│   └── identity, movies, screenings, carts, bookings, payments, tickets
│       └── commands/ và/hoặc queries/ theo feature
├── infrastructure/
│   ├── persistence/ entities, repositories, adapters, mappers
│   ├── identity/    ApplicationUser, IdentityServiceImpl, JwtService
│   ├── payment/     FakePaymentGateway
│   ├── seat_hold/   DatabaseSeatHoldService
│   └── config/      UseCaseConfiguration, SystemClock
└── web/
    ├── controllers/
    ├── requests/
    ├── responses/
    ├── security/    JwtAuthenticationFilter, SpringCurrentUser, SecurityConfig
    └── exception/   GlobalExceptionHandler

src/test/java/cinema/
└── architecture/, domain/, application/, web/
```

### Thứ tự triển khai

1. Implement domain invariant, transition và unit test có assertion cụ thể.
2. Implement handler qua ports, validation, ownership và quyền quản trị; test bằng fake ports.
3. Chốt transaction, idempotency, thời hạn hold/booking, hủy/hoàn tiền và xử lý lỗi gateway/database.
4. Chọn database, hoàn thiện JPA mapping, mapper, constraint, migration và integration test cạnh tranh đặt ghế; sau đó bật JPA.
5. Implement Identity/JWT, principal, security policy và HTTP request/response.

Trạng thái triển khai được duy trì tại README; không lặp lại ở nhiều tài liệu.
