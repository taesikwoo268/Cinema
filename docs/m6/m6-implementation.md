# M6 Implementation

> Hồ sơ implementation M6 tại thời điểm triển khai. Checkout/cart concurrency và JPA transactions được bổ sung trong Package 1–2; xác thực Bearer trong Package 3. Xem [tiến độ hiện tại](../cinema-v1-progress.md).

## 1. Scope

M6 triển khai luồng **Checkout + Booking** trên nền Cart và SeatHold của M5.

Các chức năng chính:

- checkout Cart hiện tại thành Booking;
- hỗ trợ Cart chứa ghế của nhiều Screening;
- tạo một Booking riêng cho mỗi Screening;
- hủy Booking;
- xem danh sách Booking của Customer hiện tại;
- xem chi tiết một Booking;
- kiểm tra ownership ở Application layer;
- cung cấp Booking repository in-memory;
- expose Booking HTTP API trong profile `venue-dev`.

Payment chưa thuộc M6. Booking sau checkout được tạo ở trạng thái `PENDING`.

---

## 2. Domain

M6 hoàn thiện hai thành phần domain chính:

```text
Booking
BookingItem
```
