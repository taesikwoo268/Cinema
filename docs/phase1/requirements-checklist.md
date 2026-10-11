# Pha 1 — Đối chiếu yêu cầu

Đối chiếu backend `a51fb7f` và bằng chứng Kaggle ngày 2026-10-10. Đây là trạng thái implementation/kiểm thử, không khẳng định đã merge vào main hoặc giảng viên đã nghiệm thu.

| Yêu cầu | Trạng thái / bằng chứng |
| --- | --- |
| REST API, JSON, POST/GET/DELETE | Có: `POST /carts/seats`, `GET /tickets/me`, `DELETE /carts`; [OpenAPI](../openapi/cinema-v1.yaml) |
| OpenAPI/Swagger | OpenAPI có 31 operations đã đối chiếu controller/DTO trong M12; [hướng dẫn](../m12/api.md) |
| Phân tầng API → Nghiệp vụ → Truy cập dữ liệu | [Kiến trúc README](../../README.md#kiến-trúc); Domain/Application độc lập Web/JPA, [architecture tests](../../src/test/java/cinema/architecture/CleanArchitectureTest.java) |
| Repository và ORM/data mapper | Repository ports trong Application, JPA adapters trong Infrastructure; [MySQL](../mysql.md) |
| Đăng nhập | `POST /identity/login`, Bearer token; [smoke Kaggle](evidence/cinema-smoke-1791623820788528717.json) |
| Ít nhất một GET và POST cần xác thực | `GET /tickets/me`, `POST /carts/seats`: thiếu token bị 403; luồng có token đạt |
| Xác thực qua filter, không lặp trong endpoint | [BearerAuthenticationFilter](../../src/main/java/cinema/web/security/BearerAuthenticationFilter.java) được đăng ký trong [MysqlSecurityConfig](../../src/main/java/cinema/web/security/MysqlSecurityConfig.java) |
| Đóng gói Docker | [Dockerfile](../../Dockerfile), [Compose](../../compose.yaml), [nghiệm thu M12](../m12/acceptance.md) |
| GitHub công khai, commit cẩn thận | Chủ dự án đã chuyển public; notebook chạy `git ls-remote` không dùng credentials và clone được `a51fb7f`. Không kiểm tra lại visibility trực tuyến trong đợt nhập báo cáo này |
| README mô tả kiến trúc/đặc tả | [README](../../README.md), OpenAPI và tài liệu liên kết |
| Kiểm thử tải trên Kaggle CPU | Đã thực hiện; [kết quả và giới hạn](kaggle-results.md). 25 người dùng đạt tiêu chí khảo sát; 50 vượt p95 đọc ghế, không lỗi HTTP |

## Việc còn lại để bàn giao

- Đã bổ sung [notebook và hướng dẫn](../../benchmarks/kaggle/README.md); output cũ được xóa, mã đo tải/kiểm tra nghiệp vụ giữ nguyên. Ba JSON nhúng trong bản gốc khớp SHA-256 với bằng chứng đã nhập. Chưa thực thi lại bản notebook đã làm sạch.
- Review tài liệu/bằng chứng và trạng thái CI trước khi chủ dự án quyết định merge. Không tự merge hoặc xóa nhánh.
- Frontend không nằm trong yêu cầu Pha 1. Không cần tối ưu để giấu ngưỡng độ trễ đã đo; báo cáo trung thực kết quả.
