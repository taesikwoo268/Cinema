# M4 Assignment --- Nguyễn Trung Nghĩa

## 1. Role

Team Lead + Contract Owner + Integrator.

Nghĩa không nhận implementation slice của worker trong giai đoạn
parallel. Nhiệm vụ chính là làm cho ba worker có thể bắt đầu cùng lúc và
chỉ phụ thuộc frozen contracts.

## 2. Branch

`feature/nghia/moc-8`

M3 base: `97e57f5`.

M4 worker base: `M4_BASE = TBD`.

## 3. PHASE A --- Contract Freeze

Trước khi tạo branch cho worker, Nghĩa thực hiện một M4.0 commit tối
thiểu.

### Contract changes cần thực hiện

1.  `RegisterCommand`:

``` java
RegisterCommand(String email, String password, String displayName)
```

2.  `Customer` public API giữ:

``` java
Customer(String id, String userId, String displayName)
```

Thêm invariants: - userId nonblank; - displayName nonblank và trim.

3.  Freeze:

-   `IdentityService`
-   `CustomerRepository`
-   `CurrentUser`
-   `AuthenticationResult`
-   `AuthenticatedUser`
-   `CurrentUserView`
-   `LoginCommand`
-   `GetCurrentUserQuery`
-   Customer public API.

4.  Chốt Register dùng `Supplier<String>` để sinh Customer ID.

5.  Chốt M4 runtime Customer persistence = in-memory; JPA Customer
    deferred.

### Sau contract changes

Chạy targeted tests liên quan và full regression phù hợp. Sau khi PASS,
commit baseline:

Suggested subject: `docs(identity): freeze M4 contracts and team plan`

Commit hash mới chính là `M4_BASE`.

Sau đó mới cho Long/Anh/Khanh tạo branch từ hash đó.

## 4. PHASE B --- Parallel Review

Trong khi worker làm:

-   không sửa worker-owned files;
-   review câu hỏi về frozen contract;
-   nếu contract thực sự thiếu, dừng affected slice và xử lý centrally;
-   không cho worker tự sửa shared interface.

Worker ownership:

-   Long: Customer + in-memory repository.
-   Anh: Register.
-   Khanh: Login + CurrentUser + simple Identity adapter.

## 5. PHASE C --- Merge Worker Slices

Chỉ merge sau handover đầy đủ.

Mỗi handover phải có:

-   branch;
-   commit hash;
-   changed files;
-   tests run;
-   PASS/FAIL;
-   limitations;
-   xác nhận không sửa forbidden files.

Review bằng file ownership trước khi merge.

## 6. PHASE D --- Integration

Nghĩa sở hữu:

``` text
src/main/java/cinema/infrastructure/config/UseCaseConfiguration.java
src/main/java/cinema/web/controllers/IdentityController.java
src/main/java/cinema/web/security/SecurityConfig.java
src/main/java/cinema/web/security/VenueDevSecurityConfig.java
```

Và được tạo Identity HTTP DTO/security integration files nếu thật sự
cần.

Integration goals:

-   wire Register/Login/GetCurrentUser handlers;
-   wire CustomerRepository in-memory adapter;
-   wire IdentityService/CurrentUser;
-   expose minimal Identity HTTP API;
-   map authenticated principal/userId consistently;
-   giữ Web -\> Application boundary.

Không đưa business rules vào controller/security config.

## 7. PHASE E --- Acceptance

Nghĩa sở hữu cross-slice tests.

Minimum scenarios:

-   valid register -\> Customer liên kết userId;
-   duplicate email -\> 409;
-   invalid register -\> 400;
-   valid login -\> accessToken + expiresAt;
-   invalid credentials -\> appropriate application/web error;
-   authenticated current-user request -\> đúng userId;
-   unauthenticated current-user request -\> forbidden/unauthorized theo
    security contract;
-   unsupported routes/methods vẫn bị deny;
-   không làm hỏng Venue/Movie/Screening.

## 8. PHASE F --- Documentation + Final Regression

Sau khi M4 chạy ổn:

-   cập nhật README;
-   `docs/cinema-v1-progress.md`;
-   `docs/team-work-allocation.md`;
-   architecture tests;
-   full Maven suite;
-   `git diff --check`.

## 9. Files workers must not share

Nghĩa giữ integration-only ownership của shared
configuration/Web/Security/docs. Không yêu cầu worker sửa các file này
để "làm cho test chạy".

## 10. Deferred

-   JPA Customer persistence;
-   production BCrypt;
-   production JWT/refresh tokens;
-   OAuth;
-   email verification/password reset;
-   advanced RBAC.

## 11. Definition of Done

M4 chỉ hoàn thành khi các slice đã merge, integration + acceptance PASS
và full regression không phá M1--M3.
