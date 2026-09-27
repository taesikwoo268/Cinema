# M4 Assignment --- Duy Khanh

## 1. Role

Login + Current Identity vertical slice owner.

Bạn chịu trách nhiệm implementation + tests của:

-   Login application use case;
-   GetCurrentUser application use case;
-   simple Identity infrastructure adapter.

## 2. Branch

Chỉ tạo sau `M4_BASE`:

`feature/duy-khanh/moc-8-identity`

Branch trực tiếp từ `M4_BASE`.

## 3. Objective

Cung cấp Identity implementation đơn giản đủ cho M4 mà không phụ thuộc
Customer implementation của Bảo Long và không implement Register
orchestration của Duy Anh.

## 4. Files You May Read

``` text
src/main/java/cinema/application/identity/commands/LoginCommand.java
src/main/java/cinema/application/identity/queries/GetCurrentUserQuery.java
src/main/java/cinema/application/common/interfaces/IdentityService.java
src/main/java/cinema/application/common/interfaces/CurrentUser.java
src/main/java/cinema/application/common/interfaces/TokenVerifier.java
src/main/java/cinema/application/common/models/AuthenticationResult.java
src/main/java/cinema/application/common/models/AuthenticatedUser.java
src/main/java/cinema/application/common/models/CurrentUserView.java
src/main/java/cinema/infrastructure/identity/ApplicationUser.java
src/main/java/cinema/infrastructure/identity/IdentityServiceImpl.java
src/main/java/cinema/infrastructure/identity/JwtService.java
src/main/java/cinema/web/security/SpringCurrentUser.java
docs/m4/m4-overview.md
```

## 5. Files You Own / May Modify

``` text
src/main/java/cinema/application/identity/commands/LoginCommandHandler.java
src/main/java/cinema/application/identity/queries/GetCurrentUserQueryHandler.java
src/main/java/cinema/infrastructure/identity/ApplicationUser.java
src/main/java/cinema/infrastructure/identity/IdentityServiceImpl.java
src/main/java/cinema/web/security/SpringCurrentUser.java
```

`JwtService.java` không tự implement production JWT. Nếu integration cần
TokenVerifier behavior ngoài ownership này, báo Nghĩa.

## 6. Files You May Create

Tests tương ứng, ví dụ:

``` text
src/test/java/cinema/application/identity/commands/LoginCommandHandlerTest.java
src/test/java/cinema/application/identity/queries/GetCurrentUserQueryHandlerTest.java
src/test/java/cinema/infrastructure/identity/IdentityServiceImplTest.java
src/test/java/cinema/web/security/SpringCurrentUserTest.java
```

Có thể tạo private/simple infrastructure support bên trong
`cinema.infrastructure.identity` nếu cần cho in-memory account/token
behavior, nhưng không tạo cross-layer business service.

## 7. LoginCommandHandler requirements

Dependency:

``` text
IdentityService
```

Flow:

1.  validate command trước side effects;
2.  email/password nonblank;
3.  gọi `IdentityService.login`;
4.  trả nguyên `AuthenticationResult`.

Unit test phải mock/fake interface, không phụ thuộc concrete adapter.

## 8. GetCurrentUserQueryHandler requirements

Dependency:

``` text
CurrentUser
```

Flow:

-   null query nếu convention yêu cầu -\> validation;
-   đọc `isAuthenticated`;
-   nếu authenticated, lấy userId;
-   trả `CurrentUserView(userId,true)`;
-   unauthenticated behavior phải theo frozen Application/security
    contract; không tự đưa Spring API vào handler.

## 9. IdentityServiceImpl requirements

Simple M4 adapter:

-   account email unique;
-   `register(email,password)` trả stable generated userId theo
    dependency/design được Nghĩa wire;
-   lưu credential chỉ trong Infrastructure;
-   `login` xác minh credentials;
-   thành công trả `AuthenticationResult(accessToken, expiresAt)`;
-   invalid credentials dùng application exception phù hợp;
-   không chứa CustomerRepository/Customer logic.

Không quảng bá implementation M4 là production security.

## 10. Password/token scope

Required:

-   simple credential checking;
-   simple access token + expiry đủ cho M4 tests/integration.

Deferred:

-   BCrypt production hardening;
-   production JWT signing;
-   refresh tokens;
-   OAuth2;
-   password reset;
-   email verification;
-   advanced RBAC.

`JwtService` skeleton tồn tại không có nghĩa bạn phải xây production
JWT.

## 11. SpringCurrentUser

Chỉ adapter Application `CurrentUser` sang Spring Security context.

-   authenticated principal name phải map thành userId;
-   anonymous -\> unauthenticated;
-   unauthenticated `getUserId()` -\> application `ForbiddenException`
    theo skeleton hiện tại.

Không đặt Customer lookup trong `SpringCurrentUser`.

## 12. Tests You Must Write

Login handler:

-   valid;
-   null/blank input;
-   validation before side effects;
-   AuthenticationResult preserved;
-   invalid credential propagation.

GetCurrentUser:

-   authenticated user;
-   unauthenticated behavior;
-   correct userId mapping.

IdentityServiceImpl:

-   register new account;
-   duplicate email conflict;
-   login valid;
-   login invalid;
-   token nonblank;
-   expiry present/future according to implementation contract;
-   credentials not leaked through `ApplicationUser.toString()`.

SpringCurrentUser:

-   authenticated principal;
-   anonymous principal;
-   missing authentication.

## 13. Files You MUST NOT Modify

``` text
src/main/java/cinema/application/common/interfaces/**
src/main/java/cinema/application/common/models/**
src/main/java/cinema/application/identity/commands/LoginCommand.java
src/main/java/cinema/application/identity/queries/GetCurrentUserQuery.java

src/main/java/cinema/domain/customer/**
src/main/java/cinema/application/identity/commands/RegisterCommandHandler.java
src/main/java/cinema/infrastructure/persistence/**

src/main/java/cinema/infrastructure/config/UseCaseConfiguration.java
src/main/java/cinema/web/controllers/IdentityController.java
src/main/java/cinema/web/security/SecurityConfig.java
src/main/java/cinema/web/security/VenueDevSecurityConfig.java

README.md
docs/cinema-v1-progress.md
docs/team-work-allocation.md
```

## 14. Dependencies

Implementation from Bảo Long needed: NO.

Implementation from Duy Anh needed: NO.

Login handler tests use fake/mock `IdentityService`; GetCurrentUser
tests use fake/mock `CurrentUser`.

## 15. Tests You Do NOT Own

Register tests, Customer tests, cross-slice HTTP/security acceptance.

## 16. Handover

Gửi Nghĩa branch, commit, exact changed files, tests, PASS/FAIL,
limitations và xác nhận forbidden files không đổi.

## 17. If Contract Is Insufficient

Dừng phần bị ảnh hưởng và báo Nghĩa. Không tự sửa frozen contract.
