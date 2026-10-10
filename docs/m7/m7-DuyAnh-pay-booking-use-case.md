# M7-C — PayBooking Application Use Case

> Hồ sơ phân công M7 tại thời điểm triển khai, giữ để tra cứu attribution; không phải backlog hiện tại. Fake Payment đã có, Package 1 bổ sung ticket/finalization/retry, Package 2 bổ sung transaction MySQL và Package 3 xác thực Bearer. Xem [tiến độ hiện tại](../cinema-v1-progress.md) trước khi giao việc hoặc dùng hướng dẫn security cũ.

## 1. Mục tiêu

Implement use case:

PayBookingCommandHandler

Đây là orchestration chính của M7.

Job này chịu trách nhiệm:

authentication
customer resolution
booking lookup
ownership
booking state
PaymentGateway
Payment creation
Payment persistence
Booking confirmation
Booking persistence

---

## 2. Contract cố định

Input hiện tại:

PayBookingCommand(String bookingId)

KHÔNG thay đổi command shape.

Output:

PaymentResult

KHÔNG tạo DTO khác.

---

## 3. Dependencies

PayBookingCommandHandler constructor PHẢI là:

public PayBookingCommandHandler(
    CurrentUser currentUser,
    CustomerRepository customers,
    BookingRepository bookings,
    PaymentRepository payments,
    PaymentGateway paymentGateway,
    Supplier<String> idGenerator
)

Không thêm Clock.

Payment hiện không có createdAt.

Không inject concrete Infrastructure implementation.

---

## 4. Flow chính xác

handle(command)

        |
        v

validate command
        |
        v
validate bookingId
        |
        v
currentUser.isAuthenticated()
        |
        v
currentUser.getUserId()
        |
        v
CustomerRepository.findByUserId()
        |
        v
BookingRepository.findById()
        |
        v
check booking ownership
        |
        v
Booking must PENDING
        |
        v
PaymentGateway.pay(
    booking.id,
    booking.totalAmount
)
        |
        +--------------------------+
        |                          |
        | failed                   | success
        v                          v
create FAILED Payment        validate providerReference
        |                          |
        v                          v
PaymentRepository.save       create SUCCESS Payment
        |                          |
        v                          v
return result               PaymentRepository.save
                                   |
                                   v
                             Booking.confirm()
                                   |
                                   v
                             BookingRepository.save
                                   |
                                   v
                             return result

---

## 5. Validation order

Thứ tự validation phải cố định.

### Step 1

Nếu command == null:

throw ValidationException

Không gọi dependency nào.

### Step 2

bookingId null/empty/blank:

throw ValidationException

Không gọi dependency nào.

### Step 3

Nếu:

currentUser.isAuthenticated() == false

throw ForbiddenException.

Không đọc userId sau đó.

### Step 4

userId null/empty/blank:

throw ForbiddenException.

### Step 5

Customer không tồn tại:

throw NotFoundException.

Không lookup Booking trước khi resolve Customer.

### Step 6

Booking không tồn tại:

throw NotFoundException.

### Step 7

Booking không thuộc Customer:

throw ForbiddenException.

Không gọi PaymentGateway.

### Step 8

Booking status phải là:

PENDING

Nếu:

CONFIRMED
CANCELLED

throw ConflictException.

Không gọi PaymentGateway.

---

## 6. Amount gửi sang gateway

BẮT BUỘC dùng:

booking.getTotalAmount()

Không gọi:

booking.calculateTotal()

M6 đã tạo total snapshot khi checkout.

Payment phải dùng chính snapshot amount của Booking.

---

## 7. Gateway call

Gọi đúng một lần:

paymentGateway.pay(
    booking.getId(),
    booking.getTotalAmount()
)

Không retry trong M7.

Không gọi refund trong PayBooking.

---

## 8. Gateway failure

Nếu:

PaymentResult.successful() == false

thì:

Booking KHÔNG confirm.

Tạo Payment:

id = idGenerator.get()

bookingId = booking.getId()

amount = booking.getTotalAmount()

status = FAILED

providerReference = result.providerReference()

Sau đó:

payments.save(payment)

Return chính PaymentResult của gateway.

Không gọi:

booking.confirm()

Không gọi:

bookings.save(booking)

Booking phải còn PENDING.

---

## 9. Gateway success

Nếu:

result.successful() == true

thì providerReference bắt buộc:

not null
not empty
not blank

Nếu fake/external gateway vi phạm contract này:

throw IllegalStateException

Không persist malformed success Payment.

---

## 10. Success persistence

Tạo:

new Payment(
    idGenerator.get(),
    booking.getId(),
    booking.getTotalAmount(),
    PaymentStatus.SUCCESS,
    result.providerReference()
)

Sau đó thứ tự:

payments.save(payment)

booking.confirm()

bookings.save(booking)

return result

Không tự set BookingStatus.

Phải gọi domain:

booking.confirm()

---

## 11. Duplicate payment protection

Không cần query PaymentRepository để tự phát minh duplicate rule.

Protection của M7 dựa vào Booking lifecycle:

successful payment
    |
    v
Booking CONFIRMED

Lần PayBooking tiếp theo:

Booking != PENDING
    |
    v
ConflictException

Do đó PaymentGateway không được gọi lần hai cho Booking đã CONFIRMED.

FAILED payment không confirm Booking.

Vì vậy Booking vẫn PENDING và được phép thử PayBooking lại.

Đây là behavior cố định của M7.

---

## 12. Persistence limitation

M7 V1 chưa có database transaction.

Success flow:

save Payment
    |
confirm Booking
    |
save Booking

Nếu save Booking thất bại sau khi Payment đã save,
V1 chưa rollback Payment.

KHÔNG tự xây transaction/compensation trong job này.

Đây là deferred concern.

---

## 13. Files được phép sửa/tạo

Sửa:

src/main/java/cinema/application/payments/commands/PayBookingCommandHandler.java

Tạo:

src/test/java/cinema/application/payments/commands/PayBookingCommandHandlerTest.java

Sửa wiring:

src/main/java/cinema/infrastructure/config/UseCaseConfiguration.java

Không sửa PayBookingCommand.java trừ format/import thừa nếu thực sự cần.

---

## 14. UseCaseConfiguration

Thay bean stub:

new PayBookingCommandHandler()

bằng bean `venue-dev` có dependencies:

CurrentUser
CustomerRepository
BookingRepository
PaymentRepository
PaymentGateway

và:

() -> UUID.randomUUID().toString()

Ví dụ dependency contract:

@Bean
@Profile("venue-dev")
PayBookingCommandHandler payBookingCommandHandler(
    CurrentUser currentUser,
    CustomerRepository customerRepository,
    BookingRepository bookingRepository,
    PaymentRepository paymentRepository,
    PaymentGateway paymentGateway
)

return new PayBookingCommandHandler(
    currentUser,
    customerRepository,
    bookingRepository,
    paymentRepository,
    paymentGateway,
    () -> UUID.randomUUID().toString()
)

Không tạo PaymentRepository bean tại đây.

Không tạo PaymentGateway bean tại đây.

M7-B chịu trách nhiệm các bean đó.

---

## 15. Tests bắt buộc

PayBookingCommandHandlerTest phải cover ít nhất:

1. null command -> ValidationException

2. null bookingId -> ValidationException

3. empty bookingId -> ValidationException

4. blank bookingId -> ValidationException

5. unauthenticated -> ForbiddenException

6. invalid authenticated userId -> ForbiddenException

7. customer missing -> NotFoundException

8. booking missing -> NotFoundException

9. booking belongs to another customer -> ForbiddenException

10. CANCELLED booking -> ConflictException

11. CONFIRMED booking -> ConflictException

12. invalid state không gọi PaymentGateway

13. success gọi gateway đúng:
    bookingId
    totalAmount

14. success tạo Payment SUCCESS

15. Payment amount chính xác bằng booking total snapshot

16. Payment providerReference chính xác

17. success save Payment

18. success gọi Booking.confirm()

19. success save Booking

20. success trả đúng PaymentResult

21. failed gateway tạo Payment FAILED

22. failed gateway save Payment

23. failed gateway không confirm Booking

24. failed gateway không save Booking

25. failed gateway trả PaymentResult unsuccessful

26. successful result với null/blank providerReference bị reject

27. successful payment khiến Booking thành CONFIRMED

28. verify important interaction order:
    gateway.pay
    payments.save
    booking.confirm
    bookings.save

---

## 16. Không được làm

Không sửa:

Payment.java
PaymentGateway.java
PaymentRepository.java
FakePaymentGateway.java
InMemoryPaymentRepository.java
PaymentController.java
VenueDevSecurityConfig.java

Không thêm Spring annotation vào handler.

Không dùng concrete repository.

Không dùng FakePaymentGateway trực tiếp.

Không tạo Ticket.

Ticket thuộc M8.

Không release SeatHold tại payment.

Không clear Cart.

Cart đã xử lý ở checkout M6.

---

## 17. Test

Focused:

.\mvnw.cmd -Dtest=PayBookingCommandHandlerTest test

Full:

.\mvnw.cmd test

Check:

git diff --check
git status --short

---

## 18. Acceptance criteria

Handler chỉ orchestration qua ports.

Ownership được kiểm tra trước payment.

Chỉ PENDING Booking được pay.

Gateway failure không confirm Booking.

Gateway success tạo SUCCESS Payment.

Gateway success gọi Booking.confirm().

Booking được save sau confirm.

Không tạo Ticket.

Không payment thật.

Focused tests PASS.

Full tests PASS sau khi M7-B infrastructure đã được tích hợp.

git diff --check PASS.

---

## 19. Commit

Stage explicit files.

Commit:

git commit -m "feat(payment): implement pay booking use case"

Không dùng:

git add .

Không push nếu chưa được người tích hợp yêu cầu.
