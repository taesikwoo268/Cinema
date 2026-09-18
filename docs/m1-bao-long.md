# M1 — Bản giao việc Bảo Long

Vai trò: phụ trách chính Domain và Application của Cinema/Hall/Seat.
Trạng thái: kế hoạch giao việc, chưa triển khai.
Đọc [đặc tả M1](m1-specification.md) trước khi code.
Các quy tắc mục 3 đã được Nghĩa xác nhận ngày 2026-09-18; triển khai theo đặc tả.

## 1. File hiện có được sửa

Đường dẫn gốc: src/main/java/cinema/.

- domain/cinema/Cinema.java:
  giữ constructor Cinema(String id, String name, String address) và getter;
  thêm kiểm tra name, address và trim theo đặc tả đã chốt.
- domain/cinema/Hall.java:
  giữ constructor Hall(String id, String cinemaId, String name);
  kiểm tra cinemaId không blank; kiểm tra và trim name.
- domain/cinema/Seat.java:
  giữ constructor Seat(String id, String hallId, String row, int number, SeatType type);
  kiểm tra hallId, row, number, type; chuẩn hóa row trước khi gán.
- application/common/interfaces/SeatRepository.java:
  giữ findById/findByHallId; thêm existsByHallIdAndRowAndNumber và save
  đúng chữ ký trong đặc tả; ghi hợp đồng save nguyên tử và chỉ thêm mới.
- infrastructure/persistence/adapters/JpaSeatRepositoryAdapter.java:
  chỉ thêm override cho hai phương thức mới để implementation vẫn compile.
  Tiếp tục ném UnsupportedOperationException với thông báo chưa triển khai;
  không đăng ký bean và không triển khai JPA ở M1.

Không sửa BaseEntity, Money, SeatType, SeatView hoặc các lớp Screening.
Dọn import không dùng và thay chú thích skeleton trong ba entity đã hoàn thiện.

## 2. File port và lỗi cần thêm

Trong src/main/java/cinema/application/common/interfaces/:

- CinemaRepository.java: findById, findAll, save.
- HallRepository.java: findById, findByCinemaId, existsByCinemaIdAndName, save.

Trong src/main/java/cinema/application/common/exceptions/:

- ConflictException.java: extends RuntimeException;
  constructor public ConflictException(String message).

Chữ ký, giá trị trả về và lỗi theo mục 4, 6 của đặc tả.
Không thêm Spring annotation, JPA hoặc default method thành công giả vào port.

## 3. File command cần thêm

Trong src/main/java/cinema/application/venues/commands/:

- CreateCinemaCommand.java: record (String name, String address).
- CreateHallCommand.java: record (String cinemaId, String name).
- CreateSeatCommand.java: record (String hallId, String row, Integer number, String type).
- CreateCinemaCommandHandler.java.
- CreateHallCommandHandler.java.
- CreateSeatCommandHandler.java.

Mỗi handler implements UseCase<Command tương ứng, String>.
Thêm public handle(Command input), trả ID vừa tạo.

Constructor injection:

- CreateCinemaCommandHandler(CinemaRepository cinemas, Supplier<String> idGenerator).
- CreateHallCommandHandler(CinemaRepository cinemas, HallRepository halls,
  Supplier<String> idGenerator).
- CreateSeatCommandHandler(HallRepository halls, SeatRepository seats,
  Supplier<String> idGenerator).

Luồng handle:

- Kiểm tra command null và các trường trước khi dereference/unbox.
- Type null hoặc ngoài NORMAL/VIP/COUPLE → ValidationException.
- Tìm cha; không có → NotFoundException; không lưu tài nguyên con.
- Sinh ID qua supplier; gọi constructor Domain.
- Catch riêng DomainException khi tạo entity, chuyển thành ValidationException.
- Kiểm tra trùng bằng giá trị đã chuẩn hóa từ entity.
- Gọi save; để ConflictException từ repository truyền ra.
- Trả getId(); không tạo HTTP response ở Application.

Không lặp lại toàn bộ invariant của Domain trong handler.
Kiểm tra trước dành cho cấu trúc input, parse enum và tham chiếu cha.
Domain vẫn bảo vệ constructor khi được gọi trực tiếp.

## 4. File query cần thêm

Trong src/main/java/cinema/application/venues/queries/:

- GetCinemasQuery.java: record không tham số.
- GetCinemaQuery.java: record (String id).
- GetHallsQuery.java: record (String cinemaId).
- GetHallQuery.java: record (String id).
- GetSeatsQuery.java: record (String hallId).
- GetCinemasQueryHandler.java.
- GetCinemaQueryHandler.java.
- GetHallsQueryHandler.java.
- GetHallQueryHandler.java.
- GetSeatsQueryHandler.java.

Constructor và kiểu kết quả theo mục 5 đặc tả.
Mỗi handler thêm handle(Query input):

- Validate input/ID trước khi gọi repository.
- GetCinema/GetHall: thiếu tài nguyên → NotFoundException.
- GetHalls: kiểm tra Cinema tồn tại trước khi findByCinemaId.
- GetSeats: kiểm tra Hall tồn tại trước khi findByHallId.
- Danh sách rỗng hợp lệ khi cha tồn tại.
- Map Domain sang DTO bằng getter; giữ thứ tự repository.
- Không trả entity Domain ra Web.

## 5. File DTO cần thêm

Trong src/main/java/cinema/application/venues/models/:

- CinemaView.java:
  record CinemaView(String id, String name, String address).
- HallView.java:
  record HallView(String id, String cinemaId, String name).
- PhysicalSeatView.java:
  record PhysicalSeatView(String id, String hallId, String row, int number, String type).

type của PhysicalSeatView dùng SeatType.name().
Không thêm available, status, giá hoặc thông tin Screening.

## 6. Trình tự và ranh giới bàn giao

PR 1 — hợp đồng:

- Hai port mới, sửa SeatRepository và JpaSeatRepositoryAdapter.
- ConflictException, các record command/query/DTO.
- Main phải compile và các test hiện có đạt sau khi ghép.

PR 2 — hành vi:

- Invariant Domain và tám handler.
- Constructor đúng đặc tả để Web/test của người khác sử dụng.
- Không tự sửa file test do Duy Anh sở hữu hoặc adapter in-memory của Duy Khánh.

Nhánh: feature/bao-long/moc-5-domain-application.
Tách PR hợp đồng trước khi mở PR implementation; không ghép PR chưa hoàn thiện.
Khi phát hiện test không khớp, thống nhất với tác giả test và Nghĩa,
không sửa test để bỏ qua lỗi.

## 7. Điều kiện hoàn thành

- Constructor Domain bảo vệ dữ liệu; handler không chứa phụ thuộc framework.
- Ba command và năm query hoạt động với fake/in-memory port.
- Domain/application tests của Duy Anh và architecture tests đạt.
- PR liệt kê file, hành vi, cách chạy test và các quyết định đã chốt.
- Nghĩa review và ghép main; không tự chuyển sang mốc 6.

