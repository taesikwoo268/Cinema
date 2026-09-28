# M6 Implementation

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