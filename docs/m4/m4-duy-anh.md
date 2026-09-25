# M4 Assignment --- Duy Anh

## 1. Role

Register application vertical slice owner.

Bạn chịu trách nhiệm **Register implementation + Register unit tests**.

## 2. Branch

Chỉ tạo sau khi Nghĩa công bố `M4_BASE`:

`feature/duy-anh/moc-8-register`

Branch trực tiếp từ `M4_BASE`.

## 3. Objective

Implement Register orchestration hoàn toàn ở Application layer, chỉ phụ
thuộc frozen ports/domain API; không chờ implementation của Long/Khanh.

## 4. Frozen input/domain contracts

M4 baseline phải cung cấp:

``` java
RegisterCommand(String email, String password, String displayName)
Customer(String id, String userId, String displayName)
```

Register handler dùng:

``` text
IdentityService
CustomerRepository
Supplier<String>
```

## 5. Files You May Read

``` text
src/main/java/cinema/application/identity/commands/RegisterCommand.java
src/main/java/cinema/application/common/interfaces/IdentityService.java
src/main/java/cinema/application/common/interfaces/CustomerRepository.java
src/main/java/cinema/domain/customer/Customer.java
src/main/java/cinema/application/common/exceptions/**
src/main/java/cinema/application/movies/commands/CreateMovieCommandHandler.java
src/main/java/cinema/application/screenings/commands/CreateScreeningCommandHandler.java
docs/m4/m4-overview.md
```

## 6. Files You Own / May Modify

``` text
src/main/java/cinema/application/identity/commands/RegisterCommandHandler.java
```

## 7. Files You May Create

``` text
src/test/java/cinema/application/identity/commands/RegisterCommandHandlerTest.java
```

Validator riêng chỉ được tạo trong Register/Application nếu thật sự cần
và không thay frozen contract.

## 8. Required handler design

Constructor dependencies:

``` text
IdentityService identities
CustomerRepository customers
Supplier<String> idGenerator
```

Flow:

1.  validate command và fields trước side effects;
2.  `identities.register(email,password)` -\> `userId`;
3.  bảo đảm `userId` trả về hợp lệ;
4.  kiểm tra `customers.findByUserId(userId)`;
5.  sinh Customer ID bằng `idGenerator`;
6.  tạo `Customer(customerId,userId,displayName)`;
7.  `customers.save(customer)`;
8.  trả về Customer ID.

Không gọi implementation concrete của Khanh/Long.

## 9. Validation

Application validation:

-   null command -\> `ValidationException`;
-   blank email -\> `ValidationException`;
-   blank password -\> `ValidationException`;
-   blank displayName -\> `ValidationException`.

Không tự thêm password-strength hoặc regex email phức tạp.

Validation phải xảy ra trước side effects.

## 10. Conflict rules

-   Duplicate email/account do `IdentityService` báo theo M4 contract
    -\> conflict.
-   Nếu `findByUserId(userId)` đã có Customer -\> `ConflictException`.
-   Không tự sửa `IdentityService` hoặc `CustomerRepository` để giải
    quyết conflict.

## 11. Unit-test isolation

Trong tests:

-   mock/fake `IdentityService`;
-   mock/fake `CustomerRepository`;
-   deterministic `Supplier<String>`.

**Không dùng**:

-   `IdentityServiceImpl`;
-   `InMemoryCustomerRepository`;
-   Spring context.

Vì vậy bạn không phải chờ Bảo Long/Duy Khanh.

## 12. Tests You Must Write

Tối thiểu:

-   valid register returns generated Customer ID;
-   valid register calls identity exactly as expected;
-   saved Customer has correct ID/userId/displayName;
-   null command;
-   blank email;
-   blank password;
-   blank displayName;
-   validation causes zero side effects;
-   existing Customer for returned userId -\> conflict;
-   ID generator not called before required validations/checks;
-   repository save exactly once on success.

Nếu duplicate-email behavior cần test, fake `IdentityService` throw
conflict; không cần implementation Khanh.

## 13. Files You MUST NOT Modify

``` text
src/main/java/cinema/application/identity/commands/RegisterCommand.java
src/main/java/cinema/application/common/interfaces/**
src/main/java/cinema/application/common/models/**
src/main/java/cinema/domain/customer/Customer.java

src/main/java/cinema/application/identity/commands/LoginCommandHandler.java
src/main/java/cinema/application/identity/queries/GetCurrentUserQueryHandler.java
src/main/java/cinema/infrastructure/identity/**

src/main/java/cinema/infrastructure/persistence/**
src/main/java/cinema/infrastructure/config/UseCaseConfiguration.java
src/main/java/cinema/web/**
README.md
docs/cinema-v1-progress.md
docs/team-work-allocation.md
```

## 14. Tests You Do NOT Own

Customer repository/domain tests, Login, CurrentUser, IdentityService,
HTTP/security acceptance.

## 15. Handover

Gửi Nghĩa branch, commit, changed files, tests, PASS/FAIL, limitations
và xác nhận forbidden files không đổi.

## 16. If Contract Is Insufficient

Không tự sửa contract. Dừng phần bị ảnh hưởng và báo Nghĩa.
