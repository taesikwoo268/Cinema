# M4 Identity + Customer --- Team Work Plan

**Trạng thái: M4 COMPLETED** trong phạm vi Identity + Customer đơn giản tại `bf6383e`.
Kế hoạch và phân công ban đầu được giữ bên dưới; kết quả cuối và giới hạn authentication xem cuối tài liệu.

## 1. Goal

M4 hoàn thiện Identity + Customer ở mức đơn giản theo roadmap hiện tại:

-   đăng ký tài khoản;
-   đăng nhập;
-   tạo và liên kết `Customer` với `ApplicationUser` qua `userId`;
-   Application đọc được người dùng hiện tại qua `CurrentUser`;
-   mỗi account khách hàng tương ứng tối đa một `Customer`;
-   ba worker có thể bắt đầu song song từ cùng một M4 baseline mà không
    phải chờ implementation của nhau.

Nguyên tắc bắt buộc: **implementation owner = unit/adapter test owner**.
Không giao một người viết code rồi một người khác chờ code đó để viết
test.

## 2. Source facts đã kiểm tra tại baseline M3 (lịch sử)

Baseline M3: `97e57f5`.

Các contract/skeleton hiện có:

-   `Customer(String id, String userId, String displayName)`.
-   `CustomerRepository`: `findById`, `findByUserId`, `save`.
-   `IdentityService`: `register(email,password) -> String userId`;
    `login(email,password) -> AuthenticationResult`.
-   `CurrentUser`: `getUserId()`, `isAuthenticated()`.
-   `AuthenticationResult(String accessToken, Instant expiresAt)`.
-   `AuthenticatedUser(String userId)`.
-   `CurrentUserView(String userId, boolean authenticated)`.
-   `RegisterCommand(String email, String password)` hiện chưa có
    `displayName`.
-   `LoginCommand(String email, String password)`.
-   `ApplicationUser(String id, String email, String passwordHash)`.
-   `IdentityServiceImpl`, `JwtService`, Identity handlers, Customer JPA
    adapter đều đang là skeleton.
-   M2/M3 dùng `Supplier<String>` cho ID generation.
-   M2/M3 development adapters dùng in-memory; JPA skeleton tồn tại
    nhưng không phải bằng chứng rằng M4 bắt buộc phải hoàn thiện JPA.

## 3. M4 contract decisions --- do Nghĩa chốt trước parallel work

### 3.1 Customer public API

M4 freeze API:

``` java
Customer(String id, String userId, String displayName)
String getId()
String getUserId()
String getDisplayName()
```

Domain invariants:

-   `id`: non-null/non-blank, đã được `BaseEntity` bảo vệ.
-   `userId`: non-null/non-blank.
-   `displayName`: non-null/non-blank; lưu giá trị đã `trim()`.

Bảo Long sở hữu implementation `Customer`, nhưng không được đổi public
API trên khi Duy Anh đang làm Register.

### 3.2 RegisterCommand

`RegisterCommand` cần đủ dữ liệu để tạo Customer. M4 thay contract
thành:

``` java
RegisterCommand(String email, String password, String displayName)
```

Không được tự suy `displayName` từ email và không được để worker tự chọn
quy tắc khác.

### 3.3 Customer ID

`RegisterCommandHandler` nhận `Supplier<String> idGenerator`, cùng
convention với Movie/Screening/Venue.

### 3.4 Duplicate rules

-   Email account phải unique theo Identity adapter.
-   Register email đã tồn tại -\> `ConflictException`.
-   `Customer.userId` phải unique.
-   Trước khi lưu Customer, Register kiểm tra
    `CustomerRepository.findByUserId(userId)`.
-   Nếu đã tồn tại -\> `ConflictException`.

### 3.5 Validation

Application validation tối thiểu:

-   command không null;
-   email non-null/non-blank;
-   password non-null/non-blank;
-   displayName non-null/non-blank.

Không tự thêm password-strength, regex email phức tạp hoặc business
policy ngoài M4.

### 3.6 Password

Password chỉ thuộc Identity Infrastructure.

-   Không lưu password trong `Customer`.
-   `ApplicationUser.passwordHash` là infrastructure concern.
-   Production BCrypt/password policy nâng cao: **deferred**.
-   M4 adapter đơn giản được phép dùng deterministic/simple password
    representation phục vụ milestone, nhưng không được mô tả nó là
    production security.

### 3.7 Token

`AuthenticationResult` hiện đã freeze `accessToken + expiresAt`, vì vậy
login phải trả hai giá trị này.

M4 chỉ yêu cầu **simple authentication token** đủ cho demo/test.
Production JWT/refresh-token architecture là deferred. Không bắt worker
tự mở rộng `AuthenticationResult`.

### 3.8 Persistence

M4 chọn **InMemoryCustomerRepository** cho runnable development slice,
nhất quán với cách M2/M3 đưa feature vào `venue-dev`.

Các JPA Customer skeleton:

-   `CustomerJpaEntity`
-   `CustomerPersistenceMapper`
-   `SpringDataCustomerRepository`
-   `JpaCustomerRepositoryAdapter`

được đánh dấu **DEFERRED** trong M4, không giao Bảo Long hoàn thiện.

## 4. Frozen contracts

Sau M4.0 baseline, worker chỉ được đọc/import/mock các contract sau:

``` text
src/main/java/cinema/application/common/interfaces/IdentityService.java
src/main/java/cinema/application/common/interfaces/CustomerRepository.java
src/main/java/cinema/application/common/interfaces/CurrentUser.java
src/main/java/cinema/application/common/models/AuthenticationResult.java
src/main/java/cinema/application/common/models/AuthenticatedUser.java
src/main/java/cinema/application/common/models/CurrentUserView.java
src/main/java/cinema/application/identity/commands/RegisterCommand.java
src/main/java/cinema/application/identity/commands/LoginCommand.java
src/main/java/cinema/application/identity/queries/GetCurrentUserQuery.java
```

Cross-worker Domain API cũng freeze:

``` text
src/main/java/cinema/domain/customer/Customer.java
```

Worker phát hiện contract thiếu -\> dừng phần bị ảnh hưởng và báo Nghĩa.
Không tự sửa shared contract.

## 5. Branch strategy

`M3_BASE = 97e57f5`.

`M4_BASE = 3a44962`: docs(identity): freeze M4 contracts and team plan.

**Không tạo worker branch trước M4_BASE.**

  ------------------------------------------------------------------------------------
  Thành viên              Branch sau M4_BASE                   Ownership
  ----------------------- ------------------------------------ -----------------------
  Nghĩa                   `feature/nghia/moc-8`                Lead, contract,
                                                               integration,
                                                               Web/Security,
                                                               acceptance

  Bảo Long                `feature/bao-long/moc-8-customer`    Customer Domain +
                                                               in-memory repository +
                                                               tests

  Duy Anh                 `feature/duy-anh/moc-8-register`     Register use case +
                                                               tests

  Duy Khanh               `feature/duy-khanh/moc-8-identity`   Login + CurrentUser +
                                                               simple Identity
                                                               adapter + tests
  ------------------------------------------------------------------------------------

Tất cả worker branch phải tách trực tiếp từ cùng `M4_BASE`.

## 6. Parallel dependency diagram

``` text
                         M4_BASE
                            |
          +-----------------+-----------------+
          |                 |                 |
          v                 v                 v
      Bảo Long           Duy Anh          Duy Khanh
      Customer           Register         Identity
      + in-memory        + tests          + Login
      + tests                              + CurrentUser
          |                 |              + tests
          |                 |                 |
          +-----------------+-----------------+
                            |
                            v
                          Nghĩa
                        Integration
                   Web + Security + Acceptance
```

Duy Anh compile against frozen `Customer` API, nhưng unit test Register
phải fake/mock `IdentityService` và `CustomerRepository`; không dùng
adapter của Long/Khanh.

## 7. Ownership matrix

  -----------------------------------------------------------------------------------------------------------------------
  File/package                                                            Owner                   Permission
  ----------------------------------------------------------------------- ----------------------- -----------------------
  Frozen interfaces/models/commands                                       Nghĩa                   READ-ONLY for workers

  `domain/customer/Customer.java`                                         Bảo Long                MODIFY, public API
                                                                                                  frozen

  `infrastructure/persistence/inmemory/InMemoryCustomerRepository.java`   Bảo Long                CREATE

  Register handler + Register tests                                       Duy Anh                 MODIFY/CREATE

  Login/GetCurrentUser handlers                                           Duy Khanh               MODIFY

  `IdentityServiceImpl`, `ApplicationUser`                                Duy Khanh               MODIFY

  Identity-specific unit tests                                            Duy Khanh               CREATE

  `UseCaseConfiguration.java`                                             Nghĩa                   INTEGRATOR-ONLY

  `IdentityController.java`                                               Nghĩa                   INTEGRATOR-ONLY

  Security configuration/filter/wiring                                    Nghĩa                   INTEGRATOR-ONLY

  M4 HTTP acceptance tests                                                Nghĩa                   INTEGRATOR-ONLY

  Customer JPA skeletons                                                  Nobody in M4            DEFERRED

  README/progress/allocation                                              Nghĩa                   FINAL INTEGRATION
  -----------------------------------------------------------------------------------------------------------------------

## 8. Shared hotspots

Workers không sửa:

-   `UseCaseConfiguration.java`
-   `IdentityController.java`
-   `SecurityConfig.java`
-   `VenueDevSecurityConfig.java`
-   shared Web exception mapping trừ khi Nghĩa quyết định cần thay đổi;
-   README/progress/team allocation;
-   frozen contracts.

## 9. Test ownership

-   Bảo Long: Customer domain + in-memory repository tests.
-   Duy Anh: Register handler tests.
-   Duy Khanh: Login/GetCurrentUser/Identity adapter tests.
-   Nghĩa: HTTP acceptance, security integration, architecture/full
    regression.

Không worker nào viết unit test cho implementation của worker khác.

## 10. Integration order

Integration không tạo dependency phát triển. Khi worker hoàn thành,
Nghĩa review/merge từng slice vào `feature/nghia/moc-8`, sau đó mới sửa
shared wiring/Web/Security.

Suggested merge order:

1.  Customer slice.
2.  Register slice.
3.  Identity slice.
4.  Integration by Nghĩa.

Thứ tự merge không có nghĩa worker 2/3 phải chờ worker 1.

## 11. Deferred scope

-   Customer JPA persistence.
-   production BCrypt/password hardening.
-   production JWT signing/refresh tokens.
-   OAuth2.
-   email verification/password reset.
-   advanced RBAC.
-   Cart/SeatHold/Booking/Checkout/Payment.

## 12. Definition of Done --- Worker

-   Chỉ thay đổi owned files.
-   Tự viết tests cho implementation của mình.
-   Không sửa frozen contract.
-   Targeted tests PASS.
-   `git diff --check` PASS.
-   Handover gồm branch, commit, changed files, tests, result,
    limitations.
-   Không merge vào branch của Nghĩa.

## 13. Definition of Done --- M4

-   Register hoạt động.
-   Login đơn giản hoạt động.
-   Customer được liên kết đúng `userId`.
-   Application xác định đúng current user/customer flow theo contract.
-   HTTP/security integration hoạt động trong profile được Nghĩa chốt.
-   Architecture tests PASS.
-   Full Maven regression PASS.
-   M4 docs/progress được cập nhật.

## Final M4 result

M4 **COMPLETED** trong phạm vi Identity + Customer đơn giản tại `bf6383e`,
nhánh `feature/nghia/moc-8`. Các mục planning phía trên và bốn assignment files
là lịch sử; kết quả dưới đây phản ánh implementation cuối.

Completed:

- Customer domain và in-memory Customer repository.
- Register tạo identity, liên kết/lưu Customer và trả customer ID.
- Login trả accessToken và expiresAt.
- Get Current User qua CurrentUser port; unauthenticated trả `(null, false)`.
- IdentityService implementation và SpringCurrentUser adapter.
- venue-dev wiring và Identity HTTP API: register, login, me.
- Identity HTTP acceptance; focused tests và full regression được chủ dự án xác nhận BUILD SUCCESS.

Security boundary: Login đã phát token; Bearer -> verification -> Authentication
-> SecurityContext chưa hoàn chỉnh. Không coi behavior Bearer hiện tại là yêu cầu vĩnh viễn,
và không công bố production authentication đã hoàn thành.

Deferred:

- Bearer/JWT verification, production TokenVerifier và authentication filter hoàn chỉnh.
- Production authentication/JWT signing/BCrypt, refresh tokens, OAuth/external provider.
- Password reset, email verification, advanced RBAC.
- Production Identity persistence và production Customer/JPA persistence.
- Cart/SeatHold/Booking/Checkout/Payment theo roadmap.

### Integration history và attribution

Frozen base: `3a44962`; các worker làm slice riêng, Nghĩa tích hợp vào nhánh M4.

| Thành viên | Đóng góp và bằng chứng |
| --- | --- |
| Nguyễn Trung Nghĩa | Freeze contracts/base (bao gồm Customer invariants), tích hợp các slice, shared wiring và HTTP/security acceptance; `378cfe6`, `bf6383e` |
| Bảo Long | Customer slice owner; Customer tests và in-memory repository/tests qua `f1c9bc7`, `2a222bd`, `5650921`; tích hợp tại `c63a020` |
| Duy Anh | Register handler và unit tests; tích hợp tại `705efe2` |
| Duy Khanh | Login, GetCurrentUser, IdentityService/ApplicationUser, SpringCurrentUser và tests; tích hợp tại `115a307` |

Thứ tự tích hợp thực tế: Duy Khanh -> Duy Anh -> Bảo Long -> Nghĩa integration/HTTP.
Đây là kết quả trên nhánh tích hợp, không phải khẳng định đã merge vào main.
Kế hoạch acceptance gốc có dự kiến unauthenticated bị deny; thực tế `/identity/me`
trả 200 với userId null/authenticated false. Authenticated principal được unit-test;
Bearer HTTP authentication chưa triển khai.

See:

- [M4 implementation](m4-implementation.md)
- [M4 acceptance](m4-acceptance.md)
