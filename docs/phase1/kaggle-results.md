# Pha 1 — Kết quả kiểm thử Kaggle CPU

Ngày chạy: **2026-10-10**. Backend được kiểm thử tại commit `a51fb7f496f599243415d797f283baa2ecdff8bd`. Báo cáo này tổng hợp ba file JSON người thực hiện xuất từ notebook Kaggle; không phải một lần chạy lại tại máy phát triển.

## Môi trường và phương pháp

- Kaggle Linux CPU: 4 CPU logic, `MemTotal: 32869448 kB` (khoảng 31,35 GiB); Java 25.0.4.1, MySQL 8.0.46 trên Ubuntu 24.04.
- Backend profile `mysql`, MySQL và công cụ tạo tải Python chạy trong cùng môi trường Kaggle, gọi HTTP qua loopback. Chúng chia sẻ CPU/RAM; đây không phải phép đo từ một máy phát tải độc lập.
- Khởi động trực tiếp bằng Java/MySQL; không chạy Docker trong notebook. Docker đã được kiểm tra riêng ở [M12](../m12/acceptance.md).
- Dữ liệu đọc: một phim, một phòng 30 ghế, một suất chiếu dành cho tải; danh mục từ smoke vẫn còn trong database.
- Mỗi người dùng đọc tuần tự `GET /movies`, `GET /screenings?movieId=...`, `GET /screenings/{id}/available-seats`, rồi nghỉ 1 giây. Dùng kết nối tái sử dụng, không tự retry; timeout request 10 giây.
- Warm-up: 5 người dùng trong 15 giây, loại khỏi bảng kết quả. Các mức đo: 10, 25, 50; mỗi mức khoảng 60 giây, bao gồm thời gian hoàn tất request đang chạy.
- Tiêu chí khảo sát do nhóm chọn trước lần đo: lỗi dưới 1% và p95 từng endpoint dưới 2.000 ms. Đây không phải ngưỡng bắt buộc do giảng viên quy định.

## Tải đọc

| Người dùng đồng thời | Thời gian (s) | Requests | Requests thành công/s | p95 phim (ms) | p95 suất chiếu (ms) | p95 ghế (ms) | Lỗi |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 10 | 60,02 | 1.413 | 23,54 | 14,62 | 15,38 | 544,06 | 0% |
| 25 | 60,16 | 3.204 | 53,25 | 15,81 | 14,55 | 943,58 | 0% |
| 50 | 60,80 | 3.614 | 59,44 | 79,34 | 66,11 | 2.105,67 | 0% |

Tổng 8.231 requests đo chính thức, tất cả HTTP 200. Backend vẫn chạy sau mỗi mức. p50/p99, trạng thái HTTP và warm-up nằm trong [JSON tải đọc](evidence/cinema-read-load-1791623940153405311.json).

**25 là mức cao nhất đã đo đạt tiêu chí.** Tại 50 người dùng, API đọc ghế vượt ngưỡng p95 nên bộ đo dừng đúng điều kiện; mức 100 chưa chạy trong lượt này. Không suy ra giới hạn tối đa là 25 vì chưa đo khoảng 26–49. API ghế biểu hiện chậm nhất; chưa có profiling để kết luận nguyên nhân. Các lần thử trước bị sai đường dẫn hoặc backend dừng không được ghép vào bảng này.

## Nghiệp vụ và concurrency

| Kịch bản | Kết quả |
| --- | --- |
| GET/POST thiếu token, CUSTOMER gọi thao tác ADMIN | Bị từ chối theo contract |
| Checkout → payment → booking → ticket | Booking CONFIRMED, vé VALID |
| Khách khác đọc vé hoặc trả tiền booking | Bị từ chối |
| Payment retry | Giữ nguyên vé; DB có 1 booking confirmed, reservation, ticket và payment thành công |
| 30 khách, ghế riêng; tối đa 5 khách đồng thời | 30 booking khác nhau; đúng 30 booking CONFIRMED, reservation, ticket, payment và payment thành công |
| 10 khách tranh một ghế, đồng bộ bắt đầu bằng barrier | 1 thành công, 9 HTTP 409; đúng 1 bản ghi ở mỗi nhóm kiểm tra DB |

Chi tiết: [smoke](evidence/cinema-smoke-1791623820788528717.json), [đặt vé đồng thời](evidence/cinema-write-load-1791624608923200157.json). Đây là kiểm tra số lượt hữu hạn; không có phép đo throughput/latency đặt vé kéo dài. Số worker/barrier đã được đối chiếu với [notebook được cung cấp](../../benchmarks/kaggle/cinema-phase-1-kaggle-cpu-clean.ipynb); JSON đặt vé lưu outcomes và số bản ghi, không lưu cấu hình executor.

## Bằng chứng và giới hạn

- Ba JSON được giữ nguyên byte; [manifest SHA-256](evidence/manifest.json) giúp đối chiếu bản sao. Không có trường chứa mật khẩu/token trong các file nhập. UUID trong báo cáo là dữ liệu thử.
- Đã bổ sung [notebook và hướng dẫn tái lập](../../benchmarks/kaggle/README.md). Ba báo cáo nhúng trong notebook gốc khớp SHA-256 với JSON đã nhập. Bản repo xóa output cũ, thay các đường dẫn tải cố định bằng biến, bật Internet trong metadata; mã đo tải và các assertion nghiệp vụ giữ nguyên. [Hồ sơ nhập notebook](evidence/notebook-import.json) ghi hash bản gốc/bản sạch; chưa chạy lại bản sạch.
- MySQL thực chạy ở Kaggle là **8.0.46**, khác **8.4** của Docker local. Migrations V1–V3 đã được xác nhận qua output notebook; ba JSON không chứa riêng bảng lịch sử migration.
- Maven 787 tests, 0 failures/errors, 8 skips là bằng chứng M12 trước đó, không phải số test chạy trên Kaggle. Notebook build dùng `-DskipTests`, rồi chạy các kiểm tra HTTP/DB nói trên.
- Một lượt đo, dữ liệu nhỏ, tải có thời gian nghỉ và công cụ đo chia sẻ tài nguyên: không cam kết năng lực production hoặc so sánh trực tiếp với benchmark Docker local.
- FakePaymentGateway, chưa có hold TTL; failure window giữa gateway thành công và database commit vẫn tồn tại. Không tối ưu hoặc thay đổi backend để làm đẹp kết quả.
