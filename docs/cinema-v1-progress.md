# Tiến độ Cinema V1

Ngày lưu kế hoạch: 2026-09-17.

Kế hoạch chi tiết: [Cinema V1](cinema-v1-plan.md). Tài liệu này ghi nhận trạng thái milestone, kết quả kiểm thử và công việc còn lại của nhóm.

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
- [ ] **M1 — Cinema / Hall / Seat:** tạo và truy vấn dữ liệu rạp, phòng, ghế; kiểm tra luật và tính duy nhất của ghế trong phòng.
- [ ] **M2 — Movie:** tạo, xóa và truy vấn phim; kiểm tra tiêu đề và thời lượng.
- [ ] **M3 — Screening:** tạo và truy vấn suất chiếu; kiểm tra lịch trùng trong cùng phòng và trạng thái cho phép đặt vé.
- [ ] **M4 — Identity + Customer:** đăng ký, đăng nhập đơn giản; nối ApplicationUser với Customer qua IdentityService và CurrentUser.
- [ ] **M5 — Cart + simple SeatHold:** thêm/xóa ghế, kiểm tra phòng, trạng thái ghế và chủ giỏ; giữ ghế qua SeatHoldService.
- [ ] **M6 — Checkout + Booking:** nhóm giỏ theo suất chiếu; một Booking thuộc một Screening; quản lý PENDING / CONFIRMED / CANCELLED.
- [ ] **M7 — Fake Payment:** kiểm tra ownership và booking PENDING; thanh toán giả thành công, lưu Payment và xác nhận Booking.
- [ ] **M8 — Ticket:** phát hành một vé cho mỗi BookingItem sau thanh toán thành công; mã vé và trạng thái hợp lệ.
- [ ] **M9 — Ownership / Authorization:** hoàn thiện kiểm tra quyền tại Application cho xem/hủy/thanh toán booking và xem vé.
- [ ] **M10 — REST Controllers + Exception handling:** nối đầy đủ API, DTO và xử lý lỗi.
- [ ] **M11 — Persistence + integration tests:** adapter JPA và mapping ở Infrastructure; kiểm thử luồng tích hợp từ đăng nhập đến nhận vé.
- [ ] **M12 — Docker + OpenAPI + Benchmark:** đóng gói, tài liệu API và đo baseline trước Phase 2.

## Bước tiếp theo

M1 đang triển khai phần Web độc lập trên nhánh feature/nghia/moc-5-web-integration.
Quy tắc đã chốt; bước tiếp theo là nhận PR hợp đồng/handler của Bảo Long và
adapter in-memory của Duy Khánh để nối controller, cấu hình và kiểm thử toàn luồng.

## Tiến độ M1 — 2026-09-18

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

- [ ] M2: Movie có tiêu đề và thời lượng dương.
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
