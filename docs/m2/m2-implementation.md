# M2 Implementation

## Ownership

M2 Movie được triển khai toàn bộ bởi Nghia.

## Domain

Movie giữ sáu field; MovieStatus gồm COMING_SOON, SHOWING, ARCHIVED.
Constructor bảo vệ title nonblank và durationMinutes > 0.

## Application

MovieRepository: findById, findAll, save, deleteById.
MovieView: id, title, description, durationMinutes, ageRating, MovieStatus status.

- CreateMovieCommand/Handler: validate input trước sinh ID/lưu, tạo Movie COMING_SOON, trả ID.
- DeleteMovieCommand/Handler: validate ID, lookup hoặc NotFoundException, xóa.
- GetMovieQuery/Handler: validate ID, lookup hoặc NotFoundException, map đủ sáu field.
- GetMoviesQuery/Handler: validate query, gọi findAll một lần, map giữ thứ tự.

Domain/Application không chứa HTTP response hoặc phụ thuộc Infrastructure.

## Infrastructure

InMemoryMovieRepository implements MovieRepository:

- HashMap storage; các operation synchronized.
- Duplicate ID → ConflictException; không áp unique title.
- findAll theo ID, trả snapshot bất biến và độc lập với các lần ghi tiếp theo.
- findById thiếu → Optional.empty().
- delete missing ID không gây lỗi ở repository; Application đã lookup trước delete.

MovieConfiguration đăng ký một MovieRepository bean trong venue-dev.
UseCaseConfiguration có đúng bốn Movie handler beans được giới hạn venue-dev,
cùng nhận repository singleton. Create nhận Supplier<String> sinh UUID tại composition root.

## Web

MovieController chỉ ánh xạ request → command/query → handler → HTTP response.
POST /movies; GET /movies; GET /movies/{id}; DELETE /movies/{id}.
CreateMovieRequest có title, description, durationMinutes, ageRating.
CreatedResourceResponse tái sử dụng convention M1; query trả MovieView trực tiếp.

## Security

venue-dev cho phép đúng bốn method/path Movie ở trên, giữ các route Venue đã có.
Unsupported routes/methods vẫn deny; default denyAll được giữ nguyên.
CSRF không tắt toàn cục. Chỉ exempt POST /movies và DELETE /movies/{id}
cùng các exemption POST M1 Venue hiện có.

## Profile lifecycle

Trong venue-dev: MovieController, bốn handler beans và MovieRepository active.
Ngoài venue-dev: MovieController và bốn handler beans inactive, MovieRepository absent.
Default Spring context khởi động mà không cần fallback in-memory production repository.
Movie JPA vẫn chưa triển khai; M3 Screening chưa bắt đầu tại checkpoint M2.

Xem [specification](m2-specification.md) và [acceptance](m2-acceptance.md).
