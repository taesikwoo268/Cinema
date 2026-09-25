# Bằng chứng nghiệm thu kỹ thuật M1

Ngày: 2026-09-21. Repository được kiểm tra: D:\Cinema.
Nhánh: feature/nghia/moc-5-web-integration.

**M1 hoàn thành ngày 2026-09-21 theo quy trình dự án cá nhân.**
Không suy ra nghiệm thu milestone từ exit code của Maven.

## Bằng chứng theo tiêu chí

| Tiêu chí | Bằng chứng |
| --- | --- |
| Domain/Application | Domain tests, VenueCommandHandlerTest (39), VenueQueryHandlerTest; validation trước lookup |
| HTTP tích hợp | VenueApiIntegrationTest (13): Spring context thật, controller/handler/repository thật; đủ endpoint create/read, danh sách rỗng, null/blank, 400/404/409 |
| HTTP qua server | VenueHttpAcceptanceTest (1): RANDOM_PORT, TestRestTemplate gửi qua localhost; tạo Cinema → Hall → Seat và đọc lại ID/row |
| Lỗi HTTP thực | Cùng test server xác minh number=0 → 400 VALIDATION; Cinema thiếu → 404 NOT_FOUND; Hall/Seat trùng → 409 CONFLICT; /movies → 403 |
| Khác Hall được dùng cùng vị trí | Test HTTP tạo cùng A1 ở Hall khác thành công; test adapter cũng xác minh |
| Uniqueness và cạnh tranh | InMemoryHallRepositoryTest, InMemorySeatRepositoryTest: đúng một insert thành công, một ConflictException, một bản ghi |
| Snapshot | VenueSnapshotTest (1): không thể clear list; snapshot cũ không đổi khi thêm bản ghi |
| Security | DefaultVenueSecurityTest, VenueWebBoundaryTest, VenueDevelopmentContextTest: default deny-all và allowlist venue-dev |
| Kiến trúc/hồi quy | CleanArchitectureTest: 6/6; toàn bộ clean verify không failure/error |
| Rà soát cá nhân | Implementation và các bằng chứng kỹ thuật đã đối chiếu; không yêu cầu review/merge nhóm |

VenueWebBoundaryTest dùng ProbeController chỉ để kiểm tra ranh giới Web/security.
Bằng chứng nghiệp vụ đến từ hai test tích hợp mới, không từ ProbeController.

## Lệnh đã chạy trong D:\Cinema

```powershell
.\mvnw.cmd '-Dmaven.repo.local=C:\Users\admin\.m2\repository' '-Dtest=VenueApiIntegrationTest,VenueHttpAcceptanceTest,VenueSnapshotTest' -o test
.\mvnw.cmd '-Dmaven.repo.local=C:\Users\admin\.m2\repository' -o clean verify
```

- Nhóm nghiệm thu mới: 15/15 đạt, không skipped.
- Toàn bộ: 165 trường hợp, 157 đạt, 0 failure, 0 error, 8 skipped.
- Các skipped thuộc BookingWorkflowTest skeleton ngoài M1, không tính là đã đạt.
- Maven báo BUILD SUCCESS; báo cáo tại target/surefire-reports/.

## Phần bổ sung

- Ba controller Cinema/Hall/Seat và VenueConfiguration nối các bean dùng chung.
- Mapping ConflictException → 409 CONFLICT.
- Integration HTTP, HTTP server thực và snapshot tests.
- Không thay implementation Domain/Application bằng bản sample, không sửa test để bỏ qua lỗi.

## Giới hạn và trạng thái chính thức

M1 dùng in-memory trong một JVM, mất dữ liệu khi restart; chưa có JPA/migration,
Screening, sửa/xóa địa điểm, booking hoặc ADMIN thật.
Checklist M1 trong cinema-v1-progress.md đã [x].
Không tạo commit/merge trong đợt cập nhật tài liệu này. Kết quả test trên là lần chạy trước; không chạy lại test trong đợt này.
