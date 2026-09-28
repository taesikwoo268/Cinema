# M7-B — Payment Infrastructure

## 1. Mục tiêu

Implement Infrastructure adapter cho M7:

FakePaymentGateway
InMemoryPaymentRepository
PaymentConfiguration

Job này KHÔNG implement business use case.

---

## 2. Contract cố định

Application đã có port:

PaymentGateway

với:

PaymentResult pay(String bookingId, Money amount)

PaymentResult refund(
    String paymentReference,
    Money amount
)

Không thay đổi interface.

Application đã có:

PaymentRepository

với:

Optional<Payment> findById(String id)

List<Payment> findByBookingId(String bookingId)

void save(Payment payment)

Không thay đổi interface.

---

## 3. FakePaymentGateway

Tạo:

src/main/java/cinema/infrastructure/payments/FakePaymentGateway.java

Fake gateway của V1 luôn thành công.

pay():

FakePaymentGateway
        |
        v
validate basic arguments
        |
        v
generate fake provider reference
        |
        v
PaymentResult(
    true,
    reference
)

Reference phải:

không null
không blank
unique giữa các lần pay trong cùng gateway instance

Format cố định:

FAKE-PAY-1
FAKE-PAY-2
FAKE-PAY-3
...

Dùng AtomicLong để sequence thread-safe.

Không dùng random nếu không cần thiết.

Ví dụ:

private final AtomicLong sequence = new AtomicLong();

"FAKE-PAY-" + sequence.incrementAndGet()

---

## 4. Fake refund

PaymentGateway yêu cầu refund(), vì vậy FakePaymentGateway phải implement.

V1 refund cũng luôn success.

Reference:

FAKE-REFUND-1
FAKE-REFUND-2
...

Có thể dùng sequence riêng cho refund.

refund() không thay đổi Payment domain.

Nó chỉ trả PaymentResult.

---

## 5. Basic argument protection

FakePaymentGateway.pay() reject:

bookingId null
bookingId blank
amount null

refund() reject:

paymentReference null
paymentReference blank
amount null

Có thể dùng:

IllegalArgumentException

hoặc Objects.requireNonNull cho null.

Không dùng DomainException trong Infrastructure validation này.

---

## 6. InMemoryPaymentRepository

Tạo:

src/main/java/cinema/infrastructure/persistence/inmemory/InMemoryPaymentRepository.java

Implement:

PaymentRepository

Internal storage:

LinkedHashMap<String, Payment>

Lý do dùng LinkedHashMap:

giữ insertion order deterministic giống Booking repository.

Methods:

findById(id)

findByBookingId(bookingId)

save(payment)

Các public repository operation phải synchronized.

---

## 7. Repository behavior

save(payment):

reject null payment

sau đó:

payments.put(payment.getId(), payment)

Điều này cho phép:

insert
update cùng ID

findByBookingId():

filter:

payment.getBookingId().equals(bookingId)

và giữ insertion order.

Không throw nếu không tìm thấy.

Trả:

List.of()/empty list tương ứng.

---

## 8. PaymentConfiguration

Tạo:

src/main/java/cinema/infrastructure/config/PaymentConfiguration.java

Configuration chỉ active:

@Profile("venue-dev")

Expose:

@Bean
PaymentRepository paymentRepository()

trả:

new InMemoryPaymentRepository()

và:

@Bean
PaymentGateway paymentGateway()

trả:

new FakePaymentGateway()

Không sửa UseCaseConfiguration trong job này.

---

## 9. Files được phép tạo

src/main/java/cinema/infrastructure/payments/FakePaymentGateway.java

src/main/java/cinema/infrastructure/persistence/inmemory/InMemoryPaymentRepository.java

src/main/java/cinema/infrastructure/config/PaymentConfiguration.java

src/test/java/cinema/infrastructure/payments/FakePaymentGatewayTest.java

src/test/java/cinema/infrastructure/persistence/inmemory/InMemoryPaymentRepositoryTest.java

Không sửa port:

PaymentGateway.java
PaymentRepository.java

---

## 10. FakePaymentGatewayTest

Bắt buộc kiểm tra:

1. pay returns successful == true

2. providerReference không null/blank

3. successive pay calls tạo reference khác nhau

4. reference có prefix FAKE-PAY-

5. refund returns successful == true

6. refund reference có prefix FAKE-REFUND-

7. invalid bookingId bị reject

8. null amount bị reject

9. invalid refund reference bị reject

Không test Booking ở đây.

---

## 11. InMemoryPaymentRepositoryTest

Bắt buộc kiểm tra:

1. new repository không tìm thấy ID

2. save -> findById trả đúng Payment

3. save null bị reject

4. findByBookingId chỉ trả payment của booking đó

5. nhiều payment cùng booking được trả đủ

6. insertion order được giữ

7. payment của booking khác không bị lẫn

8. save cùng ID update stored value

9. returned list không cho caller phá internal collection

---

## 12. Không được làm

Không sửa:

Payment.java
Booking.java
PayBookingCommandHandler.java
UseCaseConfiguration.java
PaymentController.java
VenueDevSecurityConfig.java

Không gọi Booking.confirm().

Không implement ownership.

Không biết CurrentUser.

Không thêm database/JPA.

Không thêm payment provider thật.

Không VNPay.

Không Stripe.

Không network call.

---

## 13. Tests

Focused:

.\mvnw.cmd -Dtest=FakePaymentGatewayTest,InMemoryPaymentRepositoryTest test

Full:

.\mvnw.cmd test

Check:

git diff --check
git status --short

---

## 14. Acceptance criteria

Fake pay luôn success.

Fake refund luôn success.

Provider reference deterministic format và unique trong instance.

Repository implement đúng PaymentRepository.

Repository giữ insertion order.

PaymentConfiguration chỉ active venue-dev.

Không thay đổi Application ports.

Không business logic trong Infrastructure.

Focused tests PASS.

Full tests PASS.

git diff --check PASS.

---

## 15. Commit

Stage explicit files, KHÔNG dùng git add .

Commit:

git commit -m "feat(payment): add fake payment infrastructure"

Không push nếu chưa được người tích hợp yêu cầu.