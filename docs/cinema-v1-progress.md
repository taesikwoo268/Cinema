# Tiến độ Cinema V1

Cập nhật tài liệu: **2026-10-10**. Trạng thái dưới đây đối chiếu code đến `10f53a4`; không đồng nghĩa mọi thay đổi đã vào main hoặc database local đã được migrate.

## Checklist hiện tại

- [x] M0 — nền Clean Architecture và architecture tests.
- [x] M1 — Cinema / Hall / Seat.
- [x] M2 — Movie; MySQL soft delete giữ tham chiếu lịch sử.
- [x] M3 — Screening core và available-seats.
- [x] M4 — Identity + Customer; Package 3 bổ sung xác thực Bearer cho MySQL.
- [x] M5 — Cart + SeatHold; bảo vệ mutation/first-cart creation.
- [x] M6 — Checkout + Booking; nhóm theo screening, chống checkout trùng.
- [x] M7 — Fake Payment, ownership và retry.
- [x] M8 — Ticket domain, phát vé theo booking item và idempotency.
- [x] M9 — Ownership; Package 3 bổ sung CUSTOMER/ADMIN ở HTTP.
- [x] M10 — REST workflow, mapping lỗi 400/403/404/409.
- [x] M11 — JPA/MySQL, Flyway, transaction, locking/uniqueness và integration tests.
- [ ] M12 — Docker / OpenAPI / benchmark; chưa có nghiệm thu.
- [ ] Frontend đơn giản và smoke test trên môi trường của người dùng.

“Đã triển khai” ở đây là code và kiểm thử trên feature branch; trạng thái merge cần kiểm tra riêng trên GitHub.

## Bàn giao theo package

| Package | Commit mốc | Phạm vi |
| --- | --- | --- |
| 1 | `2c7f219`, `f79445b`, `9a6be03` | M8–M10; concurrency fixes cho seat/cart/checkout |
| 2 | `ed9b0c2`, `97bb72c` | M11; MySQL, transaction tests, movie soft-delete regression |
| 3 | `10f53a4` | Database Bearer tokens, roles, security chain MySQL, migration V3 |

Package 3 được tạo từ Package 2 vì main chưa chứa Package 2 tại thời điểm bắt đầu. Review/merge Package 2 trước Package 3; không xóa milestone branches. Đợt sửa tài liệu này không fetch, merge, push hoặc thay database.

## Kết quả kiểm thử đã ghi nhận

| Checkpoint | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| Package 1 ban đầu | 708 | 0 | 0 | 8 |
| Package 2 sau review `97bb72c` | 766 | 0 | 0 | 8 |
| Package 3 `10f53a4` | 787 | 0 | 0 | 8 |

Package 3: `mvn clean test` có MySQL thực tế 9.4; 29 contract tests trên H2 và 29 trên MySQL đều đạt. Bao gồm V2 → V3 giữ dữ liệu, token qua restart/hai app contexts, role/ownership, rollback, concurrency, payment retry và HTTP checkout → payment → ticket bằng token thật. `git diff --check` đạt. 8 skipped là acceptance scaffolds cũ, không được dùng làm bằng chứng cho workflow; workflow được kiểm tra bằng các test mới có assertion.

Không chạy lại Maven trong đợt cập nhật tài liệu. Không suy ra MySQL đã được kiểm tra từ một lần chạy chỉ có H2.

## Công việc tiếp theo

1. Review diff và merge Package 2 → Package 3 sau khi kiểm tra CI/conflict.
2. Sao lưu database local, chạy profile `mysql` với đúng URL (ví dụ `localhost:3306/cinema_package2`), xác nhận Flyway V1–V3.
3. Đăng ký customer/admin, cấp role admin qua SQL có kiểm soát, nhập dữ liệu mẫu qua API.
4. Smoke test bằng Postman: login → chọn ghế → checkout → pay → ticket; restart và đọc lại dữ liệu.
5. Bắt đầu frontend: đăng nhập, phim/suất chiếu, chọn ghế, giỏ/thanh toán giả, vé của tôi. Chọn dev proxy/CORS trước khi tích hợp trình duyệt.
6. M12 và các nâng cấp vận hành được giao riêng; chưa tự coi là hoàn thành.

Giới hạn: hold chưa có TTL; thanh toán giả; token chưa có refresh/logout/reset/rate limit/cleanup; gateway thành công trước DB commit vẫn là failure window. Chi tiết [mysql.md](mysql.md).

## Hồ sơ lịch sử M0–M4

Các mục còn lại giữ nguyên attribution và bằng chứng tại thời điểm cũ. Các trạng thái “chưa triển khai/deferred”, quy trình cá nhân và số test bên dưới không thay thế checklist hiện tại ở đầu tài liệu.

## Trạng thái thực tế M1 — 2026-09-21

**M1 hoàn thành ngày 2026-09-21 trên D:\Cinema.**
Dự án cá nhân: không áp dụng điều kiện chờ review/merge nhóm.
Căn cứ nghiệm thu gồm luồng HTTP thật, integration, validation, uniqueness, security và hồi quy đã kiểm chứng ở lần chạy trước.

- [x] Domain và Application bảo vệ invariant, validate đầu vào trước lookup.
- [x] Ba adapter in-memory khớp port; kiểm tra duplicate và cạnh tranh đã chạy.
- [x] Thêm CinemaController, HallController, SeatController và VenueConfiguration.
- [x] GlobalExceptionHandler trả 409 CONFLICT cho ConflictException.
- [x] VenueApiIntegrationTest: 13 trường hợp với controller/handler/repository thật.
- [x] VenueHttpAcceptanceTest: server cổng ngẫu nhiên, HTTP thật tạo Cinema → Hall → Seat → đọc lại; xác minh 400/404/409/403.
- [x] VenueSnapshotTest: danh sách bất biến và không đổi sau các lần ghi tiếp theo.
- [x] clean verify: 165 trường hợp, 157 đạt, 0 failure/error, 8 workflow ngoài M1 skipped.
- [x] Rà soát code và bằng chứng kỹ thuật theo quy trình cá nhân.
- [x] Checklist M1 đã đóng.

## Trạng thái thực tế M2 — 2026-09-25

**M2 hoàn thành trên D:\Cinema. Owner: Nghia — triển khai toàn bộ M2 Movie.**
Bằng chứng kỹ thuật gồm domain, application, repository in-memory và acceptance test HTTP đều đạt.

- [x] `Movie` chặn title null/blank và duration <= 0 bằng invariant domain.
- [x] Handler tạo phim kiểm tra validation input trước khi generate ID và lưu.
- [x] Repository in-memory lưu, đọc và xóa phim đúng contract.
- [x] HTTP thật kiểm tra lifecycle: tạo → đọc → list → xóa → 404.
- [x] Test M2 có assertion thực, không `@Disabled`; 44 unit/application/repository tests và 7 Movie HTTP acceptance tests đạt (51 trường hợp trong nhóm Movie).

- [x] Movie Domain, commands/queries và MovieRepository contract hoàn thiện.
- [x] Spring composition/profile, MovieController và Movie security boundary hoàn thiện.
- [x] MovieController, bốn handler beans và InMemoryMovieRepository active trong venue-dev; ngoài profile này không tạo Movie consumers/repository, default context khởi động được.
- [x] Full clean test checkpoint 2026-09-25: 230 tests, 0 failures, 0 errors, 8 skipped thuộc workflow hiện có.

Movie JPA persistence chưa triển khai trong M2.
Tài liệu: [đặc tả](m2/m2-specification.md), [triển khai](m2/m2-implementation.md), [nghiệm thu](m2/m2-acceptance.md).

## Trạng thái thực tế M3 — 2026-09-25

**M3 hoàn thành trên D:\Cinema. Phạm vi hoàn thành là Screening core.**
Bằng chứng gồm Domain/Application, port, adapter in-memory, venue-dev composition,
HTTP API, security boundary, architecture test và full clean regression.

- [x] Screening bảo vệ invariant thời gian, Hall/Movie ID, giá và lifecycle cancel.
- [x] Create, Cancel, Get one và Get list theo Movie đã triển khai qua Application.
- [x] InMemoryScreeningRepository hỗ trợ overlap half-open, thứ tự ổn định và cập nhật state cancel.
- [x] Screening Web API và venue-dev security boundary đã hoàn thiện.
- [x] Final clean test: 299 tests, 0 failures, 0 errors, 8 skipped thuộc BookingWorkflowTest.
- [x] Architecture tests: 6/6 đạt; Screening regression: 69/69 đạt.

GetAvailableSeats, SeatHold, Booking/Checkout, Payment thật, JPA Screening persistence
và complete Screening transition chưa triển khai.
Tài liệu: [đặc tả M3](m3/m3-specification.md), [triển khai M3](m3/m3-implementation.md), [nghiệm thu M3](m3/m3-acceptance.md).

## Trạng thái thực tế M4 — 2026-09-27

**M4 hoàn thành trong phạm vi Identity + Customer đơn giản**, tại `bf6383e`
trên `feature/nghia/moc-8`, từ frozen base `3a44962`.

- [x] Customer Domain bảo vệ id/userId/displayName và trim displayName.
- [x] InMemoryCustomerRepository lookup/save, duplicate ID/userId và concurrency tests.
- [x] Register tạo identity, tạo/lưu Customer liên kết userId, trả customer ID.
- [x] Login validate credentials và trả accessToken + expiresAt.
- [x] GetCurrentUser và SpringCurrentUser đọc authentication state qua port; chưa xác thực trả `(null,false)`.
- [x] venue-dev composition và ba Identity HTTP endpoints.
- [x] HTTP acceptance: register/login thành công, duplicate email 409, sai password 403, me chưa xác thực 200.
- [x] Focused tests và full Maven regression được chủ dự án xác nhận BUILD SUCCESS; không chạy lại trong job docs.
- [x] Cập nhật implementation, acceptance và trạng thái M4.

Deferred: Bearer/JWT verification, production TokenVerifier/authentication filter,
SecurityContext authentication từ token, production authentication, refresh token,
OAuth/password reset và production Identity/Customer persistence.
Bearer hiện chưa xác thực được `/identity/me`; đây là giới hạn hiện tại, không phải acceptance vĩnh viễn.

Attribution: Bảo Long phụ trách Customer slice/repository/tests; Duy Anh phụ trách
Register/tests; Duy Khanh phụ trách Identity/Login/CurrentUser/tests; Nguyễn Trung Nghĩa
freeze contracts, tích hợp và hoàn thiện shared wiring/Web/security/HTTP acceptance.
Các slice được tích hợp qua `115a307`, `705efe2`, `c63a020`, sau đó `378cfe6` và `bf6383e`.
Không khẳng định đã merge vào main.

Tài liệu: [overview](m4/m4-overview.md), [implementation](m4/m4-implementation.md), [acceptance](m4/m4-acceptance.md).

## Yếu tố bắt buộc để M1 được đánh dấu hoàn thành

- [x] Handler và repository contract được đối chiếu với đặc tả và kiểm thử.
- [x] Tạo Cinema → Hall → Seat → đọc lại bằng Application và HTTP.
- [x] Integration test M1 đạt: tạo/đọc, null/blank, 400, 404 và 409.
- [x] Hall unique trong Cinema, Seat unique theo (hallId, row, number).
- [x] Profile venue-dev đúng allowlist; route ngoài M1 bị chặn.
- [x] Test M1 có assertion thực, không @Disabled; các test cạnh tranh đạt.
- [x] Đối chiếu implementation với bằng chứng nghiệm thu; không yêu cầu review nhóm.
- [x] Cập nhật trạng thái hoàn thành trong tài liệu.

Không coi một báo cáo test từ D:\MyCinema là bằng chứng cho D:\Cinema.
[Bằng chứng nghiệm thu kỹ thuật](m1/m1-acceptance.md) ghi lớp test và lệnh chạy.

## Bước tiếp theo tại checkpoint cũ

Phần này đã được thay thế bởi công việc hiện tại ở đầu tài liệu. Không tiếp tục giao M5 như một milestone chưa triển khai.

## Lịch sử M1 — 2026-09-18 (không phải trạng thái hiện tại)

- Đã thêm ba request DTO, response ID, profile security venue-dev và lỗi JSON 400.
- 40 trường hợp kiểm thử Web/security mới đạt; gồm test ứng dụng thật với profile
  venue-dev và test riêng có controller thử nghiệm để kiểm tra JSON/security.
- Toàn bộ verify: 61 đạt, 8 workflow skeleton ngoài M1 skipped, không lỗi.
- Chưa có controller nghiệp vụ, VenueConfiguration hoặc mapping lỗi 409;
  chờ lớp Application và adapter đúng phân công.
- Chưa nghiệm thu M1, chưa ghép main hoặc chuyển mốc 6.

## Hoàn thành M0 — 2026-09-17

- `Money` từ chối amount/currency null và amount âm bằng `DomainException`; chấp nhận 0, giữ nguyên độ chính xác và currency.
- `BaseEntity` từ chối ID null, rỗng hoặc chỉ có khoảng trắng bằng `DomainException`.
- Rà soát các port nền tảng, bổ sung `package-info.java` mô tả ranh giới và phân biệt user ID/customer ID; giữ nguyên chữ ký hiện có.
- `DomainRulesTest` được thay bằng 13 trường hợp kiểm tra nền tảng thực tế; không còn `@Disabled` hoặc test rỗng trong lớp này.
- Chạy `mvnw.cmd '-Dmaven.repo.local=C:\Users\admin\.m2\repository' -o verify` sau khi clean: build thành công; 29 trường hợp được báo cáo, 21 đạt, 8 workflow skeleton bị bỏ qua, không có failure/error. Trong đó architecture tests đạt 6/6.
- `BookingWorkflowTest` vẫn là backlog cho các milestone sau, không được tính là kiểm thử đã đạt của M0.

### Phân bổ acceptance test nghiệp vụ từ DomainRulesTest cũ

Các test rỗng được chuyển thành checklist dưới đây để triển khai cùng tính năng; chúng chưa được tính là đã kiểm thử.

- [x] M2: Movie có tiêu đề và thời lượng dương; đã xác minh tại checkpoint M2.
- [ ] M3: Screening có startTime < endTime.
- [x] M4: Customer có ID/userId/displayName hợp lệ; đã được kiểm tra bằng CustomerTest.
- [ ] M5: Cart từ chối cặp screening/seat trùng.
- [ ] M6: Booking có ít nhất một snapshot item; booking đã hủy không thể confirm.
- [ ] M8: Ticket USED/CANCELLED không thể trở lại VALID.

## Lịch sử kiểm tra M0 trước triển khai — 2026-09-17

**Chưa hoàn thành.** Cấu trúc bốn tầng và các port nền tảng đã có, nhưng hành vi nền tảng và kiểm thử domain chưa hoàn thiện.

- `BaseEntity` có ID và getter; `DomainException` đã có.
- `Money` mới là record dữ liệu, chưa kiểm tra số tiền không âm và dữ liệu amount/currency hợp lệ.
- Đã có `UseCase`, các repository port, `CurrentUser`, `IdentityService`, `PaymentGateway`, `SeatHoldService`. `CinemaRepository` và `HallRepository` chưa có; hai port này được nêu cụ thể ở M1.
- `CleanArchitectureTest`: 6 test đạt, không bỏ qua.
- `DomainRulesTest`: 8 test bị bỏ qua do `@Disabled`; tất cả thân test đang rỗng. Chưa đạt điều kiện hoàn thành M0 trong kế hoạch.
- Các test domain hiện trải rộng sang Movie, Screening, Cart, Booking và Ticket; cần phân bổ theo milestone khi triển khai.

Lệnh xác minh đã chạy thành công:

```powershell
.\mvnw.cmd '-Dmaven.repo.local=C:\Users\admin\.m2\repository' '-Dtest=DomainRulesTest,CleanArchitectureTest' -o test
```

Kết quả: 14 test được báo cáo, 0 failure, 0 error, **8 skipped**. `BUILD SUCCESS` chưa đồng nghĩa M0 hoàn thành. Chỉ kiểm tra và cập nhật tài liệu, chưa sửa implementation.

## Nhật ký

| Ngày | Nội dung | Xác minh |
| --- | --- | --- |
| 2026-09-17 | Lưu kế hoạch gốc và tạo checklist M0–M12. | Chỉ cập nhật tài liệu; chưa đánh giá mức hoàn thành implementation. |
| 2026-09-17 | Kiểm tra M0: chưa hoàn thành. | Architecture: 6 đạt; DomainRulesTest: 8 skipped, thân test rỗng; Money chưa kiểm tra invariant. |
| 2026-09-17 | Hoàn thiện M0 theo phạm vi được duyệt. | Domain: 13 đạt; architecture: 6 đạt; toàn bộ verify thành công (21 đạt, 8 workflow skipped). |
| 2026-09-25 | Nghia hoàn thành toàn bộ M2 Movie; M3 là bước tiếp theo, chưa bắt đầu. | Full clean test: 230 tests, 0 failures, 0 errors, 8 workflow skipped; Movie HTTP acceptance: 7/7; architecture: 6/6. |
