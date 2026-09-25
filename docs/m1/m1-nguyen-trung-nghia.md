# M1 — Bản giao việc Nguyễn Trung Nghĩa

Vai trò: trưởng nhóm, chốt hợp đồng, Web, cấu hình và nghiệm thu.
Trạng thái: đang triển khai trên nhánh feature/nghia/moc-5-web-integration.
Nguồn thống nhất: [đặc tả M1](m1-specification.md).

## Tiến độ thực hiện — 2026-09-18

- [x] Chốt quy tắc M1.
- [x] Thêm CreateCinemaRequest, CreateHallRequest, CreateSeatRequest và CreatedResourceResponse.
- [x] Tách SecurityConfig mặc định và VenueDevSecurityConfig theo profile.
- [x] Giới hạn route theo method/path; chỉ bỏ qua CSRF cho ba POST M1.
- [x] Thêm xử lý JSON không hợp lệ thành 400 VALIDATION.
- [x] Kiểm thử phần độc lập: VenueWebBoundaryTest (31), DefaultVenueSecurityTest (8),
  VenueDevelopmentContextTest (1) đều đạt.
- [ ] Nhận/review port, DTO, ConflictException và handler của Bảo Long.
- [ ] Nhận/review ba adapter in-memory của Duy Khánh.
- [ ] Thêm VenueConfiguration và ba controller nghiệp vụ.
- [ ] Thêm mapping ConflictException thành 409 khi lớp lỗi được bàn giao.
- [ ] Viết VenueApiIntegrationTest kiểm thử luồng nghiệp vụ thật và nghiệm thu M1.

Kết quả verify: 69 trường hợp, 61 đạt, 8 BookingWorkflowTest skeleton ngoài M1
bị bỏ qua; không failure/error. 13 test nền tảng và 6 architecture tests vẫn đạt.
VenueWebBoundaryTest dùng controller thử nghiệm chỉ trong test để xác minh
security/JSON; không thay thế handler, repository hoặc chứng minh M1 hoàn thành.
VenueDevelopmentContextTest xác minh ứng dụng thật chọn đúng một security chain
khi bật venue-dev. Hiện chưa có endpoint nghiệp vụ M1 hoạt động.

Các file test bổ sung thuộc phạm vi Web của Nghĩa; các phần của Long/Anh/Khánh
chưa được sửa trong nhánh này. Chưa commit, push hoặc ghép main tại thời điểm ghi nhận.

## 1. Chốt đầu vào và điều phối

- Đã xác nhận toàn bộ quy tắc mục 3 đặc tả ngày 2026-09-18.
  Bước tiếp theo: review PR hợp đồng của Long khi được bàn giao.
- Review PR hợp đồng của Long trước khi Khánh/Anh nối implementation.
- Chốt danh sách file của bốn người; mỗi file chỉ một chủ sửa.
- Xác nhận nền M0 đã có trên main.
- Không thay port/DTO/constructor riêng trên nhánh Web; yêu cầu Long cập nhật
  hợp đồng chung và thông báo cả nhóm khi cần đổi.
- Mọi công việc trong bản này chỉ phục vụ M1.

## 2. File cấu hình cần thêm/sửa

Gốc: src/main/java/cinema/.

Thêm infrastructure/config/VenueConfiguration.java:

- @Configuration, @Profile("venue-dev").
- Bean cinemaRepository(): trả InMemoryCinemaRepository.
- Bean hallRepository(): trả InMemoryHallRepository.
- Bean seatRepository(): trả InMemorySeatRepository.
- Bean createCinemaCommandHandler(CinemaRepository).
- Bean createHallCommandHandler(CinemaRepository, HallRepository).
- Bean createSeatCommandHandler(HallRepository, SeatRepository).
- Bean getCinemasQueryHandler(CinemaRepository).
- Bean getCinemaQueryHandler(CinemaRepository).
- Bean getHallsQueryHandler(CinemaRepository, HallRepository).
- Bean getHallQueryHandler(HallRepository).
- Bean getSeatsQueryHandler(HallRepository, SeatRepository).
- Mỗi bean handler gọi đúng constructor trong đặc tả;
  command handler nhận supplier UUID.randomUUID().toString().
- Một instance repository cho một port, dùng lại giữa các handler.

Không cần sửa UseCaseConfiguration vì các handler M1 có cấu hình riêng.
Không sửa CinemaApplication: component scan hiện có đã bao phủ cinema.

Sửa web/security/SecurityConfig.java:

- Đặt @Profile("!venue-dev") để giữ chain deny-all cho profile mặc định.
- Giữ nguyên hành vi của chain hiện có.

Thêm web/security/VenueDevSecurityConfig.java:

- @Configuration, @Profile("venue-dev").
- Bean venueDevSecurityFilterChain(HttpSecurity http).
- Cho phép chính xác các GET/POST M1 tại mục 7 đặc tả.
- Mọi request ngoài danh sách vẫn denyAll.
- Chỉ ignoring CSRF cho POST /cinemas, /halls, /seats.
- Dùng matcher theo cả method và path; không mở toàn bộ prefix /cinemas/**.
- Không yêu cầu JWT hay giả lập ADMIN ở M1.

## 3. File request/response cần thêm

Trong web/requests/:

- CreateCinemaRequest.java:
  record CreateCinemaRequest(String name, String address).
- CreateHallRequest.java:
  record CreateHallRequest(String cinemaId, String name).
- CreateSeatRequest.java:
  record CreateSeatRequest(String hallId, String row, Integer number, String type).

Trong web/responses/:

- CreatedResourceResponse.java:
  record CreatedResourceResponse(String id).

Không dùng SeatType của Domain trong Web.
Request được chuyển sang command; business validation vẫn ở Domain/Application.
Không thêm dependency Bean Validation chỉ để lặp lại kiểm tra trong M1.

## 4. File controller cần thêm

Mỗi controller: @RestController, @Profile("venue-dev"),
constructor injection handler cụ thể, không inject repository hoặc UseCase port.

web/controllers/CinemaController.java:

- @RequestMapping("/cinemas").
- create(CreateCinemaRequest): POST → CreateCinemaCommandHandler → 201 và ID.
- getAll(): GET → GetCinemasQueryHandler → List<CinemaView>.
- getById(String id): GET /{id} → GetCinemaQueryHandler → CinemaView.
- getHalls(String id): GET /{id}/halls → GetHallsQueryHandler → List<HallView>.

web/controllers/HallController.java:

- @RequestMapping("/halls").
- create(CreateHallRequest): POST → CreateHallCommandHandler → 201 và ID.
- getById(String id): GET /{id} → GetHallQueryHandler → HallView.
- getSeats(String id): GET /{id}/seats → GetSeatsQueryHandler → List<PhysicalSeatView>.

web/controllers/SeatController.java:

- @RequestMapping("/seats").
- create(CreateSeatRequest): POST → CreateSeatCommandHandler → 201 và ID.
- Không thêm GET /seats/{id} nếu đặc tả chưa có.

Dùng @RequestBody và @PathVariable tương ứng.
ResponseEntity<CreatedResourceResponse> cho POST; query trả DTO Application.
Controller không dựng entity Domain hoặc tự kiểm tra cha/trùng ghế.

## 5. File xử lý lỗi cần sửa

web/exception/GlobalExceptionHandler.java:

- Thêm conflict(ConflictException): 409, code CONFLICT.
- Thêm malformedRequest(HttpMessageNotReadableException):
  400, code VALIDATION, message rõ ràng không trả stack trace/parser nội bộ.
- Giữ mapping ValidationException → 400, NotFoundException → 404
  và các mapping hiện có.
- Không import/catch DomainException ở Web để giữ architecture tests.

Nếu dữ liệu sai chưa được chuyển từ Domain sang Application đúng hợp đồng,
gửi Long sửa handler; không mở ngoại lệ architecture để né lỗi.

## 6. File HTTP test cần thêm

Gốc: src/test/java/cinema/web/.

VenueApiIntegrationTest.java:

- @SpringBootTest, @AutoConfigureMockMvc, @ActiveProfiles("venue-dev").
- Tạo Cinema → Hall → Seat qua HTTP và dùng ID thật từ response.
- Kiểm tra GET danh sách, chi tiết, danh sách con và trường DTO.
- ExistingParentWithNoChildren: 200 và list rỗng.
- InvalidInput: blank/null/number <= 0/type không hợp lệ → 400.
- MalformedJson: JSON sai cấu trúc → 400.
- MissingParentAndResource: 404.
- DuplicateSeat/Hall: 409 theo quy tắc đã chốt.
- OtherHallSamePosition: tạo thành công.
- Gọi đúng POST M1 không cần CSRF token trong venue-dev.
- GET /movies và route ngoài M1 tiếp tục 403; không mở nhầm phương thức.
- Hai POST ghế cùng vị trí có thể được kiểm tra ở mức tích hợp nếu cần;
  kiểm thử nguyên tử bắt buộc đã thuộc Khánh, không lặp lại không cần thiết.

Dùng tên/ID cha riêng cho từng test để không phụ thuộc dữ liệu test trước.
Không đưa test assert tổng số Cinema toàn context khi dữ liệu dùng chung.

Giữ CinemaApplicationTests.java mặc định không active venue-dev;
test hiện có /movies → 403 phải tiếp tục đạt.
Không sửa test M0/architecture để bỏ qua vi phạm.

## 7. Tài liệu và nghiệm thu

Sửa các file sau sau khi triển khai thực tế:

- README.md: cách chạy venue-dev, API M1 và giới hạn in-memory.
- docs/cinema-v1-progress.md: kết quả test, commit/PR và trạng thái M1.
- docs/team-work-allocation.md: cập nhật mốc 5 khi nghiệm thu.
- docs/m1-specification.md: ghi quyết định đã chốt và sai khác được duyệt.

Không tự sửa docs/m1-venue-demo.http do Khánh sở hữu trong lúc Khánh đang làm;
gửi góp ý để Khánh sửa trên nhánh của mình.

Nhánh: feature/nghia/moc-5-web-integration.

Trình tự ghép: hợp đồng → Domain/Application → adapter → test →
Web/cấu hình → verify toàn bộ.
Có thể ghép test cùng implementation nếu điều đó giúp mỗi commit trên main
đều compile và đạt kiểm thử.

Lệnh nghiệm thu:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=venue-dev'
```

Nếu máy cần cache Maven riêng, truyền -Dmaven.repo.local với đường dẫn của máy.
Chạy verify toàn bộ, ghi số test đạt/bỏ qua và lý do.
Test bắt buộc M1 không bỏ qua; các workflow skeleton ngoài M1 được ghi riêng.
PR của Nghĩa nhờ một thành viên khác review; Nghĩa ghép sau khi đạt yêu cầu.
Chỉ mở mốc 6 sau khi toàn bộ phần việc M1 đã ghép main và nghiệm thu.
