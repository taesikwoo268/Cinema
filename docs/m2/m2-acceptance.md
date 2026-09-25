# M2 Acceptance

Checkpoint đã xác minh: 2026-09-25, D:\Cinema, branch feature/nghia/moc-6.
Owner: Nghia. Đây là kết quả lần chạy checkpoint, không chạy lại test khi cập nhật tài liệu.

## Movie HTTP Acceptance

MovieHttpAcceptanceTest: 7 tests, 0 failures, 0 errors.
SpringBootTest RANDOM_PORT + TestRestTemplate, profile venue-dev, HTTP thật.
Security filters active; không mock Movie controller/handler/repository.
POST/DELETE không gửi CSRF token.

| Scenario | Verified |
| --- | --- |
| POST valid | 201, generated ID không blank |
| GET created | 200, đủ sáu field, status COMING_SOON |
| GET collection | 200, JSON array chứa generated ID |
| DELETE existing | 204, body rỗng |
| GET deleted | 404 NOT_FOUND |
| Blank title | 400 VALIDATION |
| Duration = 0 | 400 VALIDATION |
| Duration < 0 | 400 VALIDATION |
| GET missing | 404 NOT_FOUND |
| DELETE missing | 404 NOT_FOUND |
| Wrong JSON field type | 400 VALIDATION, sanitized message |

Không thêm HTTP assertion [] vì shared context không bảo đảm empty state.
Empty-list behavior được kiểm tra ở query/repository unit tests.

## Architecture

Verified: Web → Application; Application → Domain;
Infrastructure → Application/Domain; Domain Spring-free.
CleanArchitectureTest: 6 tests pass tại checkpoint M2.

## Full Regression

Final M2 checkpoint: Tests run: 230; Failures: 0; Errors: 0; Skipped: 8.
BUILD SUCCESS. Skipped tests thuộc BookingWorkflowTest hiện có.
Nhóm Movie riêng: 44 unit/application/repository + 7 HTTP = 51 tests pass.
Default context: 9 tests pass; venue-dev context/security: 46 tests pass.

Lệnh đã chạy:

```powershell
.\mvnw.cmd '-Dmaven.repo.local=C:\Users\admin\.m2\repository' '-Dtest=MovieHttpAcceptanceTest' -o test
.\mvnw.cmd '-Dmaven.repo.local=C:\Users\admin\.m2\repository' -o clean test
```

Status: **M2 ACCEPTED**.
Phạm vi nghiệm thu là Movie in-memory trong venue-dev, không bao gồm Movie JPA hoặc M3 Screening.
Xem [specification](m2-specification.md) và [implementation](m2-implementation.md).
