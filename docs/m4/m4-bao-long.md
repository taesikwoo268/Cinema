# M4 Assignment --- Bảo Long

## 1. Role

Customer vertical slice owner.

Bạn chịu trách nhiệm **implementation + tests của chính Customer
slice**. Không viết test cho code của thành viên khác.

## 2. Branch

Chưa tạo branch cho đến khi Nghĩa công bố `M4_BASE`.

Sau đó:

`feature/bao-long/moc-8-customer`

Branch trực tiếp từ `M4_BASE`, không từ branch của Duy Anh/Duy Khanh.

## 3. Objective

Hoàn thiện Customer Domain invariants và cung cấp CustomerRepository
in-memory adapter đủ để M4 chạy/test độc lập.

## 4. Files You May Read

``` text
src/main/java/cinema/domain/common/BaseEntity.java
src/main/java/cinema/domain/common/DomainException.java
src/main/java/cinema/application/common/interfaces/CustomerRepository.java
src/main/java/cinema/domain/customer/Customer.java
src/main/java/cinema/infrastructure/persistence/inmemory/InMemoryMovieRepository.java
src/main/java/cinema/infrastructure/persistence/inmemory/InMemoryScreeningRepository.java
docs/architecture-contract.md
docs/m4/m4-overview.md
```

Các file contract chỉ READ-ONLY.

## 5. Files You Own / May Modify

``` text
src/main/java/cinema/domain/customer/Customer.java
```

Public API đã freeze:

``` java
Customer(String id, String userId, String displayName)
String getId()
String getUserId()
String getDisplayName()
```

Không đổi constructor/getters.

## 6. Files You May Create

``` text
src/main/java/cinema/infrastructure/persistence/inmemory/InMemoryCustomerRepository.java
src/test/java/cinema/domain/customer/CustomerTest.java
src/test/java/cinema/infrastructure/persistence/inmemory/InMemoryCustomerRepositoryTest.java
```

Nếu test package convention thực tế khác, giữ cùng target nhưng không mở
rộng ownership.

## 7. Implementation Requirements

Customer:

-   ID do `BaseEntity` bảo vệ;
-   userId null/empty/blank -\> `DomainException`;
-   displayName null/empty/blank -\> `DomainException`;
-   displayName được trim;
-   không chứa password/credential.

InMemoryCustomerRepository:

-   implement đúng `CustomerRepository`;
-   `findById`;
-   `findByUserId`;
-   `save`;
-   userId uniqueness;
-   deterministic behavior;
-   thread-safety theo convention in-memory repositories hiện có;
-   không expose mutable collection nội bộ.

Nếu duplicate userId cần báo conflict nhưng frozen repository contract
không đủ để biểu diễn đúng rule ở adapter, **không sửa interface**; báo
Nghĩa.

## 8. Tests You Must Write

Customer:

-   valid Customer;
-   blank/null ID;
-   blank/null userId;
-   blank/null displayName;
-   displayName trim;
-   getters preserve values.

Repository:

-   save/findById;
-   findByUserId;
-   missing returns empty;
-   uniqueness behavior theo contract đã chốt;
-   deterministic behavior.

## 9. Files You MUST NOT Modify

``` text
src/main/java/cinema/application/common/interfaces/CustomerRepository.java
src/main/java/cinema/application/common/interfaces/IdentityService.java
src/main/java/cinema/application/common/interfaces/CurrentUser.java

src/main/java/cinema/application/identity/**
src/main/java/cinema/infrastructure/identity/**

src/main/java/cinema/infrastructure/config/UseCaseConfiguration.java
src/main/java/cinema/web/controllers/IdentityController.java
src/main/java/cinema/web/security/**

src/main/java/cinema/infrastructure/persistence/entities/CustomerJpaEntity.java
src/main/java/cinema/infrastructure/persistence/mappers/CustomerPersistenceMapper.java
src/main/java/cinema/infrastructure/persistence/repositories/SpringDataCustomerRepository.java
src/main/java/cinema/infrastructure/persistence/adapters/JpaCustomerRepositoryAdapter.java

README.md
docs/cinema-v1-progress.md
docs/team-work-allocation.md
```

Customer JPA = deferred M4.

## 10. Dependencies

`CustomerRepository` interface: READ-ONLY.

Implementation needed from Duy Anh: NO.

Implementation needed from Duy Khanh: NO.

Bạn có thể hoàn thành slice chỉ từ `M4_BASE`.

## 11. Tests You Do NOT Own

-   Register handler tests.
-   Login tests.
-   GetCurrentUser tests.
-   IdentityService tests.
-   HTTP/security acceptance tests.

## 12. Commands to Run

Dùng Maven wrapper của repo. Tối thiểu chạy targeted tests của slice và
`git diff --check`. Không sửa unrelated code chỉ để full suite chạy.

## 13. Handover Requirements

Gửi Nghĩa:

-   branch;
-   commit hash;
-   exact changed files;
-   tests run;
-   test result;
-   known limitations;
-   xác nhận không sửa forbidden files.

## 14. If Contract Is Insufficient

DỪNG phần bị ảnh hưởng và báo Nghĩa. Không tự sửa frozen interface/API.
