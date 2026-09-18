# M1 — Bản giao việc Duy Khánh

Vai trò: repository in-memory, tính duy nhất nguyên tử và dữ liệu nghiệm thu.
Trạng thái: kế hoạch giao việc, chưa triển khai.
Hợp đồng: [đặc tả M1](m1-specification.md), mục 3–5.
Quy tắc mục 3 đã được Nghĩa xác nhận ngày 2026-09-18.

## 1. Phạm vi file

Không sửa Domain, port, handler, controller hoặc cấu hình Spring.
Sau khi PR hợp đồng của Bảo Long ghép main, thêm các file dưới đây.

Trong src/main/java/cinema/infrastructure/persistence/inmemory/:

- InMemoryCinemaRepository.java implements CinemaRepository.
- InMemoryHallRepository.java implements HallRepository.
- InMemorySeatRepository.java implements SeatRepository.

Mỗi lớp có constructor public không tham số.
Không thêm @Repository/@Component; Nghĩa đăng ký bean trong cấu hình M1
để tránh tạo hai instance cho cùng một port.

## 2. InMemoryCinemaRepository

Thêm và implement:

- Optional<Cinema> findById(String id).
- List<Cinema> findAll().
- void save(Cinema cinema).

Dùng Map<String, Cinema> riêng của instance.
save chỉ thêm mới; ID đã có → ConflictException.
findAll trả snapshot sắp theo ID, không trả values() trực tiếp.

## 3. InMemoryHallRepository

Thêm và implement:

- Optional<Hall> findById(String id).
- List<Hall> findByCinemaId(String cinemaId).
- boolean existsByCinemaIdAndName(String cinemaId, String name).
- void save(Hall hall).

save từ chối ID trùng và cặp (cinemaId, name) trùng theo quy tắc đã chốt.
Tên Hall đã được Domain trim; repository không áp chuẩn hóa khác Domain.
Cùng tên nhưng khác Cinema được phép.
findByCinemaId sắp theo name rồi ID, trả snapshot.

## 4. InMemorySeatRepository

Thêm và implement:

- Optional<Seat> findById(String id).
- List<Seat> findByHallId(String hallId).
- boolean existsByHallIdAndRowAndNumber(String hallId, String row, int number).
- void save(Seat seat).

save từ chối ID trùng và cặp (hallId, row, number) trùng.
Row đã được Domain chuẩn hóa; repository so sánh đúng giá trị đó.
Cùng row/number ở Hall khác được phép.
findByHallId sắp theo row, number rồi ID.

Không giữ/đặt ghế, không kiểm tra Screening và không thêm Seat.status.
Repository cha được Application kiểm tra; adapter không tự gọi adapter khác.

## 5. Tính nguyên tử

V1 có thể dùng synchronized trên tất cả thao tác của từng repository.
Trong save, kiểm tra ID, kiểm tra khóa nghiệp vụ và ghi map phải nằm trong
cùng vùng khóa. Các lần đọc cũng phải an toàn khi chạy cùng thao tác ghi.

Không dùng exists rồi put bên ngoài khóa; ConcurrentHashMap đơn lẻ
không bảo vệ đồng thời unique theo ID và theo vị trí.
Khi thất bại, dữ liệu và chỉ mục phụ phải giữ nguyên.
Không dùng static map vì test/instance khác có thể làm nhiễm dữ liệu.

Chấp nhận giới hạn dữ liệu trong một JVM và mất khi khởi động lại.
Không tuyên bố có transaction liên repository hoặc khóa phân tán.

## 6. File kiểm thử cần thêm

Trong src/test/java/cinema/infrastructure/persistence/inmemory/:

- InMemoryCinemaRepositoryTest.java:
  saveAndFind, missingIdReturnsEmpty, duplicateIdRejected,
  findAllSortedSnapshot.
- InMemoryHallRepositoryTest.java:
  findByCinemaFiltersAndSorts, duplicateIdRejected,
  duplicateNameWithinCinemaRejected, sameNameAcrossCinemasAllowed,
  concurrentDuplicateNameAllowsOneInsert.
- InMemorySeatRepositoryTest.java:
  findByHallFiltersAndSorts, duplicateIdRejected,
  duplicatePositionRejected, samePositionAcrossHallsAllowed,
  concurrentDuplicatePositionAllowsOneInsert, returnedListDoesNotExposeStorage.

Đây là tên test dự kiến; giữ đầy đủ hành vi dù điều chỉnh tên.

Test cạnh tranh dùng ExecutorService và CountDownLatch hoặc barrier;
hai tác vụ bắt đầu cùng đợt, dùng ID khác nhau nhưng cùng khóa nghiệp vụ.
Kiểm tra đúng một thành công, một ConflictException, chỉ một bản ghi được lưu.
Có timeout và shutdown executor; không dùng Thread.sleep để giả đồng thời.

Tạo repository mới cho mỗi test; không dựa vào thứ tự chạy test.

## 7. Dữ liệu mẫu và bàn giao

Thêm docs/m1-venue-demo.http:

- POST Cinema mẫu.
- Hướng dẫn lấy id từ response và gán cinemaId.
- POST Hall, lấy hallId; POST Seat NORMAL/VIP/COUPLE.
- GET danh sách/chi tiết theo các endpoint trong đặc tả.
- Thử ghế trùng để nhận 409 và Hall không có để nhận 404.
- Ghi cách chạy profile venue-dev và việc dữ liệu mất khi restart.

Không thêm startup seeder hoặc tự tạo dữ liệu mỗi lần ứng dụng khởi động.
Không thêm demo HTTP chứa ID giả được trình bày như ID có thật.
Nghĩa kiểm tra lại file này sau khi Web được tích hợp.

## 8. Điều kiện hoàn thành

Nhánh: feature/duy-khanh/moc-5-inmemory.

- Ba adapter thực hiện đúng port, độc lập Spring/JPA.
- Test repository và hai trường hợp cạnh tranh đạt.
- Không sửa JpaSeatRepositoryAdapter; thay đổi compile skeleton do Long phụ trách.
- Gửi PR gồm code, test và demo; Nghĩa ghép và chạy tích hợp.

