# Cinema Pha 1 — Kaggle CPU

[Notebook](cinema-phase-1-kaggle-cpu-clean.ipynb) chạy backend commit `a51fb7f496f599243415d797f283baa2ecdff8bd`, MySQL và công cụ tải Python trong một phiên Kaggle CPU. [Kết quả đã đo](../../docs/phase1/kaggle-results.md) và [JSON gốc](../../docs/phase1/evidence/manifest.json) được giữ riêng, không phải output mới của bản notebook đã làm sạch.

## Chạy lại

1. Import file `.ipynb` vào Kaggle. Chọn Python, Accelerator **None**, Internet **On**; dùng phiên mới để tránh dữ liệu và tiến trình cũ. Metadata Internet của bản tải về ban đầu là `false`; bản repo đổi thành `true`, vẫn cần kiểm tra giao diện trước khi chạy.
2. Chạy lần lượt từ trên xuống: môi trường → Java 25/MySQL → clone commit → database → build → start/migrations → smoke → tải đọc → concurrency. Notebook cần quyền root và apt packages như môi trường đã đo. Build dùng `-DskipTests`, không thay thế full Maven suite.
3. Dừng khi một ô lỗi. Không chạy lặp các ô tạo dữ liệu, checkout hoặc concurrency trên cùng fixture. Chỉ chạy ở Kaggle riêng cho kiểm thử; notebook tạo database `cinema_kaggle`, tài khoản DB, dữ liệu thử và quyền ADMIN cho tài khoản sinh ngẫu nhiên.
4. Tải ba JSON bằng các ô download: smoke, read-load, write-load. Đường dẫn dựa trên biến của lượt hiện tại, không phụ thuộc timestamp cũ. Lưu notebook và tải file trước khi kết thúc phiên; Quick Save không duy trì biến Python, Java hoặc MySQL.

## Kịch bản và cách đọc kết quả

- Smoke: bảo mật, checkout/payment/ticket, retry và đối chiếu database.
- Tải đọc: warm-up 5 người/15 giây; lần lượt 10/25/50/100 người, mỗi mức 60 giây. Mỗi người đọc ba endpoint rồi nghỉ 1 giây, timeout 10 giây, dùng connection pooling. Dừng tăng tải nếu lỗi >= 1% hoặc p95 bất kỳ endpoint >= 2.000 ms. `stopped_at_threshold` là kết quả đo cần báo cáo, không được sửa thành `completed`.
- Tải ghi hữu hạn: 30 khách/30 ghế, tối đa 5 worker; 10 khách tranh một ghế qua barrier. Kiểm tra cả HTTP và số booking/reservation/ticket/payment trong MySQL. Đây không phải benchmark sức tải đặt vé kéo dài.
- Backend, database và công cụ tải chia sẻ tài nguyên; dữ liệu nhỏ. Gói apt không cố định patch version; JSON ghi Java/MySQL thực tế từng lần. Không so sánh trực tiếp với Docker local hoặc coi đây là năng lực production.

## Nguồn và kiểm tra bản nhập

Notebook gốc do người thực hiện cung cấp, đã chạy ngày 2026-10-10. Ba JSON nhúng trong output gốc khớp byte với bằng chứng trong repo. Bản nhập bỏ cell mẫu Kaggle không dùng, thêm hướng dẫn, xóa output/execution count/metadata từng cell, sửa hai ô download sang biến đường dẫn và bật Internet trong metadata. Giữ nguyên toàn bộ mã đo tải và kiểm tra nghiệp vụ; không sửa backend.

Không phát hiện credential cụ thể trong bản nguồn đã xem; mật khẩu/token được sinh lúc chạy và giữ trong bộ nhớ. Không thêm ô in `APP_ENV`, tài khoản hoặc token. Xem [hash và biến đổi](../../docs/phase1/evidence/notebook-import.json). Bản làm sạch chỉ được kiểm tra cấu trúc/cú pháp; không chạy lại Kaggle, Maven hoặc benchmark trong bước nhập tài liệu.
