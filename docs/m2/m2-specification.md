# M2 — Movie

## Scope

M2 Movie đã triển khai; owner: Nghia.
Movie fields: id, title, description, durationMinutes, ageRating, status.
MovieStatus: COMING_SOON, SHOWING, ARCHIVED. Initial status: COMING_SOON.

## Domain Rules

- title không null/blank.
- durationMinutes > 0.

## Application Use Cases

- CreateMovie
- DeleteMovie
- GetMovie
- GetMovies

MovieView là record đủ sáu field; status giữ kiểu MovieStatus.

## HTTP API

| Method | Path | Success |
| --- | --- | --- |
| POST | /movies | 201 + CreatedResourceResponse: {"id":"..."} |
| GET | /movies | 200 + List<MovieView>; collection rỗng trả [] |
| GET | /movies/{id} | 200 + MovieView |
| DELETE | /movies/{id} | 204, không body |

CreateMovieRequest: String title, String description, int durationMinutes, String ageRating.
Validation: 400 VALIDATION. Missing Movie (GET/DELETE): 404 NOT_FOUND.
Controller để GlobalExceptionHandler xử lý lỗi Application.

## Persistence

Current M2 adapter: InMemoryMovieRepository; dữ liệu mất khi restart.
Movie JPA persistence không triển khai trong M2.

## Profile

Movie HTTP/application/in-memory Spring composition chỉ active trong venue-dev.
Không có Movie controller, handler beans hoặc repository bean ngoài profile này.

Xem [implementation](m2-implementation.md) và [acceptance](m2-acceptance.md).
