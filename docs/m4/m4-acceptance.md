# M4 — Identity + Customer Acceptance

## Scope

**M4 ACCEPTED** trong phạm vi Identity + Customer đơn giản, in-memory và Identity HTTP API
trong venue-dev tại `bf6383e` trên `feature/nghia/moc-8`.

Báo cáo đối chiếu source/tests và Git history; kết quả focused tests, full Maven regression
và HTTP thủ công được chủ dự án xác nhận trong yêu cầu closeout.
Không chạy lại Maven trong job tài liệu và không suy đoán tổng số test.
Xem [implementation](m4-implementation.md) và [overview](m4-overview.md).

## Customer tests

`CustomerTest` kiểm tra giữ đúng state; id kế thừa BaseEntity; null/empty/blank
id/userId/displayName bị từ chối; trim displayName; không normalize id/userId;
ID được validate trước các invariant khác; không có field/method credential.

`InMemoryCustomerRepositoryTest` kiểm tra save/findById/findByUserId; missing/null lookup
trả empty; duplicate ID/userId bị từ chối và giữ dữ liệu cũ; cho phép trùng displayName;
lookup userId chính xác; kết quả không phụ thuộc insertion order; storage không public;
hai insert cạnh tranh cùng userId chỉ một thành công, một conflict.

## Register acceptance

`RegisterCommandHandlerTest` kiểm tra trả customer ID; gọi IdentityService đúng input;
Customer được lưu đúng id/userId/displayName; null/empty/blank input; null command
không tạo side effects; Customer đã tồn tại gây conflict; không sinh ID/lưu trước
các kiểm tra userId và Customer; conflict từ save được truyền lên.

Duplicate email được kiểm tra tại IdentityServiceImplTest và HTTP acceptance.
HTTP thành công trả **201** với đúng một field `id`; duplicate email trả
**409 CONFLICT**, message chứa email. Register trả customer ID, không phải access token.

## Login acceptance

`LoginCommandHandlerTest` kiểm tra truyền input qua IdentityService, giữ nguyên
AuthenticationResult, validation trước side effects và truyền exception từ port.
`IdentityServiceImplTest` kiểm tra generated userId, duplicate email, login đúng có token
nonblank/expiry trong tương lai theo Clock cố định, unknown email/wrong password bị từ chối,
và ApplicationUser.toString không lộ password hash.

HTTP login đúng trả **200**, `accessToken` và `expiresAt` dạng chuỗi nonblank.
Sai password trả **403 FORBIDDEN**, message chính xác `Invalid credentials`.
HTTP test không kiểm tra expiry trong tương lai; kiểm tra đó nằm ở adapter unit test.

## Current User acceptance

`GetCurrentUserQueryHandlerTest` kiểm tra authenticated userId mapping;
unauthenticated trả `CurrentUserView(null,false)` mà không đọc userId;
null query bị validation trước khi gọi port.

`SpringCurrentUserTest` đặt Authentication trực tiếp vào SecurityContext để kiểm tra
principal name -> userId; anonymous và missing authentication trả false,
và getUserId ném ForbiddenException. Các unit test này không chứng minh Bearer authentication.

HTTP không có authentication: **200**, `userId:null`, `authenticated:false`.
Đây là behavior thực tế, thay cho dự kiến forbidden/unauthorized trong assignment ban đầu.

## HTTP acceptance

`IdentityHttpAcceptanceTest` dùng SpringBootTest RANDOM_PORT, TestRestTemplate,
profile venue-dev, controller/handler/adapters và security thật, không mock.
Các method thực tế:

| Test method | Assertions chính |
| --- | --- |
| registersAndLogsInThroughRealHttp | Register 201, đúng một field id nonblank; Login 200, token/expiry textual nonblank |
| duplicateEmailReturnsConflict | Register lại cùng email -> 409, code CONFLICT, message nonblank chứa email |
| invalidCredentialsReturnForbidden | Register rồi login sai password -> 403, code FORBIDDEN, message Invalid credentials |
| currentUserIsUnauthenticatedWithoutAuthentication | GET /identity/me -> 200, userId null, authenticated false |

Các POST trên không gửi CSRF token. File này không có scenario invalid-register 400,
malformed JSON, unsupported Identity routes hoặc authenticated Bearer request.
Không coi các scenario trong kế hoạch gốc là test HTTP đã hiện thực.

## Bearer-token limitation

Chủ dự án đã xác minh thủ công: gửi accessToken hiện tại dưới dạng Bearer vẫn cho
kết quả unauthenticated ở `/identity/me`. Source giải thích nguyên nhân: token verification,
Authentication và SecurityContext pipeline chưa hoàn chỉnh.

Đây là **giới hạn M4 đã biết**, không phải yêu cầu nghiệm thu vĩnh viễn.
Acceptance tests không được đóng băng quy tắc “Bearer token phải luôn unauthenticated”.
Công việc authentication sau này được phép thay đổi behavior này khi verification được triển khai.

## Regression

Focused M4 tests và full Maven regression suite đã được chủ dự án xác nhận **BUILD SUCCESS**.
Không ghi tổng test count vì chưa có output tổng hiện tại đáng tin cậy trong bằng chứng closeout.
Các số checkpoint M2/M3 trong tài liệu khác vẫn là lịch sử của các checkpoint đó.

Job này chỉ đối chiếu source/test/history và kiểm tra diff tài liệu; không chạy lại Maven,
không thay source/test để làm khớp nhận định nghiệm thu.

## Result

M4 đạt nghiệm thu cho Customer Domain/repository in-memory, Register, Login phát token,
Get Current User qua CurrentUser port, SpringCurrentUser, venue-dev wiring và Identity HTTP API.
Các worker slices đã được tích hợp trên nhánh Nghĩa; không suy ra đã merge vào main.

Bearer/JWT verification, production authentication/TokenVerifier/filter, refresh token,
OAuth, password reset và production Identity/Customer persistence vẫn deferred.
