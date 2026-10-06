# M7-D — Payment HTTP API + Security

## 1. Mục tiêu

Expose M7 PayBooking qua HTTP.

Job này chỉ chịu trách nhiệm Web/Security.

Không chứa business logic.

---

## 2. HTTP contract đã chốt

Endpoint M7:

POST /payments/{bookingId}

Response success:

HTTP 200

Body:

{
  "successful": true,
  "providerReference": "FAKE-PAY-1"
}

Controller trả trực tiếp:

PaymentResult

Không tạo Payment request DTO.

bookingId lấy từ path.

---

## 3. PaymentController

Sửa:

src/main/java/cinema/web/controllers/PaymentController.java

Controller phải active chỉ trong:

@Profile("venue-dev")

Base:

@RequestMapping("/payments")

Endpoint:

@PostMapping("/{bookingId}")

public PaymentResult pay(@PathVariable String bookingId)

Implementation:

return payBookingCommand.handle(
    new PayBookingCommand(bookingId)
);

Controller không validate bookingId.

Application handler chịu trách nhiệm validation.

---

## 4. Controller responsibilities

Controller CHỈ:

HTTP
    |
    v
@PathVariable bookingId
    |
    v
PayBookingCommand
    |
    v
PayBookingCommandHandler
    |
    v
PaymentResult
    |
    v
JSON

Không:

load Booking
check CurrentUser
check Customer
check ownership
check BookingStatus
call PaymentGateway
create Payment
confirm Booking
save repository

---

## 5. Security

Sửa:

src/main/java/cinema/web/security/VenueDevSecurityConfig.java

Tạo matcher:

var payBooking =
    paths.matcher(
        HttpMethod.POST,
        "/payments/{bookingId}"
    );

Endpoint là state-changing POST.

Do đó thêm `payBooking` vào:

csrf.ignoringRequestMatchers(...)

và:

authorizeHttpRequests(...).requestMatchers(...).permitAll()

Lưu ý:

`permitAll` ở venue-dev KHÔNG có nghĩa Application bỏ authentication.

Authentication nghiệp vụ vẫn được:

PayBookingCommandHandler
    |
    v
CurrentUser

kiểm tra.

Đây giống Cart/Booking routes hiện tại.

---

## 6. PaymentControllerTest

Tạo:

src/test/java/cinema/web/controllers/PaymentControllerTest.java

Dùng:

MockMvcBuilders.standaloneSetup(controller)

Mock:

PayBookingCommandHandler

Không boot toàn Spring context chỉ để test controller mapping.

---

## 7. Test happy path

Given:

PaymentResult(
    true,
    "FAKE-PAY-123"
)

Mock:

when(
    handler.handle(
        new PayBookingCommand("booking-1")
    )
).thenReturn(result)

Call:

POST /payments/booking-1

Expect:

200 OK

content type JSON

JSON:

{
  "successful": true,
  "providerReference": "FAKE-PAY-123"
}

Verify:

handler.handle(
    new PayBookingCommand("booking-1")
)

đúng một lần.

---

## 8. Failure result mapping

Controller không tự chuyển:

successful == false

thành HTTP error.

Nếu handler trả:

PaymentResult(
    false,
    ...
)

controller vẫn trả:

HTTP 200

với:

successful: false

Lý do:

PaymentGateway decline/failure là kết quả nghiệp vụ của payment attempt,
không phải lỗi HTTP transport.

Không tự phát minh 402 Payment Required trong M7.

---

## 9. Exception handling

Không catch:

ValidationException
ForbiddenException
NotFoundException
ConflictException
DomainException

trong PaymentController.

Giữ exception handling strategy hiện có của project.

Không tạo Payment-specific exception handler.

---

## 10. Files được phép sửa/tạo

Sửa:

src/main/java/cinema/web/controllers/PaymentController.java

src/main/java/cinema/web/security/VenueDevSecurityConfig.java

Tạo:

src/test/java/cinema/web/controllers/PaymentControllerTest.java

Không sửa UseCaseConfiguration.

Không sửa Application handler.

Không sửa Domain.

Không sửa Infrastructure adapters.

---

## 11. Tests bắt buộc

PaymentControllerTest ít nhất có:

1. POST /payments/booking-1 -> 200

2. bookingId được map chính xác vào PayBookingCommand

3. successful được serialize đúng

4. providerReference được serialize đúng

5. handler chỉ được gọi một lần

6. không gọi handler khác

7. unsuccessful PaymentResult vẫn trả 200 và:
   successful == false

8. direct controller call trả chính PaymentResult object từ handler nếu phù hợp với pattern test hiện tại

---

## 12. Security verification

Sau khi sửa VenueDevSecurityConfig phải kiểm tra:

POST /payments/{bookingId}

được:

CSRF ignored trong venue-dev

và:

permitAll tại web security boundary

Không mở:

/payments/**

bằng wildcard rộng.

Chỉ mở endpoint M7 cụ thể.

---

## 13. Không được làm

Không tạo business validation trong controller.

Không gọi PaymentGateway từ controller.

Không inject repository vào controller.

Không confirm Booking trong controller.

Không tạo Ticket.

Không thêm endpoint:

refund
get payment
list payments
delete payment

Các endpoint đó không thuộc M7.

Không thay đổi BookingController.

Không thay đổi CartController.

---

## 14. Tests

Focused:

.\mvnw.cmd -Dtest=PaymentControllerTest test

Sau khi M7-A/B/C đã merge:

.\mvnw.cmd test

Sau đó:

git diff --check
git status --short

---

## 15. Acceptance criteria

POST /payments/{bookingId} hoạt động.

Controller tạo đúng PayBookingCommand.

Controller trả đúng PaymentResult.

Không business logic trong Web.

Payment route được khai báo chính xác trong venue-dev security.

Không mở wildcard payment routes.

Focused test PASS.

Full suite PASS sau integration.

git diff --check PASS.

Không sửa file ngoài scope.

---

## 16. Commit

Stage explicit files:

git add src/main/java/cinema/web/controllers/PaymentController.java
git add src/main/java/cinema/web/security/VenueDevSecurityConfig.java
git add src/test/java/cinema/web/controllers/PaymentControllerTest.java

Check:

git diff --cached --check
git diff --cached --stat

Commit:

git commit -m "feat(payment): expose M7 payment HTTP API"

Không dùng:

git add .

Không push nếu chưa được người tích hợp yêu cầu.