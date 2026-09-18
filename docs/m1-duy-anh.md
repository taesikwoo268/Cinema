# M1 — Bản giao việc Duy Anh

Vai trò: kiểm thử độc lập Domain và Application của M1.
Trạng thái: kế hoạch giao việc, chưa triển khai.
Đọc [đặc tả M1](m1-specification.md); dùng quy tắc mục 3 đã được Nghĩa
xác nhận ngày 2026-09-18 để viết test expectation.

## 1. Phạm vi sở hữu

Chỉ thêm các file test trong hai package dưới đây.
Không sửa source sản phẩm, DomainRulesTest của M0, test adapter của Duy Khánh,
test HTTP/cấu hình của Nghĩa hoặc architecture tests hiện có.

Các lớp test dùng JUnit 5; dùng Mockito có sẵn trong starter-test để mock port.
Không thêm thư viện mới hoặc khởi động Spring cho test Domain/Application.
Supplier<String> trả ID xác định để assert chính xác.

## 2. Test Domain cần thêm

Gốc: src/test/java/cinema/domain/cinema/.

CinemaTest.java:

- rejectsBlankName: null, empty, khoảng trắng.
- rejectsBlankAddress: theo lựa chọn đã chốt.
- preservesValidFields: name/address hợp lệ, trim đúng.
- ID vẫn đi qua BaseEntity; không sao chép cả bộ test M0.

HallTest.java:

- rejectsBlankCinemaId.
- rejectsBlankName.
- preservesCinemaIdAndNormalizesName.
- Không kiểm thử Cinema có tồn tại ở Domain; đây là việc của Application.

SeatTest.java:

- rejectsBlankHallId và rejectsBlankRow.
- rejectsNonPositiveNumber: 0, số âm.
- rejectsNullType.
- acceptsEachSeatType: NORMAL, VIP, COUPLE.
- normalizesRow: a và " A " cho cùng giá trị A theo quy tắc đã duyệt.
- preservesNumberAndHallId.
- Không kiểm tra uniqueness bằng cách tạo hai object Domain:
  uniqueness phụ thuộc repository, không nằm trong constructor Seat.

Mỗi trường hợp lỗi constructor assert DomainException.

## 3. Test command handler cần thêm

Gốc: src/test/java/cinema/application/venues/.

CreateCinemaCommandHandlerTest.java:

- createsCinemaAndReturnsGeneratedId: capture entity truyền vào save.
- rejectsNullCommand và invalidDomainDataBecomesValidationException.
- duplicateIdConflictPropagates: mock save ném ConflictException,
  xác nhận handler không đổi nó thành ValidationException.

CreateHallCommandHandlerTest.java:

- createsHallForExistingCinema.
- rejectsNullCommandAndBlankParentId.
- missingCinemaDoesNotSave: NotFoundException và verify không save.
- duplicateNameDoesNotSave: theo quy tắc đã chốt.
- repositoryConflictPropagates: kiểm tra tình huống precheck chưa thấy trùng
  nhưng save ném ConflictException.
- domainValidationBecomesApplicationValidation.

CreateSeatCommandHandlerTest.java:

- createsSeatForExistingHall.
- rejectsNullCommand, missingNumber, invalidNumber, missingOrUnknownType.
- missingHallDoesNotSave.
- duplicatePositionDoesNotSave.
- checksDuplicateUsingNormalizedRow.
- repositoryConflictPropagates.
- domainValidationBecomesApplicationValidation.

Type chưa hợp lệ cần kiểm tra không lưu dữ liệu.
Ở trường hợp parent thiếu, verify repository tài nguyên con không save.
Không ép chính xác thứ tự mọi lời gọi nội bộ nếu đặc tả không yêu cầu.

## 4. Test query handler cần thêm

Trong cùng package application/venues/:

GetCinemasQueryHandlerTest.java:

- returnsMappedList và returnsEmptyList.
- rejectsNullQuery.

GetCinemaQueryHandlerTest.java:

- returnsView, missingCinemaThrowsNotFound, rejectsBlankId.

GetHallsQueryHandlerTest.java:

- returnsOnlyRequestedCinemaHalls theo dữ liệu mock đúng hợp đồng port.
- existingCinemaWithoutHallsReturnsEmpty.
- missingCinemaThrowsNotFound; không coi cha thiếu là danh sách rỗng.
- rejectsBlankCinemaId.

GetHallQueryHandlerTest.java:

- returnsView, missingHallThrowsNotFound, rejectsBlankId.

GetSeatsQueryHandlerTest.java:

- returnsPhysicalSeatViews: id, hallId, row, number, type đúng.
- existingHallWithoutSeatsReturnsEmpty.
- missingHallThrowsNotFound.
- rejectsBlankHallId.

Test mapping dùng nhiều phần tử có ID/parent khác nhau để phát hiện gán nhầm.
Không yêu cầu DTO vật lý trả available; trường này không thuộc M1.
Test sort/filter thật của repository thuộc Duy Khánh, không chứng minh bằng mock.

## 5. Phối hợp và kiểm chứng

Nhánh: feature/duy-anh/moc-5-domain-application-tests.

- Sau PR hợp đồng của Long, chuẩn bị test cases theo chữ ký đã chốt.
- Test gọi handler chưa tồn tại chỉ chạy được khi implementation tương ứng sẵn sàng.
  Cập nhật từ main hoặc chạy trên nhánh tích hợp để xác minh trước PR.
- Không ghép test chưa compile vào main.
- Không thêm @Disabled, test rỗng hoặc đổi expectation chỉ để build xanh.
- Lỗi sản phẩm gửi Long sửa; bất đồng quy tắc gửi Nghĩa chốt.
- PR ghi số test, lệnh chạy và nhóm ràng buộc đã phủ.

Lệnh dự kiến sau tích hợp:

```powershell
.\mvnw.cmd '-Dtest=CinemaTest,HallTest,SeatTest,*CommandHandlerTest,*QueryHandlerTest' test
```

Nếu pattern trùng test ngoài M1, dùng tên đầy đủ của lớp test cần chạy.
Nghĩa chạy verify toàn bộ khi nghiệm thu; không chỉ dựa vào test riêng nhánh này.

