# M5 Acceptance

## Verification

M5 đã được kiểm tra bằng:

- Domain tests;
- Application handler tests;
- In-memory repository/service tests;
- CartController tests;
- full Maven regression;
- `git diff --check`.

Các checkpoint trong quá trình triển khai đều đạt BUILD SUCCESS trước khi đóng M5.

## Accepted Behaviors

Đã xác minh:

- add seat hợp lệ;
- authentication và Customer ownership;
- Screening tồn tại;
- Seat tồn tại;
- Seat thuộc Hall của Screening;
- booked seat bị từ chối;
- hold conflict bị từ chối;
- Cart không duplicate seat;
- remove seat;
- release hold khi remove;
- clear Cart;
- release các hold khi clear;
- get Cart của Customer hiện tại;
- map Money sang CartItemView;
- HTTP controller delegation.

## Exit Criteria

M5 hoàn thành trong phạm vi:

- Cart Domain;
- Cart Application;
- simple SeatHold;
- seat reservation check;
- in-memory Infrastructure;
- venue-dev wiring;
- Cart HTTP API;
- tests.

Deferred:

- Redis SeatHold;
- JPA Cart persistence;
- database transaction;
- distributed concurrency.