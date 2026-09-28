# M6 — Checkout + Booking

## Goal

M6 chuyển Cart thành Booking và cung cấp lifecycle cơ bản của Booking.

## Domain

Booking gồm:

- id
- customerId
- screeningId
- items
- totalAmount
- status
- createdAt

BookingItem gồm:

- seatId
- price

BookingStatus:

- PENDING
- CONFIRMED
- CANCELLED

Một Booking chỉ thuộc một Screening.

## Domain Rules

- Booking phải có ít nhất một item.
- Booking item không được null.
- BookingItem phải có seatId hợp lệ và price hợp lệ.
- Các item dùng để tính tổng phải cùng currency.
- `PENDING -> CONFIRMED` hợp lệ.
- `PENDING -> CANCELLED` hợp lệ.
- terminal state không được transition lại.

## Checkout Flow

CurrentUser
-> Customer
-> active Cart
-> verify all SeatHolds
-> group CartItems by screeningId
-> create Booking(s) PENDING
-> BookingRepository.save()
-> clear Cart
-> CartRepository.save()
-> return bookingIds

## Booking Use Cases

- CheckoutCart
- CancelBooking
- GetMyBookings
- GetBooking

## Ownership

Customer chỉ được:

- xem Booking của mình;
- cancel Booking của mình.

Ownership được kiểm tra tại Application layer.

## Web API

- POST /bookings/checkout
- POST /bookings/{id}/cancel
- GET /bookings/me
- GET /bookings/{id}

Payment không thuộc M6.