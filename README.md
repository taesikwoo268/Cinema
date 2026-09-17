# Cinema — Architecture Contract v0.1

Cinema được xây lại theo Clean Architecture. Đã hoàn thiện **M0 — nền kiến trúc**, gồm kiểm tra dữ liệu của `Money`, `BaseEntity` và kiểm thử nền tảng; các chức năng bán vé vẫn ở mức skeleton.

## 1. Cấu trúc

```text
src/main/java/cinema/
├── domain/          Model, value object, enum và chữ ký hành vi
├── application/     Command/query, handler, DTO và ports
├── infrastructure/  Adapter persistence, identity, payment, seat hold và cấu hình
└── web/             Controller, security và xử lý exception

src/test/java/cinema/
├── architecture/
├── domain/
├── application/
└── web/
```

Dependency:

```text
Web → Application → Domain
Infrastructure → Application + Domain
```

Domain dùng Java thuần. Application làm việc với bên ngoài qua ports. Controller không chứa business rule. ArchUnit kiểm tra các ranh giới này.

## 2. Domain đã chốt

```text
Movie → Screening → Seat selection/hold → Cart → Booking → Payment → Ticket
```

Customer liên kết với account qua `userId`; credential thuộc Infrastructure Identity. Seat là ghế vật lý, còn trạng thái giữ/đặt ghế thuộc cặp Screening–Seat.

Cart được chứa nhiều Screening. Khi checkout, mỗi Screening tạo một Booking. Booking dùng BookingItem riêng để giữ snapshot, không dùng lại CartItem.

Application lấy danh tính qua CurrentUser để kiểm tra ownership. Phần nối implementation nằm ở Infrastructure config.

## 3. Chạy và kiểm tra

Dùng JDK 25. Dự án đã có Maven Wrapper:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Nếu Maven trỏ cache vào `C:\.m2` và không ghi được, truyền `-Dmaven.repo.local` với đường dẫn cache có quyền truy cập.

## 4. Phần đã có và phần chưa làm

Ứng dụng khởi động được mà chưa cần database, Redis hay RabbitMQ. Controller chưa có endpoint nghiệp vụ; Security đang chặn toàn bộ request. JWT filter chưa đăng ký, chưa có tài khoản mặc định hay thanh toán hoạt động.

Handler chưa implement sẽ ném `FeatureNotImplementedException`. Domain method và adapter chưa implement sẽ ném `UnsupportedOperationException`. `BaseEntity` từ chối ID null/blank; `Money` từ chối amount/currency null và số tiền âm. Các invariant nghiệp vụ khác còn phải triển khai theo milestone. JPA entity mới có ID, phần mapping còn phải làm tiếp.

Test đang chạy kiểm tra luật nền tảng, dependency, khởi động/security và việc handler báo chưa implement. M0 có 13 domain test và 6 architecture test đạt, không bỏ qua. `BookingWorkflowTest` còn 8 test skeleton bị `@Disabled`, thuộc các bước triển khai sau.

Chi tiết: [Architecture Contract](docs/architecture-contract.md).

Model và các luồng nghiệp vụ: [Thiết kế chi tiết](docs/architecture-contract-source.txt).

Kế hoạch và tiến độ: [Cinema V1](docs/cinema-v1-plan.md) · [Checklist M0–M12](docs/cinema-v1-progress.md).
