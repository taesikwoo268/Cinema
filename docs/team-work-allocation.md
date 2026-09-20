# Phân công công việc Cinema V1

## 1. Phạm vi và nguyên tắc

Tài liệu sử dụng **18 mốc trong [kế hoạch Cinema V1](cinema-v1-plan.md)**,
bao gồm cả xác định mục tiêu, nguyên tắc và thiết kế bản đồ kiến trúc.
Số mốc 1–18 khác với mã roadmap kỹ thuật M0–M12.

**Nguyễn Trung Nghĩa — trưởng nhóm** đã hoàn thành mốc 1–4,
bao gồm V0 tại mốc 4 (giai đoạn 0 / M0).
A: Bảo Long; B: Duy Anh; C: Duy Khánh.

**Cả nhóm chỉ triển khai một mốc tại một thời điểm, theo thứ tự 5 → 18.**
Không gom nhiều mốc thành một đợt làm song song.
Trong cùng mốc, các thành viên có thể làm song song phần việc độc lập.
Phần việc phụ thuộc phải chờ hợp đồng hoặc thay đổi liên quan được ghép trước.

Mỗi mốc có một người phụ trách chính và phần việc cụ thể cho cả nhóm.
Người phụ trách chính chịu trách nhiệm chức năng; Nghĩa review, ghép vào
`main` và xác nhận nghiệm thu. Hoàn thành ở nhánh cá nhân chưa được tính
là hoàn thành mốc.

Mốc 14, 15 và 16 lần lượt hoàn thiện toàn bộ REST API, persistence và testing.
Trước đó, mỗi mốc vẫn phải có adapter tạm và kiểm thử cần thiết để chạy,
nghiệm thu chức năng. Các kiểm tra ownership cần cho use case được viết
ngay tại mốc đó; mốc 13 rà soát và hoàn thiện nhất quán toàn hệ thống.

## 2. Phân công theo từng mốc

### Mốc 1 — Xác định mục tiêu V1

- Phụ trách: Nguyễn Trung Nghĩa.
- Trạng thái: **Hoàn thành**.
- Sản phẩm: phạm vi và luồng nghiệp vụ mục tiêu.

### Mốc 2 — Nguyên tắc đơn giản hóa

- Phụ trách: Nguyễn Trung Nghĩa.
- Trạng thái: **Hoàn thành**.
- Sản phẩm: quy tắc phân biệt nghiệp vụ bắt buộc và implementation đơn giản.

### Mốc 3 — Bản đồ toàn hệ thống

- Phụ trách: Nguyễn Trung Nghĩa.
- Trạng thái: **Hoàn thành**.
- Sản phẩm: thiết kế các phân hệ, quan hệ và hướng kiến trúc.

### Mốc 4 — V0: nền kiến trúc, giai đoạn 0 / M0

- Phụ trách: Nguyễn Trung Nghĩa.
- Trạng thái: **Hoàn thành**.
- Sản phẩm: cấu trúc bốn tầng, port nền tảng và invariant chung.
- Kiểm thử: 13 domain tests và 6 architecture tests đạt.
- Commit M0: `947df2b`. Xác nhận nền tảng này có trên `main` trước khi
  tạo nhánh triển khai mốc 5.

### Mốc 5 — Cinema / Hall / Seat

Đặc tả và giao việc chi tiết:

- [Đặc tả M1 và hợp đồng dùng chung](m1-specification.md).
- [Nguyễn Trung Nghĩa: Web, cấu hình và tích hợp](m1-nguyen-trung-nghia.md).
- [Bảo Long: Domain và Application](m1-bao-long.md).
- [Duy Anh: kiểm thử Domain/Application](m1-duy-anh.md).
- [Duy Khánh: adapter in-memory và kiểm thử](m1-duy-khanh.md).

Các bản giao việc này quy định file và chữ ký hàm cho M1. Nghĩa đã xác nhận
các quy tắc mục 3 đặc tả ngày 2026-09-18; triển khai và kiểm thử theo hợp đồng này.

Phụ trách chính: **Bảo Long**. Trạng thái: **Đang triển khai phần Web độc lập của Nghĩa**.

Ngày 2026-09-18: request/response, security theo profile và xử lý JSON lỗi đã có
kiểm thử đạt trên nhánh Nghĩa. Phần nối controller/handler/repository còn chờ
bàn giao; mốc 5 chưa được nghiệm thu. Chi tiết tại bản giao việc của Nghĩa.

- **Nghĩa:** Chốt port, cấu hình ghép nối; review và tích hợp.
- **Bảo Long:** Triển khai Domain, repository port và use case Cinema/Hall/Seat.
- **Duy Anh:** Viết test quy tắc tên, số ghế và ghế trùng trong phòng.
- **Duy Khánh:** Chuẩn bị dữ liệu mẫu, adapter in-memory và kịch bản tạo/truy vấn theo hợp đồng đã chốt.

**Điều kiện nghiệm thu:** Tạo/truy vấn rạp, phòng, ghế hoạt động; từ chối dữ liệu sai và ghế trùng trong phòng.

### Mốc 6 — Movie

Phụ trách chính: **Duy Anh**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Review hợp đồng Movie, ghép cấu hình và nghiệm thu.
- **Bảo Long:** Viết test tiêu đề, thời lượng và các trường hợp không tìm thấy phim.
- **Duy Anh:** Triển khai Domain, port, use case và adapter Movie.
- **Duy Khánh:** Chuẩn bị dữ liệu phim và kịch bản kiểm thử tạo/truy vấn/xóa.

**Điều kiện nghiệm thu:** Luồng quản lý phim chạy được; tiêu đề và thời lượng được kiểm tra.

### Mốc 7 — Screening

Phụ trách chính: **Duy Anh**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Chốt quy tắc lịch chiếu; tích hợp và nghiệm thu.
- **Bảo Long:** Nối tra cứu Hall/Seat đã hoàn thành; kiểm tra ghế thuộc phòng của suất.
- **Duy Anh:** Triển khai Screening, truy vấn và kiểm tra lịch trùng qua repository.
- **Duy Khánh:** Viết test giờ bắt đầu/kết thúc, lịch trùng và trạng thái không cho đặt.

**Điều kiện nghiệm thu:** Tạo/truy vấn suất chiếu hoạt động; lịch cùng phòng không trùng; trạng thái bookable đúng.

### Mốc 8 — Identity / Customer

Phụ trách chính: **Duy Khánh**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Chốt phân biệt user ID/customer ID, hợp đồng CurrentUser; ghép cấu hình.
- **Bảo Long:** Viết test hồ sơ Customer và ánh xạ user–customer.
- **Duy Anh:** Chuẩn bị kịch bản đăng ký/đăng nhập, dữ liệu mẫu và tài liệu sử dụng.
- **Duy Khánh:** Triển khai IdentityService, Customer, CurrentUser và identity adapter đơn giản.

**Điều kiện nghiệm thu:** Đăng ký/đăng nhập đơn giản hoạt động; Application xác định đúng customer hiện tại.

### Mốc 9 — Cart + Seat Hold

Phụ trách chính: **Bảo Long**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Chốt vòng đời giữ/nhả ghế, review và tích hợp.
- **Bảo Long:** Triển khai Cart, use case thêm/xóa ghế và SeatHold/SeatReservation adapter đơn giản.
- **Duy Anh:** Viết test ghế sai phòng, suất không bookable, ghế đã đặt và giỏ trùng.
- **Duy Khánh:** Nối danh tính hiện tại và viết test giữ ghế của khách khác; sửa identity adapter nếu cần.

**Điều kiện nghiệm thu:** Thêm/xóa giỏ và giữ/nhả ghế hoạt động; kiểm tra trạng thái ghế và chủ giỏ đúng.

### Mốc 10 — Checkout / Booking

Phụ trách chính: **Nguyễn Trung Nghĩa**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Triển khai Booking, Checkout và hủy booking; sở hữu handler điều phối và trạng thái Domain.
- **Bảo Long:** Nối Cart/SeatHold với Checkout qua port; xử lý giữ/nhả ghế phục vụ booking.
- **Duy Anh:** Viết test nhóm theo suất, snapshot giá, tổng tiền và giỏ rỗng.
- **Duy Khánh:** Viết test ownership, hủy booking và chuyển trạng thái không hợp lệ.

**Điều kiện nghiệm thu:** Checkout tạo Booking PENDING, mỗi booking thuộc một suất; tổng tiền và hủy đúng. Chỉ kiểm thử confirm ở Domain; chưa triển khai Payment hoặc Ticket.

### Mốc 11 — Payment

Phụ trách chính: **Duy Khánh**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Nối kết quả thanh toán với Booking.confirm(); review hợp đồng và tích hợp.
- **Bảo Long:** Nối cập nhật SeatReservation sau thanh toán; kiểm thử giữ/đặt ghế.
- **Duy Anh:** Viết test luồng thanh toán, lỗi và yêu cầu lặp theo hợp đồng V1.
- **Duy Khánh:** Triển khai Payment, FakePaymentGateway và PayBooking handler; kiểm tra ownership và PENDING.

**Điều kiện nghiệm thu:** Thanh toán thành công lưu Payment SUCCESS và xác nhận Booking; trạng thái ghế nhất quán; từ chối sai chủ/trạng thái. Chưa triển khai Ticket.

### Mốc 12 — Ticket

Phụ trách chính: **Duy Anh**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Review việc tích hợp phát vé với Booking và nghiệm thu.
- **Bảo Long:** Viết test vé đúng ghế, đúng suất và đủ số lượng booking item.
- **Duy Anh:** Triển khai Ticket, phát hành và truy vấn vé, mã vé và trạng thái.
- **Duy Khánh:** Nối gọi phát hành vé sau thanh toán trong handler Payment; kiểm thử đường đi không thành công.

**Điều kiện nghiệm thu:** Mỗi booking item có một vé sau thanh toán thành công; không phát vé khi chưa đủ điều kiện hoặc phát trùng.

### Mốc 13 — Ownership / Authorization

Phụ trách chính: **Duy Khánh**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Rà soát và hoàn thiện quyền xem/hủy Booking; kiểm thử tích hợp.
- **Bảo Long:** Rà soát quyền Cart và giữ/nhả ghế; bổ sung trường hợp truy cập chéo.
- **Duy Anh:** Rà soát quyền truy vấn Ticket qua chủ Booking; bổ sung test truy cập chéo.
- **Duy Khánh:** Điều phối quy tắc CurrentUser/Customer; hoàn thiện quyền Payment và bộ kịch bản hai khách hàng.

**Điều kiện nghiệm thu:** GetBooking, CancelBooking, PayBooking, GetTicket kiểm tra ownership tại Application; khách thứ nhất không thao tác tài nguyên của khách thứ hai.

### Mốc 14 — REST API hoàn chỉnh

Phụ trách chính: **Nguyễn Trung Nghĩa**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Chốt quy ước HTTP/DTO/lỗi; hoàn thiện BookingController, exception chung và cấu hình.
- **Bảo Long:** Hoàn thiện controller/DTO Cinema/Hall/Seat và Cart.
- **Duy Anh:** Hoàn thiện controller/DTO Movie, Screening và Ticket.
- **Duy Khánh:** Hoàn thiện controller/DTO Identity và Payment, nối security adapter.

**Điều kiện nghiệm thu:** API V1 đầy đủ và nhất quán; controller gọi use case; HTTP tests đạt với adapter hiện có.

### Mốc 15 — Persistence

Phụ trách chính: **Nguyễn Trung Nghĩa**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Chốt DB/schema/transaction, migration tổng và cấu hình; triển khai adapter Booking.
- **Bảo Long:** Triển khai mapping/adapter Cinema/Hall/Seat, Cart và SeatHold/SeatReservation.
- **Duy Anh:** Triển khai mapping/adapter Movie, Screening và Ticket.
- **Duy Khánh:** Triển khai persistence Identity/Customer và Payment.

**Điều kiện nghiệm thu:** Adapter JPA thay adapter tạm; migration, khóa và transaction đúng; dữ liệu đọc lại được sau khởi động lại; test persistence đạt.

### Mốc 16 — Testing

Phụ trách chính: **Nguyễn Trung Nghĩa**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Điều phối kiểm thử hồi quy và toàn luồng; sửa lỗi Booking/tích hợp.
- **Bảo Long:** Hoàn thiện bộ test rạp/ghế/giỏ/hold và sửa lỗi thuộc phạm vi.
- **Duy Anh:** Hoàn thiện bộ test Movie/Screening/Ticket và sửa lỗi thuộc phạm vi.
- **Duy Khánh:** Hoàn thiện bộ test Identity/Payment/Ownership và sửa lỗi thuộc phạm vi.

**Điều kiện nghiệm thu:** Toàn bộ test bắt buộc đạt, không để rỗng hoặc bỏ qua; luồng đăng nhập → chọn ghế → checkout → payment → ticket đạt.

### Mốc 17 — Phạm vi công nghệ V1 và Phase 2

Phụ trách chính: **Nguyễn Trung Nghĩa**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Chốt báo cáo giới hạn V1 và danh sách nâng cấp Phase 2.
- **Bảo Long:** Ghi nhận giới hạn SeatHold, thời hạn và concurrency; nêu hướng nâng cấp.
- **Duy Anh:** Ghi nhận giới hạn truy vấn, lịch chiếu và phát vé; nêu hướng nâng cấp.
- **Duy Khánh:** Ghi nhận identity/payment giả và giới hạn quyền truy cập; nêu hướng nâng cấp.

**Điều kiện nghiệm thu:** Tài liệu phân biệt rõ chức năng V1 đã có và công việc Phase 2 chưa triển khai; không bắt đầu code Phase 2.

### Mốc 18 — Roadmap code và bàn giao V1

Phụ trách chính: **Nguyễn Trung Nghĩa**. Trạng thái: **Chưa bắt đầu**.

- **Nghĩa:** Đối chiếu roadmap M0–M12; hoàn thiện Docker, hướng dẫn chạy và nghiệm thu V1.
- **Bảo Long:** Chuẩn bị dữ liệu demo, chạy benchmark đặt ghế và ghi điều kiện/kết quả đo.
- **Duy Anh:** Tổng hợp OpenAPI và ví dụ request/response từ API đã hoàn thành.
- **Duy Khánh:** Kiểm tra demo đăng nhập/thanh toán/quyền truy cập, cấu hình và hướng dẫn trên môi trường mới.

**Điều kiện nghiệm thu:** Roadmap và tài liệu bàn giao đầy đủ; chạy được Docker và demo toàn luồng; có OpenAPI và baseline benchmark. Các mục Docker/OpenAPI/benchmark đối chiếu M12 trong roadmap.

## 3. Phân chia file và hạn chế xung đột

Các nhóm chức năng được duy trì để thành viên tích lũy hiểu biết xuyên suốt:

- **Nghĩa:** kiến trúc chung, Booking và cấu hình tích hợp.
- **Bảo Long:** Cinema/Hall/Seat, Cart, SeatHold và SeatReservation.
- **Duy Anh:** Movie, Screening và Ticket.
- **Duy Khánh:** Identity/Customer, Payment và security adapter.

Đây là phạm vi duy trì mã nguồn, không cho phép triển khai trước mốc.
Ví dụ, khi cả nhóm ở mốc 10, Duy Anh viết test Booking theo phân công;
chưa triển khai Ticket của mốc 12.

Trước mỗi mốc, Nghĩa ghi rõ file/package sản phẩm và file test của từng người.
Mỗi file chỉ có một người sửa chính trong cùng lượt tích hợp.
Test hỗ trợ được tách thành các lớp theo nội dung để tránh cùng sửa một lớp test.

Các chữ ký port, DTO và hành vi Domain cần thống nhất trước khi triển khai.
Phần định nghĩa dùng chung được review và ghép trước; các nhánh phụ thuộc
cập nhật từ `main` rồi tiếp tục. Fake port hỗ trợ kiểm thử độc lập trong
cùng mốc, nhưng nghiệm thu phải kiểm thử lại với implementation đã tích hợp.

Nghĩa quản lý `pom.xml`, `UseCaseConfiguration`, cấu hình ứng dụng,
`GlobalExceptionHandler`, migration tổng và README.
Thành viên ghi yêu cầu ghép nối trong PR để Nghĩa tích hợp.
Các port theo chức năng vẫn thuộc người phụ trách tương ứng;
thay đổi ảnh hưởng người khác phải được thống nhất trước.

Ví dụ phân chia mốc 11:

- Duy Khánh sửa Payment và PayBooking handler.
- Nghĩa sửa Booking khi cần; cung cấp hành vi confirm cho Duy Khánh gọi.
- Bảo Long sửa adapter SeatHold/SeatReservation; cung cấp thao tác cập nhật ghế.
- Duy Anh viết lớp test thanh toán riêng.
- Duy Khánh thực hiện lời gọi Booking và SeatReservation trong handler do Duy Khánh sở hữu.
  Các thành viên khác không đồng thời chỉnh sửa handler này.

Ở mốc 15, mỗi người bàn giao schema/mapping của nhóm chức năng mình.
Nghĩa quản lý thứ tự migration và các khóa ngoại liên phân hệ.

## 4. Quy trình nhánh và review

1. Nghĩa xác nhận mốc trước đã nghiệm thu và công bố phạm vi mốc hiện tại.
2. Chốt hợp đồng, danh sách file, người phụ trách và hạn bàn giao.
3. Tạo nhánh cá nhân từ `main` mới nhất theo mẫu
   `feature/<thanh-vien>/moc-<so>-<noi-dung>`.
4. Triển khai phần việc của mốc hiện tại, viết test và commit nhỏ.
   Công việc phụ thuộc chỉ bắt đầu sau khi đầu vào cần thiết đã sẵn sàng.
5. Thành viên push lên nhánh của mình và tạo PR vào `main`.
   PR ghi rõ mốc, file thay đổi và mục đích, kết quả test, phụ thuộc,
   thay đổi API/schema và phần việc còn thiếu.
6. Nghĩa review và ghép các PR theo thứ tự phụ thuộc.
   Thành viên cập nhật nhánh, xử lý xung đột và chạy lại test trước khi ghép.
   PR của Nghĩa được một thành viên khác review trước khi Nghĩa ghép.
7. Khi tất cả phần việc đã ghép, chạy build và kiểm thử chung trên `main`.
8. Nghĩa xác nhận nghiệm thu, cập nhật tiến độ rồi mới mở mốc tiếp theo.

Ví dụ cả nhóm cùng làm mốc 10:

- `feature/nghia/moc-10-booking`
- `feature/bao-long/moc-10-cart-hold-integration`
- `feature/duy-anh/moc-10-checkout-tests`
- `feature/duy-khanh/moc-10-ownership-tests`

Nếu còn lỗi tích hợp, cả nhóm tiếp tục xử lý trong mốc hiện tại.
Thành viên đã xong hỗ trợ review, test hoặc tài liệu; không chuyển trước
sang mốc sau. Không tự ghép nhánh cá nhân vào `main`.

Quy trình này chưa tạo nhánh, PR hoặc cấu hình bảo vệ nhánh trên dịch vụ Git.

## 5. Điều kiện chuyển mốc

Mỗi mốc chỉ được đánh dấu hoàn thành khi tất cả điều kiện sau đạt:

- [ ] Nghĩa hoàn thành phần việc.
- [ ] Bảo Long hoàn thành phần việc.
- [ ] Duy Anh hoàn thành phần việc.
- [ ] Duy Khánh hoàn thành phần việc.
- [ ] Tất cả PR của mốc đã review và ghép vào `main`.
- [ ] Build, architecture tests và các test liên quan đạt trên `main`.
- [ ] Kiểm thử tích hợp đạt tiêu chí nghiệm thu của mốc.
- [ ] Không có test bắt buộc bị bỏ qua hoặc để rỗng.
- [ ] Tài liệu, hạn chế đã biết và kết quả kiểm thử được cập nhật.
- [ ] Nghĩa xác nhận cho phép bắt đầu mốc tiếp theo.

Đối với mốc tài liệu 17, nghiệm thu tập trung vào tính đầy đủ và chính xác
của báo cáo; không tạo test phần mềm chỉ để kiểm tra thay đổi văn bản.
Các kết quả kiểm thử V1 đã được nghiệm thu phải được giữ nguyên.

## 6. Trạng thái và bước tiếp theo

- **Đã hoàn thành:** mốc 1–4, do Nguyễn Trung Nghĩa thực hiện.
- **Mốc tiếp theo:** mốc 5 — Cinema / Hall / Seat, Bảo Long phụ trách chính.
- **Đang triển khai:** mốc 5, phần Web độc lập của Nghĩa.
- **Chưa bắt đầu:** mốc 6–18.
- Hạn bàn giao được chốt tại đầu mỗi mốc theo thời gian của các thành viên.
- Lưu kết quả nghiệm thu và liên kết PR/commit tại
  [Tiến độ Cinema V1](cinema-v1-progress.md).

Trình tự bắt buộc: **hoàn thành phần việc → review → ghép main →
kiểm thử chung → nghiệm thu → chuyển mốc**.
