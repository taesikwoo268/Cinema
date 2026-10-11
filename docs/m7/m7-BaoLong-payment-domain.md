# M7-A — Payment Domain

> Hồ sơ phân công M7 tại thời điểm triển khai, giữ để tra cứu attribution; không phải backlog hiện tại. Fake Payment đã có, Package 1 bổ sung ticket/finalization/retry, Package 2 bổ sung transaction MySQL và Package 3 xác thực Bearer. Xem [tiến độ hiện tại](../cinema-v1-progress.md) trước khi giao việc hoặc dùng hướng dẫn security cũ.

## 1. Mục tiêu

Hoàn thiện Payment domain của M7.

Job này chỉ chịu trách nhiệm:

Payment
PaymentStatus
Payment domain tests

Không implement:

PayBookingCommandHandler
PaymentGateway
PaymentRepository adapter
Controller
Spring configuration
Security

---

## 2. Contract đã chốt

M7 là Fake Payment.

Flow tổng thể:

Booking PENDING
        |
        v
PaymentGateway.pay()
        |
        +---- failure ---> Payment FAILED
        |
        +---- success ---> Payment SUCCESS
                              |
                              v
                       Booking CONFIRMED

Payment domain KHÔNG được biết:

CurrentUser
BookingRepository
PaymentRepository
PaymentGateway
Spring
HTTP
Controller

---

## 3. Payment model

File:

src/main/java/cinema/domain/payment/Payment.java

Giữ constructor public hiện tại:

Payment(
    String id,
    String bookingId,
    Money amount,
    PaymentStatus status,
    String providerReference
)

Fields:

id
bookingId
amount
status
providerReference

Payment tiếp tục extends BaseEntity.

Không đổi constructor signature vì M7-C sẽ dùng chính constructor này.

---

## 4. PaymentStatus

File:

src/main/java/cinema/domain/payment/PaymentStatus.java

Giữ nguyên:

PENDING
SUCCESS
FAILED
REFUNDED

Không thêm status mới.

---

## 5. Invariants bắt buộc

Payment constructor phải reject:

bookingId == null
bookingId blank

amount == null

status == null

SUCCESS nhưng providerReference:
    null
    empty
    blank

REFUNDED nhưng providerReference:
    null
    empty
    blank

PENDING và FAILED được phép providerReference == null.

Không tự tạo ID trong Payment.

ID vẫn do caller truyền vào và BaseEntity kiểm tra.

Money đã tự bảo vệ:

amount != null
currency != null
amount >= 0

Không duplicate validation thuộc Money.

---

## 6. Immutability

Payment của M7 là snapshot của một payment attempt.

Fields tiếp tục final.

Không tạo setter.

Không tạo:

setStatus()
markSuccess()
markFailed()

M7 tạo Payment sau khi PaymentGateway đã trả kết quả.

Ví dụ success:

new Payment(
    paymentId,
    bookingId,
    booking.getTotalAmount(),
    PaymentStatus.SUCCESS,
    providerReference
)

Ví dụ failure:

new Payment(
    paymentId,
    bookingId,
    booking.getTotalAmount(),
    PaymentStatus.FAILED,
    providerReference
)

---

## 7. File được phép sửa

CHỈ:

src/main/java/cinema/domain/payment/Payment.java

Nếu thực sự cần format/comment:

src/main/java/cinema/domain/payment/PaymentStatus.java

Tạo:

src/test/java/cinema/domain/payment/PaymentTest.java

Không sửa file ngoài scope.

---

## 8. Tests bắt buộc

PaymentTest phải ít nhất kiểm tra:

1. valid SUCCESS payment giữ đúng:
   id
   bookingId
   amount
   status
   providerReference

2. valid FAILED payment

3. PENDING cho phép null providerReference

4. reject null bookingId

5. reject empty bookingId

6. reject blank bookingId

7. reject null amount

8. reject null status

9. SUCCESS reject null providerReference

10. SUCCESS reject empty providerReference

11. SUCCESS reject blank providerReference

12. REFUNDED reject null/blank providerReference

13. amount object được giữ nguyên chính xác, không tự rounding

Không test logic của PaymentGateway tại đây.

---

## 9. Không được làm

Không sửa:

Booking.java
BookingStatus.java
PayBookingCommand.java
PayBookingCommandHandler.java
PaymentGateway.java
PaymentRepository.java
UseCaseConfiguration.java
PaymentController.java
VenueDevSecurityConfig.java

Không thêm Spring annotation vào Domain.

Không thêm JPA annotation.

Không thêm Lombok.

Không tạo dependency từ Domain ra Application/Infrastructure/Web.

---

## 10. Lệnh test

Chạy focused test:

.\mvnw.cmd -Dtest=PaymentTest test

Sau đó:

.\mvnw.cmd test

Cuối cùng:

git diff --check
git status --short

Tất cả phải PASS.

---

## 11. Acceptance criteria

Job hoàn thành khi:

Payment bảo vệ đầy đủ invariants trên.

Payment vẫn immutable.

Constructor signature không đổi.

PaymentStatus không đổi contract.

Không có Spring/JPA trong Domain.

Focused tests PASS.

Full mvn test PASS.

git diff --check PASS.

Không có file ngoài scope bị thay đổi.

---

## 12. Commit

Chỉ stage file thuộc job:

git add src/main/java/cinema/domain/payment/Payment.java
git add src/main/java/cinema/domain/payment/PaymentStatus.java
git add src/test/java/cinema/domain/payment/PaymentTest.java

Kiểm tra:

git diff --cached --check
git diff --cached --stat

Commit:

git commit -m "feat(payment): implement payment domain rules"

Không push nếu chưa được người tích hợp yêu cầu.
