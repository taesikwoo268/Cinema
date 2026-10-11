# M7 — Fake Payment

> Hồ sơ phân công M7 tại thời điểm triển khai, giữ để tra cứu attribution; không phải backlog hiện tại. Fake Payment đã có, Package 1 bổ sung ticket/finalization/retry, Package 2 bổ sung transaction MySQL và Package 3 xác thực Bearer. Xem [tiến độ hiện tại](../cinema-v1-progress.md) trước khi giao việc hoặc dùng hướng dẫn security cũ.

## Goal

M7 triển khai thanh toán giả để hoàn thiện transition:

PENDING Booking
-> payment
-> Payment record
-> CONFIRMED Booking

Ticket không thuộc M7; Ticket được triển khai ở M8.

## Team Allocation

### Bảo Long — Payment Domain

Phạm vi:

- Payment
- PaymentStatus
- domain invariants
- Payment domain tests

Không sửa Application, Web hoặc configuration.

### Duy Anh — PayBooking Application

Phạm vi:

- PayBookingCommandHandler
- PayBookingCommandHandlerTest

Flow:

validate
-> authenticate
-> Customer
-> Booking
-> ownership
-> require PENDING
-> PaymentGateway.pay()
-> save Payment
-> success: Booking.confirm()
-> save Booking

Không implement Infrastructure adapter.

### Duy Khanh — Payment Infrastructure

Phạm vi:

- FakePaymentGateway
- InMemoryPaymentRepository
- tests cho hai adapter

Fake gateway V1 không gọi provider bên ngoài.

Không sửa PayBookingCommandHandler.

### Nguyễn Trung Nghĩa — Integration Lead

Phạm vi:

- PaymentConfiguration
- UseCaseConfiguration wiring
- PaymentController / security khi đến integration job
- integration tests
- full regression
- architecture review
- merge/review ba phần còn lại
- cập nhật documentation khi đóng M7

## Shared Contracts

Các thành viên không tự ý thay đổi:

- PaymentGateway
- PaymentRepository
- PaymentResult
- PayBookingCommand

Nếu cần thay đổi contract phải thống nhất trước.

## Integration Order

Bảo Long — Domain
        ↓
Duy Khanh — Infrastructure
        ↓
Duy Anh — Application
        ↓
Nguyễn Trung Nghĩa — Integration
        ↓
M7 Acceptance
