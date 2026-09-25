# Tiến độ Cinema V1

Ngày lưu kế hoạch: 2026-09-17.

Kế hoạch chi tiết: [Cinema V1](cinema-v1-plan.md). Tài liệu này ghi nhận trạng thái milestone, kết quả kiểm thử và công việc còn lại của dự án cá nhân.

## Nguyên tắc thực hiện

- Nghiệp vụ tương đối đầy đủ, công nghệ triển khai cố tình đơn giản.
- Giữ Clean Architecture; Domain độc lập, Application điều phối qua các port, Infrastructure cung cấp adapter, Web chuyển đổi HTTP.
- V1 dùng identity đơn giản, fake payment và seat hold DB/in-memory. Các nâng cấp JWT, BCrypt, Redis, RabbitMQ, thanh toán thật và concurrency nâng cao thuộc Phase 2.
- Làm tuần tự từng milestone; xác minh tiêu chí hoàn thành và kiểm thử liên quan trước khi chuyển bước.
- Sau mỗi milestone, cập nhật checklist, kết quả kiểm thử và việc còn lại tại tài liệu này.
- Chưa đánh dấu hoàn thành dựa trên skeleton hoặc mô tả cũ; cần đối chiếu code và kiểm thử thực tế.

## Checklist

Kế hoạch gồm **13 milestone**, từ M0 đến M12.

- [x] **M0 — Architecture foundation:** hoàn thiện thành phần chung và port; 13 DomainRulesTest và 6 architecture tests đạt, không bỏ qua.
- [x] **M1 — Cinema / Hall / Seat:** hoàn thành ngày 2026-09-21; đã kiểm chứng luồng HTTP và các điều kiện kỹ thuật.
- [x] **M2 — Movie:** tạo, xóa và truy vấn phim; kiểm tra tiêu đề và thời lượng.
- [x] **M3 — Screening:** hoàn thành Screening core: tạo, truy vấn, liệt kê theo Movie, hủy, kiểm tra lịch trùng cùng Hall, HTTP API và security trong venue-dev. GetAvailableSeats, booking và trạng thái bookable vẫn deferred.
- [ ] **M4 — Identity + Customer:** đăng ký, đăng nhập đơn giản; nối ApplicationUser với Customer qua IdentityService và CurrentUser.
- [ ] **M5 — Cart + simple SeatHold:** thêm/xóa ghế, kiểm tra phòng, trạng thái ghế và chủ giỏ; giữ ghế qua SeatHoldService.
- [ ] **M6 — Checkout + Booking:** nhóm giỏ theo suất chiếu; một Booking thuộc một Screening; quản lý PENDING / CONFIRMED / CANCELLED.
- [ ] **M7 — Fake Payment:** kiểm tra ownership và booking PENDING; thanh toán giả thành công, lưu Payment và xác nhận Booking.
- [ ] **M8 — Ticket:** phát hành một vé cho mỗi BookingItem sau thanh toán thành công; mã vé và trạng thái hợp lệ.
- [ ] **M9 — Ownership / Authorization:** hoàn thiện kiểm tra quyền tại Application cho xem/hủy/thanh toán booking và xem vé.
- [ ] **M10 — REST Controllers + Exception handling:** nối đầy đủ API, DTO và xử lý lỗi.
- [ ] **M11 — Persistence + integration tests:** adapter JPA và mapping ở Infrastructure; kiểm thử luồng tích hợp từ đăng nhập đến nhận vé.
- [ ] **M12 — Docker + OpenAPI + Benchmark:** đóng gói, tài liệu API và đo baseline trước Phase 2.

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

## Bước tiếp theo

M4 — Identity + Customer: NEXT / NOT STARTED. Các lần chạy test tiếp theo do chủ dự án thực hiện; trợ lý cung cấp lệnh, không tự chạy test.

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
- [ ] M4: Customer có ID và userId hợp lệ (luật ID chung đã có ở M0).
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
