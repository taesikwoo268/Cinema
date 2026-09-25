# M3 — Screening

## Goal

M3 quản lý lịch chiếu cơ bản:

- tạo Screening;
- đọc một Screening;
- liệt kê Screening theo movie;
- hủy Screening.

M3 không bao gồm seat availability hoặc booking.

## Domain Model

Screening gồm các field:

- `id`
- `movieId`
- `hallId`
- `startTime`
- `endTime`
- `basePrice`
- `status`

ScreeningStatus gồm:

- `SCHEDULED`
- `CANCELLED`
- `COMPLETED`

## Domain Rules

- `movieId` không null/blank.
- `hallId` không null/blank.
- `startTime` và `endTime` không null.
- `startTime < endTime`.
- `basePrice >= 0`.
- Screening mới bắt đầu ở trạng thái `SCHEDULED`.

Cancellation:

- `SCHEDULED -> CANCELLED` được phép.
- `CANCELLED -> CANCELLED` bị từ chối.
- `COMPLETED -> CANCELLED` bị từ chối.

## Scheduling Rule

Hai Screening trong cùng Hall không được overlap. Công thức là:

```text
existing.startTime < newEnd
&& existing.endTime > newStart
```

Các interval chỉ chạm biên vẫn được phép:

```text
10:00–12:00
12:00–13:00
```

Repository hiện không loại Screening khỏi overlap theo status; mọi status được xét theo contract hiện tại.

## Application Use Cases

- `CreateScreening`
- `CancelScreening`
- `GetScreening`
- `GetScreenings` theo `movieId`

`GetAvailableSeats` nằm ngoài phạm vi M3 và được deferred.

## Create Flow

```text
Command
-> validate
-> load Movie
-> load Hall
-> check overlap
-> create Screening
-> save
-> return ID
```

## Cancel Flow

```text
Command
-> validate
-> find Screening
-> Screening.cancel()
-> translate invalid transition to ConflictException
-> save successful transition
```

Invalid lifecycle transition không làm gọi `save`.

## HTTP Contract

| Method | Path | Success |
| --- | --- | --- |
| POST | `/screenings` | 201 + `{ "id": "..." }` |
| GET | `/screenings/{id}` | 200 + `ScreeningView` |
| GET | `/screenings?movieId={movieId}` | 200 + `List<ScreeningView>` |
| POST | `/screenings/{id}/cancel` | 204, không body |

Cancel là state transition, không xóa resource. Sau khi cancel, `GET /screenings/{id}` vẫn trả `200` với status `CANCELLED`.

## Error Contract

- `ValidationException` -> `400`.
- `NotFoundException` -> `404`.
- `ConflictException` -> `409`.

Ví dụ conflict:

- Screening mới overlap một Screening cùng Hall.
- Cancel Screening đã `CANCELLED` hoặc `COMPLETED`.

## Explicitly Deferred

- `GetAvailableSeats`.
- `SeatHold`.
- Booking/Checkout.
- Payment thật.
- Transition hoàn thành Screening (`complete`).
- Persistence Screening bằng JPA.
