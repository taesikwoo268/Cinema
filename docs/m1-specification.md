# Đặc tả M1 — Cinema / Hall / Seat

Ngày lập: 2026-09-18. Mốc 5 trong kế hoạch 18 mốc; mã kỹ thuật M1.
Trạng thái: quy tắc M1 đã được Nguyễn Trung Nghĩa duyệt ngày 2026-09-18;
đang triển khai phần Web độc lập, chưa hoàn thành M1.

## 1. Mục tiêu và hiện trạng

M1 cung cấp dữ liệu rạp → phòng → ghế để Screening sử dụng ở mốc sau.
Luồng nghiệm thu: tạo Cinema, tạo Hall thuộc Cinema, tạo Seat thuộc Hall,
sau đó truy vấn lại qua Application và HTTP.

Code hiện tại đã có Cinema, Hall, Seat, SeatType và kiểm tra ID trong BaseEntity.
Các entity chưa kiểm tra luật riêng. SeatRepository chỉ có findById/findByHallId.
Chưa có CinemaRepository, HallRepository hoặc use case quản lý địa điểm.
JpaSeatRepositoryAdapter còn là skeleton. Profile mặc định vẫn chặn toàn bộ request;
profile venue-dev đã có allowlist method/path M1, nhưng controller nghiệp vụ và
cấu hình nối handler/repository chưa được tích hợp.

M1 dùng repository in-memory. Dữ liệu mất khi khởi động lại.
Chưa triển khai JPA/migration, sửa/xóa địa điểm, tạo ghế hàng loạt,
Screening, giá ghế, booking, JWT hoặc phân quyền ADMIN thật.

## 2. Ràng buộc nghiệp vụ

Yêu cầu đã có trong kế hoạch:

- Cinema.name và Hall.name không null, rỗng hoặc toàn khoảng trắng.
- Seat.row không null, rỗng hoặc toàn khoảng trắng; Seat.number > 0.
- Một vị trí (hallId, row, number) chỉ có một Seat.
- Ghế cùng row/number ở hai Hall khác nhau được phép tồn tại.
- SeatType gồm NORMAL, VIP, COUPLE.
- Seat là ghế vật lý, không có trạng thái booked/held/available.
- Cinema/Hall/Seat tham chiếu nhau bằng ID; không tạo aggregate chứa cả hệ thống.

Điều kiện bổ sung cho hợp đồng M1:

- Hall.cinemaId, Seat.hallId không null/blank; Seat.type không null.
- Hall chỉ được tạo khi Cinema tồn tại.
- Seat chỉ được tạo khi Hall tồn tại.
- ID được sinh ở Application, không lấy ID mới từ request tạo tài nguyên.
- Domain kiểm tra dữ liệu của entity; Application phối hợp tra cứu và kiểm tra trùng.
- Repository phải bảo đảm kiểm tra và ghi unique nguyên tử.

## 3. Quy tắc đã duyệt — 2026-09-18

Nguyễn Trung Nghĩa đã xác nhận toàn bộ các quy tắc dưới đây.
Bốn bản giao việc và các kiểm thử sử dụng thống nhất các quy tắc này.
Nếu cần thay đổi, trưởng nhóm xác nhận và cập nhật đặc tả cùng test expectation
trước khi sửa code phụ thuộc.

- Address: bắt buộc không null/blank; trim khi lưu.
- Tên Cinema/Hall: trim khi lưu; chưa chuẩn hóa chữ hoa/thường.
- Tên Hall: unique trong cùng Cinema sau trim, phân biệt hoa/thường.
  Ràng buộc này được xác nhận áp dụng trong M1.
- Seat.row: trim rồi uppercase bằng Locale.ROOT; a, A và " A " cùng một hàng.
  Không tự giới hạn hàng ghế chỉ còn A–Z.
- COUPLE: một Seat với loại COUPLE; chưa suy ra hai chỗ hoặc tự tạo hai Seat.
- ID tham chiếu: kiểm tra blank, tra đúng giá trị; không trim/đổi ID.
- Mở HTTP M1 qua profile phát triển venue-dev; cấu hình mặc định vẫn deny-all.
- Phạm vi độ dài chuỗi: chưa áp giới hạn VARCHAR của schema đề xuất ở M1.
  Chốt giới hạn trước khi chuyển sang persistence tại mốc 15.

Không thêm unique Cinema.name hoặc giới hạn số Hall/Seat khi chưa có yêu cầu.

## 4. Hợp đồng repository

Đường dẫn chung: src/main/java/cinema/application/common/interfaces/.

Thêm CinemaRepository.java:

```java
Optional<Cinema> findById(String id);
List<Cinema> findAll();
void save(Cinema cinema);
```

Thêm HallRepository.java:

```java
Optional<Hall> findById(String id);
List<Hall> findByCinemaId(String cinemaId);
boolean existsByCinemaIdAndName(String cinemaId, String name);
void save(Hall hall);
```

Sửa SeatRepository.java, giữ hai phương thức hiện có và bổ sung:

```java
boolean existsByHallIdAndRowAndNumber(String hallId, String row, int number);
void save(Seat seat);
```

Hợp đồng save trong M1:

- Chỉ thêm mới; trùng ID hoặc khóa nghiệp vụ ném ConflictException.
- Kiểm tra unique và ghi dữ liệu phải nguyên tử trong adapter.
- Application có thể kiểm tra trùng trước để báo lỗi sớm, nhưng adapter vẫn kiểm tra lại.
- Không dùng default method trả thành công giả để tránh sửa implementation.
- Single lookup trả Optional.empty khi thiếu; query danh sách trả list rỗng.
- Trả snapshot danh sách; caller không được sửa cấu trúc lưu trữ qua kết quả.
- Thứ tự: Cinema theo ID; Hall theo name rồi ID; Seat theo row, number rồi ID.
  Đây là thứ tự chuỗi thông thường, không phải thứ tự số tự nhiên của nhãn hàng.

Mỗi adapter in-memory là một bean dùng chung trong ứng dụng.
Bảo đảm nguyên tử cho một repository instance; không tuyên bố bảo đảm nhiều JVM.
Dữ liệu được tạo qua Application; adapter không tự gọi repository cha.

## 5. Command, query và DTO

Đặt command/query/handler trong application/venues/commands và application/venues/queries.
Đặt DTO trong application/venues/models.
Mỗi record và mỗi handler là một file riêng.
Các kiểu dưới đây là hợp đồng dùng chung cho triển khai M1.

Commands:

```java
CreateCinemaCommand(String name, String address)
CreateHallCommand(String cinemaId, String name)
CreateSeatCommand(String hallId, String row, Integer number, String type)
```

Dùng Integer để nhận biết number bị thiếu; type dạng String để Web không import Domain.
Application chuyển type thành SeatType, chỉ chấp nhận tên enum đúng chữ hoa.
Mỗi Create...CommandHandler implements UseCase<Command, String>;
handle(Command input) trả ID mới.

Constructor handler:

```java
CreateCinemaCommandHandler(CinemaRepository cinemas, Supplier<String> idGenerator)
CreateHallCommandHandler(CinemaRepository cinemas, HallRepository halls,
                         Supplier<String> idGenerator)
CreateSeatCommandHandler(HallRepository halls, SeatRepository seats,
                         Supplier<String> idGenerator)
```

Supplier<String> được truyền từ cấu hình bằng UUID.randomUUID().toString();
test truyền supplier xác định. Domain không sinh ID.

Queries và constructor handler:

```java
GetCinemasQuery()
GetCinemaQuery(String id)
GetHallsQuery(String cinemaId)
GetHallQuery(String id)
GetSeatsQuery(String hallId)

GetCinemasQueryHandler(CinemaRepository cinemas)
GetCinemaQueryHandler(CinemaRepository cinemas)
GetHallsQueryHandler(CinemaRepository cinemas, HallRepository halls)
GetHallQueryHandler(HallRepository halls)
GetSeatsQueryHandler(HallRepository halls, SeatRepository seats)
```

Các handler implements UseCase<Query, Result> và khai báo handle(Query input).
Kết quả tương ứng: List<CinemaView>, CinemaView, List<HallView>, HallView,
List<PhysicalSeatView>.

DTO:

```java
CinemaView(String id, String name, String address)
HallView(String id, String cinemaId, String name)
PhysicalSeatView(String id, String hallId, String row, int number, String type)
```

Không sửa SeatView hiện có vì DTO đó mang available theo suất chiếu.
M1 không sử dụng GetAvailableSeatsQuery của Screening.

## 6. Quy trình và lỗi Application

CreateCinema: kiểm tra input → sinh ID → constructor Domain → save → trả ID.
CreateHall: kiểm tra input → tìm Cinema → tạo Hall hợp lệ →
kiểm tra tên trùng theo giá trị đã chuẩn hóa → save → trả ID.
CreateSeat: kiểm tra input/type/number → tìm Hall → tạo Seat hợp lệ →
kiểm tra vị trí đã chuẩn hóa → save → trả ID.

- Query một tài nguyên không có: NotFoundException.
- Query danh sách con: kiểm tra cha tồn tại trước.
  Cha có nhưng không có con trả list rỗng; cha không có trả NotFoundException.
- Command/query null hoặc trường đầu vào sai: ValidationException.
- Constructor Domain ném DomainException: handler chuyển thành ValidationException.
- Trùng ID/tên Hall/vị trí ghế: ConflictException; không đổi thành ValidationException.
- Không catch RuntimeException tổng quát rồi biến mọi lỗi thành lỗi dữ liệu.
- Domain không import Application/Spring/JPA.
- Web chỉ biết Application và lỗi Application; không import DomainException.

Thêm application/common/exceptions/ConflictException.java,
constructor public ConflictException(String message).

## 7. HTTP tối thiểu để nghiệm thu

- POST /cinemas: body name, address; trả 201 và CreatedResourceResponse(id).
- GET /cinemas: trả 200 và danh sách CinemaView.
- GET /cinemas/{id}: trả 200 hoặc 404.
- POST /halls: body cinemaId, name; trả 201 và ID.
- GET /cinemas/{id}/halls: trả 200 hoặc 404 nếu Cinema không tồn tại.
- GET /halls/{id}: trả 200 hoặc 404.
- POST /seats: body hallId, row, number, type; trả 201 và ID.
- GET /halls/{id}/seats: trả 200 hoặc 404 nếu Hall không tồn tại.

Lỗi dùng ErrorResponse(code, message) hiện có:

- 400 VALIDATION: input/enum sai, thiếu trường, JSON không hợp lệ.
- 404 NOT_FOUND: tài nguyên hoặc cha không tồn tại.
- 409 CONFLICT: dữ liệu trùng.
- 403: request bị security chặn.

Không buộc trả Location cho Seat vì chưa có GET /seats/{id}.
Chưa thêm endpoint ngoài danh sách trên.

Profile venue-dev cho phép đúng method/path trên, chỉ bỏ qua CSRF cho
ba POST tạo tài nguyên M1; mọi route khác tiếp tục deny-all.
Không mở rộng thành permitAll toàn ứng dụng hoặc tắt CSRF toàn cục.
Profile mặc định giữ nguyên hành vi kiểm thử scaffold.

## 8. Kiểm thử và nghiệm thu

- Domain: null/blank, number <= 0, type null, chuẩn hóa theo quyết định đã chốt.
- Application: cha không có, truy vấn rỗng, tạo thành công, trùng và ánh xạ lỗi.
- Adapter: ID trùng, Hall trùng, Seat trùng; list là snapshot và sắp xếp đúng.
- Cạnh tranh: hai yêu cầu lưu cùng khóa nghiệp vụ, chỉ một thành công,
  yêu cầu còn lại ConflictException, tổng số bản ghi là một.
- HTTP: tạo Cinema → Hall → Seat và đọc lại; 400/404/409; request ngoài M1 bị chặn.
- Test M0, architecture tests và kiểm thử cũ vẫn đạt.
- Test bắt buộc M1 không @Disabled, không thân rỗng.
- Tất cả PR đã ghép main, chạy kiểm thử chung và Nghĩa ký nhận trước mốc 6.

## 9. Bốn bản giao việc và trình tự tích hợp

- [Nguyễn Trung Nghĩa — Web, cấu hình và tích hợp](m1-nguyen-trung-nghia.md).
- [Bảo Long — Domain và Application](m1-bao-long.md).
- [Duy Anh — kiểm thử Domain/Application](m1-duy-anh.md).
- [Duy Khánh — adapter in-memory và test persistence tạm](m1-duy-khanh.md).

Bước 1: đã hoàn thành — Nghĩa xác nhận các quyết định mục 3 ngày 2026-09-18.
Bước 2: Bảo Long bàn giao PR hợp đồng: port, record command/query/DTO,
ConflictException và bổ sung chữ ký cho JpaSeatRepositoryAdapter để code compile.
Bước 3: sau khi PR hợp đồng ghép main, chia bốn nhánh theo bản giao việc.
Các handler constructor được thống nhất ở mục 5; test/Web phụ thuộc handler
chỉ hoàn thiện biên dịch khi implementation của Long đã sẵn sàng.
Không ghép code gọi lớp chưa tồn tại hoặc để main không build được.
Bước 4: ghép Domain/Application, adapter, test, cuối cùng cấu hình/Web.
Bước 5: chạy toàn bộ verify, nghiệm thu và cập nhật tiến độ.

Độc lập ở đây là không sở hữu trùng file, không phải không có phụ thuộc.
Không tạo stub thành công giả hoặc dùng @Disabled để che phần chưa triển khai.

