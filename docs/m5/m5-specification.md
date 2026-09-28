# M5 — Cart + Simple SeatHold

## Goal

M5 cung cấp giỏ vé tạm thời trước Checkout:

- thêm ghế vào Cart;
- xóa ghế khỏi Cart;
- xóa toàn bộ Cart;
- xem Cart hiện tại;
- giữ ghế tạm thời bằng SeatHoldService;
- ngăn ghế đã booked hoặc đang được customer khác giữ.

## Domain

Cart gồm:

- id
- customerId
- items

CartItem gồm:

- screeningId
- seatId
- unitPrice

Các luật chính:

- Cart thuộc một Customer.
- Không được duplicate `(screeningId, seatId)` trong cùng Cart.
- Giá không âm.
- Add seat chỉ được thực hiện khi Screening và Seat hợp lệ.
- Seat phải thuộc Hall của Screening.
- Seat đã booked không được thêm.
- Seat đang được customer khác hold không được thêm.

## Application Use Cases

- AddSeatToCart
- RemoveSeatFromCart
- ClearCart
- GetMyCart

## Add Seat Flow

CurrentUser
-> Customer
-> Screening
-> Seat
-> check seat belongs to Hall
-> SeatReservationRepository.isBooked()
-> SeatHoldService.hold()
-> Cart.addItem()
-> CartRepository.save()

## Infrastructure V1

M5 cố tình dùng implementation đơn giản:

- InMemoryCartRepository
- SimpleSeatHoldService
- InMemorySeatReservationRepository

Redis và persistence production nằm ngoài M5.

## Web API

- POST /carts/seats
- DELETE /carts/seats
- DELETE /carts
- GET /carts/me

Các endpoint chỉ active trong profile `venue-dev`.