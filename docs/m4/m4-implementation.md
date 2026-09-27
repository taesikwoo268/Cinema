# M4 — Identity + Customer Implementation

## Scope

M4 **COMPLETED** cho Identity + Customer đơn giản tại `bf6383e` trên `feature/nghia/moc-8`.
Phạm vi gồm Customer Domain, in-memory Customer repository, Register, Login,
Get Current User, IdentityService, SpringCurrentUser, venue-dev wiring và Identity HTTP API.
M4 không cung cấp production authentication hoặc production persistence.

Căn cứ: implementation và tests tại HEAD trên; [acceptance](m4-acceptance.md)
ghi riêng bằng chứng chạy test/HTTP do chủ dự án xác nhận.
[Overview](m4-overview.md) giữ lịch sử contract và phân công.

## Domain

`Customer` kế thừa `BaseEntity`, giữ `id`, `userId`, `displayName`.
ID được BaseEntity bảo vệ; userId và displayName phải non-null/non-blank,
vi phạm ném `DomainException`. DisplayName được trim; id/userId giữ nguyên giá trị.
Customer liên kết hồ sơ với identity account qua userId, không chứa password,
credential, token hay logic Spring Security. Customer ID và identity user ID là hai identity riêng.

## Application

### Register

```text
HTTP -> RegisterCommand(email, password, displayName)
     -> RegisterCommandHandler
     -> IdentityService.register -> userId
     -> CustomerRepository.findByUserId
     -> generate customerId -> Customer -> CustomerRepository.save
     -> customer ID
```

Handler validate command và ba field nonblank trước side effects. Sau khi đăng ký
identity, handler kiểm tra userId trả về hợp lệ và chưa có Customer tương ứng,
sinh ID bằng `Supplier<String>`, tạo/lưu Customer rồi trả **customer ID**.
Không tự suy displayName từ email; không thêm email regex/password-strength policy.

- Email đã tồn tại: IdentityService ném `ConflictException`, được truyền lên.
- Customer đã có cho userId: handler ném `ConflictException`.
- Conflict khi repository save được truyền lên.
- UserId trả về không hợp lệ: `ValidationException`.
- `DomainException` khi tạo Customer được dịch thành `ValidationException`.

Luồng hiện tại không có transaction/rollback liên adapter: identity đã được tạo
trước khi Customer được lưu. Không coi hai thao tác này là atomic persistence.

### Login

```text
LoginCommand(email, password) -> LoginCommandHandler
    -> IdentityService.login -> AuthenticationResult
```

Handler validate command/email/password trước lời gọi port và trả nguyên result:
`AuthenticationResult(String accessToken, Instant expiresAt)`.
Sai credentials từ adapter được truyền lên dưới dạng `ForbiddenException`.

### Get Current User

```text
GetCurrentUserQuery -> GetCurrentUserQueryHandler
    -> CurrentUser -> CurrentUserView(String userId, boolean authenticated)
```

Null query bị từ chối bằng `ValidationException`. Khi `isAuthenticated()` false,
handler trả `(null, false)` và không gọi `getUserId()`; khi true, trả userId từ port.
Đây là identity user ID, không phải Customer lookup hoặc customer ID.

## Infrastructure

`IdentityServiceImpl` giữ accounts trong `ConcurrentHashMap` theo email chính xác,
không trim/lowercase email. `putIfAbsent` bảo vệ duplicate email; userId mặc định là UUID.
Credential lưu dưới dạng SHA-256 hash, so sánh bằng `MessageDigest.isEqual`.
Đây là adapter development, không phải password hardening cho production.
`ApplicationUser.toString()` chỉ xuất id, không xuất password hash.

Login đúng trả token UUID ngẫu nhiên và expiry bằng Clock hiện tại cộng một giờ.
Token không phải JWT; adapter không lưu ánh xạ token với account và chưa xác minh
hoặc thực thi expiry trên request. Account in-memory mất khi restart.

`InMemoryCustomerRepository` dùng HashMap, các operation synchronized:

- `findById` và `findByUserId` trả Optional; missing trả empty.
- `findByUserId` so khớp chính xác userId.
- `save` từ chối duplicate customer ID hoặc userId bằng `ConflictException`;
  không phải upsert và không yêu cầu displayName unique.

`CustomerConfiguration` đăng ký repository singleton trong **venue-dev**.
`UseCaseConfiguration` wire Register với IdentityService, CustomerRepository và UUID supplier;
bean Register chỉ active trong venue-dev. IdentityService, Login và GetCurrentUser beans
không có giới hạn profile. `IdentitySecurityConfiguration` ở Web cung cấp
CurrentUser bằng SpringCurrentUser, cũng không giới hạn profile.
Không có production Customer persistence được nối vào luồng này.

## Web

`IdentityController` chỉ active trong venue-dev; chuyển request thành command/query,
gọi handler và chuyển kết quả thành HTTP response.

| Method/path | Request | Success |
| --- | --- | --- |
| POST /identity/register | email, password, displayName | 201, `{"id":"<customerId>"}` |
| POST /identity/login | email, password | 200, `{"accessToken":"...","expiresAt":"<Instant>"}` |
| GET /identity/me | Không body | 200, `{"userId":null,"authenticated":false}` nếu chưa xác thực |

Khi CurrentUser đã authenticated, `/identity/me` biểu diễn userId và `authenticated:true`;
M4 chưa có Bearer pipeline để tạo trạng thái đó từ token Login.
LoginRequest và RegisterRequest che password trong `toString()`.

GlobalExceptionHandler trả lỗi dạng `{"code":"...","message":"..."}`:

- Input không hợp lệ: 400 VALIDATION; JSON lỗi được sanitize.
- Duplicate email/Customer: 409 CONFLICT; email trùng có message `Email already registered: <email>`.
- Sai credentials: 403 FORBIDDEN, message `Invalid credentials`.

VenueDevSecurityConfig permit đúng ba method/path Identity trên; chỉ hai POST Identity
được thêm vào CSRF exemptions. Các allowlist M1–M3 được giữ; route khác vẫn denyAll.
Ngoài venue-dev, SecurityConfig vẫn deny-all và IdentityController không active.

## Security boundary

**COMPLETED:** Login -> kiểm tra credentials -> access token + expiresAt.
SpringCurrentUser đọc Authentication đã có trong SecurityContext; principal name
map thành userId. Missing/anonymous Authentication là unauthenticated;
`getUserId()` trực tiếp trong trạng thái đó ném `ForbiddenException("Authentication required")`.

**NOT COMPLETED:** Bearer token -> verification -> Authentication -> SecurityContext -> SpringCurrentUser.

`JwtService` implements TokenVerifier nhưng `verify` còn ném UnsupportedOperationException.
`JwtAuthenticationFilter` chưa đăng ký và thân filter còn ném ServletException.
Login không tự tạo Authentication/SecurityContext. Vì vậy M4 không phải production authentication.
Hạn chế Bearer hiện tại không phải contract buộc các milestone sau phải giữ nguyên.

## Clean Architecture

Domain độc lập framework. Application điều phối qua các port `IdentityService`,
`CustomerRepository`, `CurrentUser` và chỉ dùng Domain để tạo Customer.
Infrastructure implements IdentityService/CustomerRepository; Web implements CurrentUser
bằng Spring Security và controller chỉ gọi Application. Composition root nối dependency.
Không chuyển credential hay HTTP/security responsibility vào Customer Domain.

## Deferred

Theo code hiện tại và [kế hoạch M4](m4-overview.md), còn ngoài phạm vi nghiệm thu:

- Bearer/JWT verification, production TokenVerifier, authentication filter hoàn chỉnh và mapping SecurityContext.
- Production authentication, JWT signing, BCrypt/password hardening, advanced RBAC.
- Refresh token, OAuth/external provider, password reset và email verification.
- Production Identity persistence; runtime hiện chỉ giữ accounts in-memory.
- Production Customer/JPA persistence; M4 chọn in-memory.
- Cart/SeatHold/Booking/Checkout/Payment theo roadmap tiếp theo.
