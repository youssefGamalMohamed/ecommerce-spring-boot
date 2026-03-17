# Tasks: Architecture Refactor & Enhancement

**Input**: Design documents from `/specs/005-architecture-refactor/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Not explicitly requested — test tasks are NOT included. Implementation tasks only.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story. Due to cross-cutting entity changes, a Foundational phase handles shared model changes first.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/main/java/com/app/ecommerce/` (package root)
- **Config**: `src/main/resources/application.yml`
- **Build**: `pom.xml` at repository root

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add new Maven dependencies and configuration required by multiple user stories.

- [ ] T001 Add new Maven dependencies to `pom.xml`: add `spring-boot-starter-security`, `jjwt-api` (version 0.11.5), `jjwt-impl` (version 0.11.5, runtime scope), `jjwt-jackson` (version 0.11.5, runtime scope), and `spring-boot-starter-actuator` under the `<dependencies>` section. Keep existing dependencies unchanged. Use groupId `io.jsonwebtoken` for all JJWT artifacts.

- [ ] T002 Add new configuration properties to `src/main/resources/application.yml`: (1) Under a new top-level `jwt` key, add `secret-key` with a base64-encoded 256-bit secret string, `access-token-expiration` set to `900000` (15 minutes in ms), and `refresh-token-expiration` set to `604800000` (7 days in ms). (2) Under `management.endpoints.web.exposure`, add `include: health,info,metrics`. (3) Under `management.endpoint.health`, add `show-details: always`. (4) Under `management.health`, add `redis.enabled: true` and `db.enabled: true`. Keep all existing configuration unchanged.

---

## Phase 2: Foundational (Entity Layer Fixes)

**Purpose**: Fix data model issues (BigDecimal, @Version, FetchType.LAZY, remove @JsonIgnore, LocalDate) that ALL subsequent user stories depend on. These are cross-cutting changes shared by US1, US2, US6, US7.

**CRITICAL**: No user story work can begin until this phase is complete. All entity changes must be applied together to avoid partial inconsistency.

- [ ] T003 Modify `src/main/java/com/app/ecommerce/product/Product.java`: (1) Change field `price` from `double` to `BigDecimal`. (2) Add `@Column(precision = 19, scale = 2)` annotation to the `price` field. (3) Add a new field `@Version private Long version;` after the `quantity` field. (4) Change `@ManyToMany(fetch = FetchType.EAGER, ...)` on the `categories` field to `@ManyToMany(fetch = FetchType.LAZY, ...)` — keep all other attributes (cascade, JoinTable) unchanged. (5) Remove the `@JsonIgnore` annotation from the `cartItem` field. (6) Remove the `@ToString.Exclude` annotation from the `cartItem` field only if it was added solely to prevent Jackson circular reference — keep it if it prevents Lombok toString recursion (check if `Cart` also has `@ToString.Exclude` on its side). (7) Add import for `java.math.BigDecimal` and `jakarta.persistence.Version`. Ensure `com.fasterxml.jackson.annotation.JsonIgnore` import is removed if no longer used.

- [ ] T004 Modify `src/main/java/com/app/ecommerce/category/Category.java`: (1) Add a new field `@Version private Long version;` after the `name` field. (2) Change `@ManyToMany(mappedBy = "categories", fetch = FetchType.EAGER, ...)` on the `products` field to `@ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY, ...)` — keep all other attributes (cascade) unchanged. (3) Remove the `@JsonIgnore` annotation from the `products` field. (4) Remove the import for `com.fasterxml.jackson.annotation.JsonIgnore` if no longer used. (5) Add import for `jakarta.persistence.Version`.

- [ ] T005 Modify `src/main/java/com/app/ecommerce/order/Order.java`: (1) Change field `totalPrice` from `double` to `BigDecimal`. (2) Add `@Column(precision = 19, scale = 2)` annotation to the `totalPrice` field. (3) Add a new field `@Version private Long version;` after the `totalPrice` field. (4) Add import for `java.math.BigDecimal` and `jakarta.persistence.Version`.

- [ ] T006 Modify `src/main/java/com/app/ecommerce/order/DeliveryInfo.java`: (1) Change field `date` from `String` to `LocalDate`. (2) Add import for `java.time.LocalDate`. (3) Remove the `String` type if it was the only usage. The `@Embeddable` annotation and other fields (`status`, `address`) remain unchanged.

- [ ] T007 Modify `src/main/java/com/app/ecommerce/cart/Cart.java`: (1) Add a new field `@Version private Long version;` after the `id` field. (2) Remove the `@JsonIgnore` annotation from the `order` field. (3) Keep `@ToString.Exclude` on the `order` field (it prevents Lombok toString recursion). (4) Remove the import for `com.fasterxml.jackson.annotation.JsonIgnore` if no longer used. (5) Add import for `jakarta.persistence.Version`.

- [ ] T008 Modify `src/main/java/com/app/ecommerce/cart/CartItem.java`: (1) Remove the `@JsonIgnore` annotation from the `cart` field. (2) Keep `@ToString.Exclude` on the `cart` field (prevents Lombok recursion). (3) Remove the import for `com.fasterxml.jackson.annotation.JsonIgnore` if no longer used.

- [ ] T009 Modify `src/main/java/com/app/ecommerce/product/ProductRepository.java`: Add an `@EntityGraph(attributePaths = "categories")` annotation on the existing `findAll(Specification<Product> spec, Pageable pageable)` method (this may need to be explicitly declared as an override). Also add a new method: `@EntityGraph(attributePaths = "categories") Optional<Product> findById(UUID id);` — this overrides the default JpaRepository.findById to eagerly load categories. Add import for `org.springframework.data.jpa.repository.EntityGraph`.

- [ ] T010 Modify `src/main/java/com/app/ecommerce/product/ProductSpecifications.java`: Change the parameter types of `priceGte(Double minPrice)` to `priceGte(BigDecimal minPrice)` and `priceLte(Double maxPrice)` to `priceLte(BigDecimal maxPrice)`. Inside each method, change the null check to use `BigDecimal` and update the `criteriaBuilder.greaterThanOrEqualTo` / `criteriaBuilder.lessThanOrEqualTo` calls to work with `BigDecimal`. Add import for `java.math.BigDecimal`.

- [ ] T011 Verify the application compiles and starts after all entity changes by running `mvn clean compile`. Fix any compilation errors from type changes (BigDecimal in services, mappers, controllers). This is a verification step — the detailed fixes for services/mappers/controllers come in subsequent phases, but any immediate compilation blockers (e.g., MapStruct mapper methods that now have type mismatches) must be resolved here by temporarily updating the affected mappers and service methods to use `BigDecimal` and `LocalDate` where they reference entity price/date fields.

**Checkpoint**: Application compiles. Entities have @Version, BigDecimal prices, LocalDate delivery date, LAZY fetch, no @JsonIgnore. Database columns will be auto-migrated by Hibernate on next startup.

---

## Phase 3: User Story 3 — Separate Input and Output Contracts (Priority: P1) 🎯 MVP

**Goal**: Split single DTOs into separate request (create/update) and response DTOs with proper field exposure. This phase also incorporates US5 (Input Validation) since validation annotations go on the new request DTOs.

**Independent Test**: Send a create request with only writable fields and verify the response includes system-generated fields. Send a create request with an `id` field and verify it is ignored. Send an update with only changed fields and verify other fields are unchanged.

### Implementation for User Story 3

**Comment: We start with responses (rename existing DTOs) then create request DTOs, then update mappers, then update services, then update controllers. This order minimizes compilation errors.**

- [ ] T012 [P] [US3] Create `src/main/java/com/app/ecommerce/shared/dto/BaseResponse.java`: Copy the content of `BaseDto.java` and rename the class to `BaseResponse`. Keep all fields (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`) with their `@Schema(accessMode = Schema.AccessMode.READ_ONLY)` annotations. Keep `@SuperBuilder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`. This class will be the new base for all response DTOs. Do NOT delete `BaseDto.java` yet (it will be removed after all references are migrated).

- [ ] T013 [P] [US3] Create `src/main/java/com/app/ecommerce/product/ProductResponse.java`: Create a new class extending `BaseResponse`. Fields: `UUID id` (with `@Schema(accessMode = READ_ONLY)`), `String name`, `String description`, `BigDecimal price`, `Integer quantity`, `Long version`, `Set<CategoryResponse> categories`. Use `@SuperBuilder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Schema(description = "Product response")`. All fields should have `@Schema` annotations with descriptions and examples (e.g., `@Schema(description = "Product name", example = "Wireless Mouse")`).

- [ ] T014 [P] [US3] Create `src/main/java/com/app/ecommerce/category/CategoryResponse.java`: Create a new class extending `BaseResponse`. Fields: `UUID id` (READ_ONLY), `String name`, `Long version`. Use same Lombok annotations as ProductResponse. Add `@Schema` annotations.

- [ ] T015 [P] [US3] Create `src/main/java/com/app/ecommerce/order/OrderResponse.java`: Create a new class extending `BaseResponse`. Fields: `UUID id` (READ_ONLY), `PaymentType paymentType`, `BigDecimal totalPrice`, `Long version`, `DeliveryInfoResponse deliveryInfo`, `CartDto cart`. Use same Lombok annotations.

- [ ] T016 [P] [US3] Create `src/main/java/com/app/ecommerce/order/DeliveryInfoResponse.java`: Create a simple class (does NOT extend BaseResponse — DeliveryInfo is an embeddable, not an audited entity). Fields: `Status status`, `String address`, `LocalDate date`. Use `@Builder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`.

- [ ] T017 [P] [US3] Create `src/main/java/com/app/ecommerce/product/CreateProductRequest.java`: Fields: `@NotBlank @Size(max = 255) String name`, `String description` (optional), `@NotNull @DecimalMin("0.00") BigDecimal price`, `@NotNull @Min(0) Integer quantity`, `@NotEmpty Set<UUID> categoryIds`. Use `@Builder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`. Add `@Schema` annotations with descriptions and examples. Do NOT extend any base class. Import from `jakarta.validation.constraints.*`.

- [ ] T018 [P] [US3] Create `src/main/java/com/app/ecommerce/product/UpdateProductRequest.java`: Fields: `@Size(max = 255) String name` (nullable/optional), `String description` (nullable), `@DecimalMin("0.00") BigDecimal price` (nullable), `@Min(0) Integer quantity` (nullable), `Set<UUID> categoryIds` (nullable), `@NotNull Long version` (required for optimistic locking). Use same annotations as CreateProductRequest. All fields except `version` are optional — null means "don't change".

- [ ] T019 [P] [US3] Create `src/main/java/com/app/ecommerce/category/CreateCategoryRequest.java`: Fields: `@NotBlank @Size(max = 100) String name`. Use `@Builder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`.

- [ ] T020 [P] [US3] Create `src/main/java/com/app/ecommerce/category/UpdateCategoryRequest.java`: Fields: `@Size(max = 100) String name` (nullable/optional), `@NotNull Long version`. Use same annotations.

- [ ] T021 [P] [US3] Create `src/main/java/com/app/ecommerce/order/CreateOrderRequest.java`: Fields: `@NotNull PaymentType paymentType`, `@NotNull UUID cartId`. Use `@Builder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`.

- [ ] T022 [P] [US3] Create `src/main/java/com/app/ecommerce/order/UpdateOrderRequest.java`: Fields: `Status deliveryStatus` (nullable — validated via state machine in US8, for now just accept any valid Status), `String deliveryAddress` (nullable), `LocalDate deliveryDate` (nullable), `@NotNull Long version`. Use same annotations.

- [ ] T023 [US3] Modify `src/main/java/com/app/ecommerce/product/ProductMapper.java`: (1) Add new mapping method `Product mapToEntity(CreateProductRequest request);` — ignore `id`, `categories`, `cartItem`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version` (all auto-managed). (2) Add new mapping method `void updateEntityFromRequest(UpdateProductRequest request, @MappingTarget Product target);` — use `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` to skip null fields (partial update). Ignore `id`, `categories`, `cartItem`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`. Ignore `version` in the mapping (version is managed by JPA, not the mapper — the version from the request DTO is set on the entity manually in the service before save to trigger optimistic lock check). (3) Add new mapping method `ProductResponse mapToResponse(Product product);` — map all fields including `version`. (4) Add new mapping method `Page<ProductResponse> mapToResponsePage(Page<Product> page);` — or keep the existing pattern of using `.map(productMapper::mapToResponse)` on the Page. (5) Keep existing mapping methods (`mapToDto`, `mapToEntity(ProductDto)`) temporarily for backward compatibility until all callers are migrated. Add `uses = {CategoryMapper.class}` if not already present. Add imports for `CreateProductRequest`, `UpdateProductRequest`, `ProductResponse`, `org.mapstruct.BeanMapping`, `org.mapstruct.NullValuePropertyMappingStrategy`.

- [ ] T024 [US3] Modify `src/main/java/com/app/ecommerce/category/CategoryMapper.java`: (1) Add `Category mapToEntity(CreateCategoryRequest request);` — ignore `id`, `products`, audit fields, `version`. (2) Add `void updateEntityFromRequest(UpdateCategoryRequest request, @MappingTarget Category target);` — use `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)`. Ignore `id`, `products`, audit fields. Ignore `version`. (3) Add `CategoryResponse mapToResponse(Category category);` — map all fields including `version`. (4) Keep existing methods temporarily.

- [ ] T025 [US3] Modify `src/main/java/com/app/ecommerce/order/OrderMapper.java`: (1) Add `Order mapToEntity(CreateOrderRequest request);` — ignore `id`, `deliveryInfo`, `cart`, audit fields, `version`, `totalPrice`. Note: `cart` and `totalPrice` will be resolved in the service layer, not by the mapper. (2) Add `void updateEntityFromRequest(UpdateOrderRequest request, @MappingTarget Order target);` — map `deliveryStatus` → `deliveryInfo.status`, `deliveryAddress` → `deliveryInfo.address`, `deliveryDate` → `deliveryInfo.date`. Use `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)`. This mapping is complex due to the embedded object — if MapStruct cannot handle it directly, add `@AfterMapping` or handle it manually in the service. (3) Add `OrderResponse mapToResponse(Order order);` — map all fields including `version`. (4) Update `uses = {DeliveryInfoMapper.class, CartMapper.class}` if not already present.

- [ ] T026 [US3] Modify `src/main/java/com/app/ecommerce/order/DeliveryInfoMapper.java`: Add `DeliveryInfoResponse mapToResponse(DeliveryInfo deliveryInfo);` — map `status`, `address`, `date` (now `LocalDate`). Keep existing methods.

- [ ] T027 [US3] Modify `src/main/java/com/app/ecommerce/product/ProductService.java` (interface): Change method signatures: (1) `ProductResponse save(CreateProductRequest request);` (2) `ProductResponse findById(UUID productId);` (3) `Page<ProductResponse> findAll(String name, BigDecimal minPrice, BigDecimal maxPrice, UUID categoryId, Pageable pageable);` (4) `ProductResponse updateById(UUID productId, UpdateProductRequest request);` (5) `void deleteById(UUID productId);` — keep as-is. Add imports for new types.

- [ ] T028 [US3] Modify `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java`: Update all methods to match the new interface signatures. (1) `save(CreateProductRequest request)`: Map `request` to entity using `productMapper.mapToEntity(request)`. Resolve categories from `request.getCategoryIds()` by calling `categoryService.getCategories(request.getCategoryIds())`. Set categories on entity. Save and return `productMapper.mapToResponse(savedProduct)`. (2) `findById(UUID)`: Return `productMapper.mapToResponse(product)` instead of `mapToDto`. (3) `findAll(...)`: Change `Double minPrice/maxPrice` parameters to `BigDecimal`. Map results using `.map(productMapper::mapToResponse)`. (4) `updateById(UUID, UpdateProductRequest)`: Find existing product. Set `product.setVersion(request.getVersion())` to enable optimistic lock check. Use `productMapper.updateEntityFromRequest(request, product)`. If `request.getCategoryIds() != null`, resolve and set categories. Save and return `productMapper.mapToResponse(updatedProduct)`. (5) `deleteById`: Keep as-is. Remove all old DTO imports (`ProductDto`). Add imports for new types.

- [ ] T029 [US3] Modify `src/main/java/com/app/ecommerce/category/CategoryService.java` (interface): Change method signatures: (1) `CategoryResponse save(CreateCategoryRequest request);` (2) `CategoryResponse findById(UUID categoryId);` (3) `Page<CategoryResponse> findAll(String name, Pageable pageable);` (4) `CategoryResponse updateById(UUID categoryId, UpdateCategoryRequest request);` (5) `void deleteById(UUID categoryId);` (6) Keep `Set<Category> getCategories(Set<UUID> categoryIds);` — this returns entities, not DTOs, for internal use by ProductService.

- [ ] T030 [US3] Modify `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java`: Update all methods to match new interface signatures. (1) `save(CreateCategoryRequest)`: Check for duplicate name using `categoryRepository.findByName(request.getName())`. Map request to entity. Save and return `categoryMapper.mapToResponse(savedCategory)`. (2) `findById`: Return `categoryMapper.mapToResponse(category)`. (3) `findAll`: Map results using `.map(categoryMapper::mapToResponse)`. (4) `updateById(UUID, UpdateCategoryRequest)`: Find existing. Set `category.setVersion(request.getVersion())`. Use `categoryMapper.updateEntityFromRequest(request, category)`. Check name uniqueness if name changed. Save and return `categoryMapper.mapToResponse(updated)`. (5) `deleteById`: Keep as-is.

- [ ] T031 [US3] Modify `src/main/java/com/app/ecommerce/order/OrderService.java` (interface): Change method signatures: (1) `OrderResponse createNewOrder(CreateOrderRequest request);` (2) `OrderResponse findById(UUID orderId);` (3) `OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request);` (4) `Page<OrderResponse> findAll(Status status, PaymentType paymentType, Instant createdAfter, Instant createdBefore, Pageable pageable);`

- [ ] T032 [US3] Modify `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java`: Update all methods. (1) `createNewOrder(CreateOrderRequest)`: Look up Cart by `request.getCartId()`. Calculate `totalPrice` as `BigDecimal` by summing `cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getProductQuantity()))` for all cart items. Create Order entity, set `paymentType`, `totalPrice`, `cart`, and default `deliveryInfo`. Save and return `orderMapper.mapToResponse(savedOrder)`. (2) `findById`: Return `orderMapper.mapToResponse(order)`. (3) `updateOrder(UUID, UpdateOrderRequest)`: Find existing order. Set `order.setVersion(request.getVersion())`. Apply delivery info changes from request (if non-null). Save and return `orderMapper.mapToResponse(updated)`. (4) `findAll`: Map results using `.map(orderMapper::mapToResponse)`.

- [ ] T033 [US3] Modify `src/main/java/com/app/ecommerce/product/ProductController.java` (interface): Change method signatures to use new request/response types. (1) `save(@Valid @RequestBody CreateProductRequest request)` → returns `ResponseEntity<ApiResponseDto<ProductResponse>>`. (2) `findById(@PathVariable UUID id)` → returns `ResponseEntity<ApiResponseDto<ProductResponse>>`. (3) `findAll(String name, BigDecimal minPrice, BigDecimal maxPrice, UUID categoryId, Pageable pageable)` → returns `ResponseEntity<ApiResponseDto<Page<ProductResponse>>>`. (4) `updateById(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request)` → returns `ResponseEntity<ApiResponseDto<ProductResponse>>`. (5) Change `@PutMapping` to `@PatchMapping` on the update method. (6) Keep `@DeleteMapping` as-is. Update all `@ApiResponse` and `@Operation` annotations to reference new types.

- [ ] T034 [US3] Modify `src/main/java/com/app/ecommerce/product/ProductControllerImpl.java`: Update to match new interface. Change method implementations to use new request/response types. Change `@PutMapping("/{id}")` to `@PatchMapping("/{id}")` on `updateById`. Ensure `@Valid` is on request body parameters.

- [ ] T035 [US3] Modify `src/main/java/com/app/ecommerce/category/CategoryController.java` (interface): Same pattern as ProductController — change to `CreateCategoryRequest`/`UpdateCategoryRequest`/`CategoryResponse`. Change update from `@PutMapping` to `@PatchMapping`.

- [ ] T036 [US3] Modify `src/main/java/com/app/ecommerce/category/CategoryControllerImpl.java`: Update to match new interface. Change `@PutMapping` to `@PatchMapping` on update.

- [ ] T037 [US3] Modify `src/main/java/com/app/ecommerce/order/OrderController.java` (interface): Change to `CreateOrderRequest`/`UpdateOrderRequest`/`OrderResponse`. Change update from `@PutMapping` to `@PatchMapping`.

- [ ] T038 [US3] Modify `src/main/java/com/app/ecommerce/order/OrderControllerImpl.java`: Update to match new interface. Change `@PutMapping` to `@PatchMapping` on update.

- [ ] T039 [US3] Delete the old DTO files that are now replaced — but ONLY after verifying zero remaining references: `ProductDto.java`, `CategoryDto.java`, `OrderDto.java`, `DeliveryInfoDto.java`, `BaseDto.java`. Search the entire codebase for imports of these classes first. If `CartDto.java` and `CartItemDto.java` are still used (Cart has no create/update operations), keep them. If they reference `BaseDto`, update them to extend `BaseResponse` or be standalone.

- [ ] T040 [US3] Update `src/main/java/com/app/ecommerce/cart/CartDto.java` and `src/main/java/com/app/ecommerce/cart/CartItemDto.java`: If these extend `BaseDto`, change them to extend `BaseResponse`. If `CartItemDto` references `ProductDto`, change the reference to `ProductResponse`. Update `CartMapper.java` and `CartItemMapper.java` if their return types reference old DTOs.

- [ ] T041 [US3] Verify the application compiles with `mvn clean compile`. Fix any remaining references to old DTO classes. Ensure all controller endpoints return correct response types. Verify Swagger UI loads and shows new request/response schemas.

**Checkpoint**: All endpoints use separate request/response DTOs. Create requests accept only writable fields with validation. Update requests support partial updates. Responses include all fields plus version and audit metadata. PUT is replaced by PATCH for updates.

---

## Phase 4: User Story 1 — Data Integrity on Write Operations (Priority: P1)

**Goal**: Add explicit @Transactional boundaries to all service methods and handle optimistic locking conflicts.

**Independent Test**: Simulate concurrent updates to the same product — one should succeed, the other should receive 409 Conflict.

### Implementation for User Story 1

- [ ] T042 [P] [US1] Modify `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java`: Add `@Transactional` annotation on `save()`, `updateById()`, and `deleteById()` methods. Add `@Transactional(readOnly = true)` on `findById()` and `findAll()` methods. Import `org.springframework.transaction.annotation.Transactional`. Ensure the annotation is at the method level, not class level.

- [ ] T043 [P] [US1] Modify `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java`: Add `@Transactional` on `save()`, `updateById()`, `deleteById()`, `getCategories()`. Add `@Transactional(readOnly = true)` on `findById()` and `findAll()`.

- [ ] T044 [P] [US1] Modify `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java`: Add `@Transactional` on `createNewOrder()` and `updateOrder()`. Add `@Transactional(readOnly = true)` on `findById()` and `findAll()`.

- [ ] T045 [P] [US1] Modify `src/main/java/com/app/ecommerce/cart/CartServiceImpl.java`: Add `@Transactional(readOnly = true)` on `findById()`.

- [ ] T046 [P] [US1] Modify `src/main/java/com/app/ecommerce/cart/CartItemServiceImpl.java`: Add `@Transactional(readOnly = true)` on `findById()`.

- [ ] T047 [US1] Modify `src/main/java/com/app/ecommerce/shared/exception/RestExceptionHandler.java`: Add a new handler method for `ObjectOptimisticLockingFailureException` (from `org.springframework.orm.ObjectOptimisticLockingFailureException`). The handler should: (1) Log a warning with the exception message. (2) Return `ResponseEntity` with status 409 (CONFLICT). (3) Use `ErrorResponseDto.conflict("Resource was modified by another user. Please refresh and try again.", request.getRequestURI())` — if `conflict()` factory method doesn't exist on `ErrorResponseDto`, create it. (4) Import `org.springframework.orm.ObjectOptimisticLockingFailureException`. (5) Add `@ExceptionHandler(ObjectOptimisticLockingFailureException.class)`.

- [ ] T048 [US1] Modify `src/main/java/com/app/ecommerce/shared/dto/ErrorResponseDto.java`: Add a static factory method `public static ErrorResponseDto conflict(String message, String path)` that creates an `ErrorResponseDto` with `success = false`, `status = 409`, `error = "CONFLICT"`, `message = message`, `path = path`, `timestamp = System.currentTimeMillis()`. Follow the same pattern as existing factory methods (`notFound()`, `badRequest()`, etc.).

**Checkpoint**: All service methods have explicit transactional boundaries. Concurrent updates to the same entity trigger 409 Conflict via @Version optimistic locking. Read-only operations use `readOnly = true` for performance.

---

## Phase 5: User Story 6 — Optimized Data Loading and Caching (Priority: P2)

**Goal**: Replace allEntries=true cache eviction with targeted key-based eviction.

**Independent Test**: Update one product, verify other cached products remain without cache miss. Verify no N+1 queries on product list (already handled by @EntityGraph in Phase 2).

### Implementation for User Story 6

- [ ] T049 [P] [US6] Modify `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java` cache annotations: (1) On `save()`: Change `@CacheEvict(value = CacheConstants.PRODUCTS, allEntries = true)` to `@CachePut(value = CacheConstants.PRODUCTS, key = "#result.id")`. Import `org.springframework.cache.annotation.CachePut`. Note: `#result` refers to the return value of the method. Ensure the method returns `ProductResponse` which has an `id` field. **Important**: `@CachePut` caches the return value (the `ProductResponse`), not the entity. This means `findById` must also return `ProductResponse` from cache. Since `findById` uses `@Cacheable` with key `#productId` and returns `ProductResponse`, the cached types will match. (2) On `updateById()`: Change `@CacheEvict(value = CacheConstants.PRODUCTS, allEntries = true)` to `@CachePut(value = CacheConstants.PRODUCTS, key = "#result.id")`. (3) On `deleteById()`: Change `@CacheEvict(value = CacheConstants.PRODUCTS, allEntries = true)` to `@CacheEvict(value = CacheConstants.PRODUCTS, key = "#productId")` — evict only the deleted product.

- [ ] T050 [P] [US6] Modify `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java` cache annotations: (1) On `save()`: Change `@CacheEvict(value = CacheConstants.CATEGORIES, allEntries = true)` to `@CachePut(value = CacheConstants.CATEGORIES, key = "#result.id")`. (2) On `updateById()`: Change `@CacheEvict(value = CacheConstants.CATEGORIES, allEntries = true)` to `@CachePut(value = CacheConstants.CATEGORIES, key = "#result.id")`. (3) On `deleteById()`: Change `@CacheEvict(value = CacheConstants.CATEGORIES, allEntries = true)` to `@CacheEvict(value = CacheConstants.CATEGORIES, key = "#categoryId")`.

- [ ] T051 [P] [US6] Modify `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java` cache annotations: (1) On `createNewOrder()`: Change `@CacheEvict(value = CacheConstants.ORDERS, allEntries = true)` to `@CachePut(value = CacheConstants.ORDERS, key = "#result.id")`. (2) On `updateOrder()`: Change `@CacheEvict(value = CacheConstants.ORDERS, allEntries = true)` to `@CachePut(value = CacheConstants.ORDERS, key = "#result.id")`.

**Checkpoint**: Cache eviction is targeted — updating product #1 does not invalidate cache for product #2. Delete evicts only the deleted entry. Save/update put the new value into cache.

---

## Phase 6: User Story 4 — Secure API Access (Priority: P2)

**Goal**: Implement JWT-based authentication with role-based access control. Secure all write endpoints, keep product/category browsing public.

**Independent Test**: Attempt to POST /products without a token — get 401. Login as ADMIN — POST succeeds. Login as CUSTOMER — POST /products returns 403.

### Implementation for User Story 4

- [ ] T052 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/Role.java`: Create an enum with two values: `ADMIN` and `CUSTOMER`. Add `import` as needed.

- [ ] T053 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/User.java`: Create a JPA entity implementing `org.springframework.security.core.userdetails.UserDetails`. Fields: `@Id @GeneratedValue(strategy = GenerationType.UUID) UUID id`, `@Column(unique = true, nullable = false, length = 100) String username`, `@Column(unique = true, nullable = false, length = 255) String email`, `@Column(nullable = false, length = 255) String password`, `@Enumerated(EnumType.STRING) @Column(nullable = false) Role role`, `@Column(nullable = false) boolean enabled = true`. Extend `BaseEntity` for audit fields. Implement `UserDetails` methods: `getAuthorities()` returns `List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))`, `isAccountNonExpired()` returns `true`, `isAccountNonLocked()` returns `true`, `isCredentialsNonExpired()` returns `true`, `isEnabled()` returns `this.enabled`. Use `@Entity`, `@Table(name = "users")`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@SuperBuilder`.

- [ ] T054 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/Token.java`: JPA entity. Fields: `@Id @GeneratedValue(strategy = GenerationType.UUID) UUID id`, `@Column(unique = true, nullable = false, length = 500) String accessToken`, `@Column(unique = true, nullable = false, length = 500) String refreshToken`, `boolean revoked = false`, `boolean expired = false`, `@ManyToOne @JoinColumn(name = "user_id", nullable = false) User user`. Extend `BaseEntity`. Use `@Entity`, `@Table(name = "tokens")`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@SuperBuilder`.

- [ ] T055 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/UserRepository.java`: Interface extending `JpaRepository<User, UUID>`. Methods: `Optional<User> findByUsername(String username);`, `Optional<User> findByEmail(String email);`, `boolean existsByUsername(String username);`, `boolean existsByEmail(String email);`.

- [ ] T056 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/TokenRepository.java`: Interface extending `JpaRepository<Token, UUID>`. Methods: `Optional<Token> findByAccessToken(String accessToken);`, `Optional<Token> findByRefreshToken(String refreshToken);`, `List<Token> findAllByUserAndRevokedFalseAndExpiredFalse(User user);`.

- [ ] T057 [US4] Create `src/main/java/com/app/ecommerce/shared/security/JwtService.java`: A `@Service` class. Inject `@Value("${jwt.secret-key}") String secretKey`, `@Value("${jwt.access-token-expiration}") long accessTokenExpiration`, `@Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration`. Methods: (1) `String generateAccessToken(UserDetails userDetails)` — create JWT with subject = username, claim "role" = first authority, issuedAt = now, expiration = now + accessTokenExpiration. Sign with `Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))` using `SignatureAlgorithm.HS256`. (2) `String generateRefreshToken(UserDetails userDetails)` — same but with refreshTokenExpiration and no extra claims. (3) `String extractUsername(String token)` — parse claims and return subject. (4) `boolean isTokenValid(String token, UserDetails userDetails)` — extract username, compare with userDetails.getUsername(), check not expired. (5) Private helper `Claims extractAllClaims(String token)` — parse and return claims. (6) Private helper `boolean isTokenExpired(String token)` — check expiration claim against now. Use JJWT library classes: `Jwts`, `Claims`, `Keys`, `Decoders`, `SignatureAlgorithm`.

- [ ] T058 [US4] Create `src/main/java/com/app/ecommerce/shared/security/SecurityUserDetailsService.java`: A `@Service` class implementing `UserDetailsService`. Inject `UserRepository`. Implement `loadUserByUsername(String username)`: find user by username from repository, throw `UsernameNotFoundException` if not found, return the `User` entity (which implements `UserDetails`).

- [ ] T059 [US4] Create `src/main/java/com/app/ecommerce/shared/security/JwtAuthenticationFilter.java`: A `@Component` class extending `OncePerRequestFilter`. Inject `JwtService` and `SecurityUserDetailsService`. Override `doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)`: (1) Extract `Authorization` header. If null or doesn't start with "Bearer ", call `filterChain.doFilter()` and return. (2) Extract token (substring after "Bearer "). (3) Extract username from token using `jwtService.extractUsername()`. (4) If username is not null and `SecurityContextHolder.getContext().getAuthentication() == null`: load `UserDetails` via `securityUserDetailsService.loadUserByUsername(username)`. (5) If `jwtService.isTokenValid(token, userDetails)`: create `UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())`, set details from request, set in `SecurityContextHolder`. (6) Call `filterChain.doFilter()`. Wrap token parsing in try-catch — on any exception (expired, malformed), log warning and continue without setting auth (request proceeds unauthenticated, security filter chain will reject it).

- [ ] T060 [US4] Create `src/main/java/com/app/ecommerce/shared/config/SecurityConfig.java`: A `@Configuration` `@EnableWebSecurity` class. Inject `JwtAuthenticationFilter` and `SecurityUserDetailsService`. Define `@Bean SecurityFilterChain securityFilterChain(HttpSecurity http)`: (1) Disable CSRF (`csrf.disable()`). (2) Set session management to `STATELESS`. (3) Configure authorization: `.requestMatchers(HttpMethod.POST, "/auth/**").permitAll()`, `.requestMatchers(HttpMethod.GET, "/auth/verify-registration/**", "/auth/forget-password/**").permitAll()`, `.requestMatchers(HttpMethod.GET, "/products/**", "/categories/**").permitAll()`, `.requestMatchers("/swagger-ui/**", "/api-docs/**", "/webjars/**").permitAll()`, `.requestMatchers("/actuator/health").permitAll()`, `.requestMatchers("/actuator/**").hasRole("ADMIN")`, `.anyRequest().authenticated()`. (4) Add `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`. (5) Set `authenticationProvider` bean. Define `@Bean AuthenticationProvider authenticationProvider()`: `DaoAuthenticationProvider` with `SecurityUserDetailsService` and `BCryptPasswordEncoder`. Define `@Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config)`: return `config.getAuthenticationManager()`. Define `@Bean PasswordEncoder passwordEncoder()`: return `new BCryptPasswordEncoder()`.

- [ ] T061 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/RegisterRequest.java`: Fields: `@NotBlank @Size(min = 3, max = 100) String username`, `@NotBlank @Email String email`, `@NotBlank @Size(min = 8, max = 100) String password`. Use `@Builder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`.

- [ ] T062 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/LoginRequest.java`: Fields: `@NotBlank String username`, `@NotBlank String password`. Same annotations.

- [ ] T063 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/LoginResponse.java`: Fields: `String accessToken`, `String refreshToken`. Same annotations.

- [ ] T064 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/RefreshTokenRequest.java`: Fields: `@NotBlank String refreshToken`. Same annotations.

- [ ] T065 [US4] Create `src/main/java/com/app/ecommerce/auth/AuthService.java` (interface): Methods: `LoginResponse register(RegisterRequest request);`, `LoginResponse login(LoginRequest request);`, `LoginResponse refreshToken(RefreshTokenRequest request);`.

- [ ] T066 [US4] Create `src/main/java/com/app/ecommerce/auth/AuthServiceImpl.java`: A `@Service` `@RequiredArgsConstructor` `@Slf4j` class implementing `AuthService`. Inject `UserRepository`, `TokenRepository`, `JwtService`, `PasswordEncoder`, `AuthenticationManager`. (1) `register()`: Check `userRepository.existsByUsername()` and `existsByEmail()` — throw `DuplicatedUniqueColumnValueException` if taken. Create `User` entity with encoded password, role = `CUSTOMER`, enabled = true. Save user. Generate access + refresh tokens. Save `Token` entity. Return `LoginResponse`. (2) `login()`: Call `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password))`. Find user by username. Revoke all existing valid tokens for this user (`findAllByUserAndRevokedFalseAndExpiredFalse` → set `revoked = true`, save all). Generate new tokens, save `Token` entity, return `LoginResponse`. (3) `refreshToken()`: Find token by refreshToken in `TokenRepository`. If not found or revoked/expired, throw `IllegalArgumentException("Invalid refresh token")`. Extract username from refresh token via `JwtService`. Load `UserDetails`. Validate token. Revoke old token. Generate new pair, save, return. Add `@Transactional` on all methods.

- [ ] T067 [US4] Create `src/main/java/com/app/ecommerce/auth/AuthController.java` (interface): Define methods with OpenAPI annotations: `@PostMapping("/register")` → `register(@Valid @RequestBody RegisterRequest)`, `@PostMapping("/login")` → `login(@Valid @RequestBody LoginRequest)`, `@PostMapping("/refresh-token")` → `refreshToken(@Valid @RequestBody RefreshTokenRequest)`. Add `@Tag(name = "Authentication")`.

- [ ] T068 [US4] Create `src/main/java/com/app/ecommerce/auth/AuthControllerImpl.java`: `@RestController` `@RequestMapping("/auth")` `@RequiredArgsConstructor` `@Slf4j` implementing `AuthController`. (1) `register()`: Call `authService.register(request)`, return `ApiResponseDto.created(loginResponse)` with 201 status. (2) `login()`: Call `authService.login(request)`, return `ApiResponseDto.success(loginResponse)` with 200 status. (3) `refreshToken()`: Call `authService.refreshToken(request)`, return `ApiResponseDto.success(loginResponse)` with 200 status.

- [ ] T069 [US4] Modify `src/main/java/com/app/ecommerce/shared/config/JpaConfig.java`: Update the `auditorAware()` bean to pull the authenticated username from `SecurityContextHolder` instead of returning hardcoded "SYSTEM_USER". Implementation: `return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).filter(Authentication::isAuthenticated).map(Authentication::getName).or(() -> Optional.of("SYSTEM_USER"));`. This falls back to "SYSTEM_USER" for unauthenticated operations (e.g., system-initiated). Import `org.springframework.security.core.context.SecurityContextHolder` and `org.springframework.security.core.Authentication`.

- [ ] T070 [US4] Modify `src/main/java/com/app/ecommerce/shared/exception/RestExceptionHandler.java`: Add handlers for: (1) `org.springframework.security.access.AccessDeniedException` → 403 FORBIDDEN. (2) `org.springframework.security.authentication.BadCredentialsException` → 401 UNAUTHORIZED with message "Invalid username or password". (3) `org.springframework.security.core.AuthenticationException` → 401 UNAUTHORIZED. Add factory methods `forbidden()` and `unauthorized()` to `ErrorResponseDto` if they don't exist.

**Checkpoint**: All write endpoints require authentication. GET /products and GET /categories are public. ADMIN can CRUD everything. CUSTOMER can read products/categories and manage own orders. Audit fields record real usernames. JWT tokens issued on login, refresh supported.

---

## Phase 7: User Story 8 — Order Lifecycle State Management (Priority: P3)

**Goal**: Enforce valid order status transitions so invalid changes (e.g., CANCELED → DELIVERED) are rejected.

**Independent Test**: Try to update a CANCELED order to DELIVERED — should receive 400 with allowed transitions listed.

### Implementation for User Story 8

- [ ] T071 [P] [US8] Modify `src/main/java/com/app/ecommerce/shared/enums/Status.java`: Add a field `private final Set<Status> allowedTransitions;` and a constructor. Define transitions: `NOT_MOVED_OUT_FROM_WAREHOUSE(Set.of(ON_THE_WAY_TO_CUSTOMER, CANCELED))`, `ON_THE_WAY_TO_CUSTOMER(Set.of(DELIVERED, CANCELED))`, `DELIVERED(Collections.emptySet())`, `CANCELED(Collections.emptySet())`. Add a public method `public Set<Status> getAllowedTransitions() { return allowedTransitions; }`. Add a public method `public boolean canTransitionTo(Status target) { return allowedTransitions.contains(target); }`. Import `java.util.Set` and `java.util.Collections`. Note: Since enum values reference each other, you may need to use a static initializer block or lazy initialization instead of constructor parameters. One approach: use a `static { }` block after all enum values are defined to set up the transitions map. Alternatively, use a switch expression in `getAllowedTransitions()` that returns the set for each status — this avoids the forward-reference problem.

- [ ] T072 [P] [US8] Create `src/main/java/com/app/ecommerce/shared/exception/InvalidStateTransitionException.java`: A `RuntimeException` subclass. Constructor: `InvalidStateTransitionException(Status currentStatus, Status requestedStatus)`. The message should be: `"Cannot transition from " + currentStatus + " to " + requestedStatus + ". Allowed transitions from " + currentStatus + ": " + (currentStatus.getAllowedTransitions().isEmpty() ? "(none — terminal state)" : currentStatus.getAllowedTransitions())`. Store `currentStatus` and `requestedStatus` as fields for the exception handler.

- [ ] T073 [US8] Modify `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java` `updateOrder()` method: Before applying the delivery status change, add validation: if `request.getDeliveryStatus() != null`, get the current status from `order.getDeliveryInfo().getStatus()`, call `currentStatus.canTransitionTo(request.getDeliveryStatus())`. If false, throw `new InvalidStateTransitionException(currentStatus, request.getDeliveryStatus())`. Only apply the status change if validation passes.

- [ ] T074 [US8] Modify `src/main/java/com/app/ecommerce/shared/exception/RestExceptionHandler.java`: Add `@ExceptionHandler(InvalidStateTransitionException.class)` handler. Return 400 BAD_REQUEST with `ErrorResponseDto.badRequest(exception.getMessage(), request.getRequestURI())`.

**Checkpoint**: Invalid order status transitions are rejected with a clear error message listing allowed transitions. Terminal states (DELIVERED, CANCELED) cannot transition to anything.

---

## Phase 8: User Story 9 — Application Health and Observability (Priority: P3)

**Goal**: Expose health check and metrics endpoints via Spring Boot Actuator.

**Independent Test**: Hit `/ecommerce/api/v1/actuator/health` and verify it reports database and Redis status.

### Implementation for User Story 9

- [ ] T075 [US9] Verify `spring-boot-starter-actuator` dependency was added in T001. If not, add it now to `pom.xml`.

- [ ] T076 [US9] Verify actuator configuration was added in T002. If not, add to `src/main/resources/application.yml`: under `management.endpoints.web.exposure.include: health,info,metrics`, `management.endpoint.health.show-details: always`, `management.health.redis.enabled: true`, `management.health.db.enabled: true`. Also add `management.endpoints.web.base-path: /actuator` to ensure the actuator endpoints are accessible under the application context path.

- [ ] T077 [US9] Verify the security configuration (T060) allows `/actuator/health` as public and `/actuator/**` as ADMIN-only. If not, update `SecurityConfig.java`.

**Checkpoint**: `/actuator/health` returns database and Redis health status. `/actuator/metrics` (admin-only) exposes JVM, HTTP, and cache metrics.

---

## Phase 9: User Story 10 — Idempotent Write Operations (Priority: P3)

**Goal**: Prevent duplicate order creation when the same request is retried.

**Independent Test**: Send POST /orders twice with the same Idempotency-Key header — verify only one order is created and both responses are identical.

### Implementation for User Story 10

- [ ] T078 [P] [US10] Create `src/main/java/com/app/ecommerce/shared/idempotency/IdempotencyRecord.java`: JPA entity. Fields: `@Id @GeneratedValue(strategy = GenerationType.UUID) UUID id`, `@Column(unique = true, nullable = false, length = 255) String idempotencyKey`, `@Column(nullable = false) int httpStatus`, `@Column(nullable = false, columnDefinition = "TEXT") String responseBody` (the cached JSON response), `@Column(nullable = false) Instant createdAt`, `@Column(nullable = false) Instant expiresAt`. Use `@Entity`, `@Table(name = "idempotency_records")`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`. Do NOT extend BaseEntity (no audit fields needed for infrastructure records). Set `createdAt` in `@PrePersist` and `expiresAt = createdAt.plus(24, ChronoUnit.HOURS)`.

- [ ] T079 [P] [US10] Create `src/main/java/com/app/ecommerce/shared/idempotency/IdempotencyRepository.java`: Interface extending `JpaRepository<IdempotencyRecord, UUID>`. Methods: `Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);`, `void deleteByExpiresAtBefore(Instant now);`.

- [ ] T080 [US10] Create `src/main/java/com/app/ecommerce/shared/idempotency/IdempotencyService.java`: A `@Service` `@RequiredArgsConstructor` `@Slf4j` class. Inject `IdempotencyRepository` and `ObjectMapper` (Jackson). Methods: (1) `Optional<IdempotencyRecord> findByKey(String key)` — call `idempotencyRepository.findByIdempotencyKey(key)`. Filter out expired records (where `expiresAt.isBefore(Instant.now())`). (2) `void store(String key, int httpStatus, Object responseBody)` — serialize `responseBody` to JSON string using `objectMapper.writeValueAsString()`. Create and save `IdempotencyRecord`. (3) Add `@Scheduled(fixedRate = 3600000)` on a method `cleanupExpiredKeys()` that calls `idempotencyRepository.deleteByExpiresAtBefore(Instant.now())` and logs the cleanup. Add `@Transactional` on this method.

- [ ] T081 [US10] Enable scheduling in the application: Add `@EnableScheduling` to `src/main/java/com/app/ecommerce/EcommerceApplication.java` (the main class) or to a dedicated configuration class. If using the main class, just add the annotation next to `@SpringBootApplication`.

- [ ] T082 [US10] Modify `src/main/java/com/app/ecommerce/order/OrderControllerImpl.java` `createNewOrder()` method: (1) Add a parameter `@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey`. (2) At the start of the method, if `idempotencyKey != null`: call `idempotencyService.findByKey(idempotencyKey)`. If present, deserialize the stored `responseBody` back to `ApiResponseDto` and return it with the stored `httpStatus` — `return ResponseEntity.status(record.getHttpStatus()).body(objectMapper.readValue(record.getResponseBody(), ApiResponseDto.class))`. (3) If not present (or key is null): proceed with normal order creation. After creating the order and building the response, if `idempotencyKey != null`: call `idempotencyService.store(idempotencyKey, 201, responseDto)`. (4) Inject `IdempotencyService` and `ObjectMapper` into the controller. (5) Update the corresponding `OrderController.java` interface to include the `@RequestHeader` parameter.

- [ ] T083 [US10] Modify `src/main/java/com/app/ecommerce/order/OrderController.java` (interface): Add `@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey` parameter to the `createNewOrder` method signature. Add `@Parameter(description = "Unique key for idempotent order creation. If the same key is sent twice within 24 hours, the second request returns the original response without creating a new order.")` OpenAPI annotation on the parameter.

**Checkpoint**: Duplicate POST /orders requests with the same Idempotency-Key create only one order. Expired keys (>24h) are cleaned up hourly.

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Final cleanup, verification, and documentation updates.

- [ ] T084 Delete any remaining old DTO files that were replaced but not yet removed in T039. Search the entire codebase for any imports of `ProductDto`, `CategoryDto`, `OrderDto`, `DeliveryInfoDto`, `BaseDto`. Remove the files and fix any remaining references.

- [ ] T085 Verify all OpenAPI/Swagger annotations are updated to reference new request/response types. Check that Swagger UI at `/swagger-ui` shows correct schemas for all endpoints including the new `/auth/**` endpoints.

- [ ] T086 Review `src/main/java/com/app/ecommerce/shared/config/HttpLoggingConfig.java`: Ensure the `CommonsRequestLoggingFilter` does NOT log the `Authorization` header value. If the current configuration includes all headers, add a header predicate or note that `CommonsRequestLoggingFilter` already masks sensitive headers (verify by checking Spring Boot 3.0 behavior). If it does log Authorization, configure `setHeaderPredicate` to exclude it.

- [ ] T087 Run `mvn clean compile` to verify the entire project compiles without errors. Fix any remaining issues.

- [ ] T088 Run `mvn spring-boot:run` to verify the application starts successfully. Check that Hibernate creates/alters the expected tables (users, tokens, idempotency_records, plus column type changes on Product.price, Order.total_price, DeliveryInfo.delivery_date, and new version columns).

- [ ] T089 Run the quickstart.md verification steps: (1) Register a user via POST /auth/register. (2) Login via POST /auth/login. (3) Use the access token to create a product (ADMIN). (4) Verify GET /products works without a token (public). (5) Verify POST /products without a token returns 401. (6) Verify PATCH /products/{id} with a stale version returns 409. (7) Verify invalid order status transition returns 400. (8) Verify /actuator/health returns component statuses. (9) Verify duplicate order with same Idempotency-Key returns same response.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 (needs new dependencies) — BLOCKS all user stories
- **US3 DTO Split (Phase 3)**: Depends on Phase 2 (entity types changed) — BLOCKS US1, US5, US6
- **US1 Transactions (Phase 4)**: Depends on Phase 3 (service methods must have final signatures)
- **US6 Caching (Phase 5)**: Depends on Phase 3 (cache annotations on final methods)
- **US4 Security (Phase 6)**: Depends on Phase 1 (security dependency) — can run in parallel with Phases 3-5 if interfaces are stable
- **US8 State Machine (Phase 7)**: Depends on Phase 3 (UpdateOrderRequest type) — can run in parallel with Phase 6
- **US9 Observability (Phase 8)**: Depends on Phase 1 + Phase 6 (actuator security)
- **US10 Idempotency (Phase 9)**: Depends on Phase 3 (OrderController types) + Phase 6 (auth for orders)
- **Polish (Phase 10)**: Depends on all previous phases

### Critical Path

```
Phase 1 → Phase 2 → Phase 3 → Phase 4 (parallel with Phase 5)
                                      → Phase 6 → Phase 8
                                      → Phase 7
                                      → Phase 9
                                      → Phase 10
```

### User Story Dependencies

- **US3 (P1)**: Can start after Phase 2 — No dependencies on other stories. **Start here.**
- **US1 (P1)**: Can start after US3 (needs final service method signatures)
- **US6 (P2)**: Can start after US3 (needs final cache method signatures)
- **US4 (P2)**: Technically independent but easier after US3 (controller types are final)
- **US5 (P2)**: Completed as part of US3 (validation on request DTOs)
- **US7 (P2)**: Completed as part of Phase 2 (entity cleanup)
- **US8 (P3)**: Requires US3 (UpdateOrderRequest type) + simple enum change
- **US9 (P3)**: Requires Phase 1 + Phase 6 (actuator needs security config)
- **US10 (P3)**: Requires US3 (order controller types) + US4 (auth for orders)

### Within Each Phase

- Tasks marked [P] can run in parallel (different files, no dependencies)
- Models/entities before mappers before services before controllers
- Verify compilation after each phase

### Parallel Opportunities

**Within Phase 2 (Foundational)**:
- T003, T004, T005, T006, T007, T008 can all run in parallel (different entity files)

**Within Phase 3 (DTO Split)**:
- T012-T022 (all new DTO files) can run in parallel
- T023-T026 (mapper updates) can run in parallel
- T027-T032 (service updates) must be sequential per domain but parallel across domains
- T033-T038 (controller updates) can be parallel across domains

**Within Phase 4 (Transactions)**:
- T042-T046 can all run in parallel (different service files)

**Within Phase 5 (Caching)**:
- T049-T051 can all run in parallel (different service files)

**Within Phase 6 (Security)**:
- T052-T056, T061-T064 can all run in parallel (entity/DTO files)

---

## Parallel Example: Phase 3 DTO Creation

```bash
# Launch all response DTOs in parallel:
Task T012: "Create BaseResponse.java"
Task T013: "Create ProductResponse.java"
Task T014: "Create CategoryResponse.java"
Task T015: "Create OrderResponse.java"
Task T016: "Create DeliveryInfoResponse.java"

# Launch all request DTOs in parallel:
Task T017: "Create CreateProductRequest.java"
Task T018: "Create UpdateProductRequest.java"
Task T019: "Create CreateCategoryRequest.java"
Task T020: "Create UpdateCategoryRequest.java"
Task T021: "Create CreateOrderRequest.java"
Task T022: "Create UpdateOrderRequest.java"
```

---

## Implementation Strategy

### MVP First (Phase 1 + 2 + 3 Only)

1. Complete Phase 1: Setup (pom.xml, application.yml)
2. Complete Phase 2: Entity fixes (BigDecimal, @Version, LAZY, cleanup)
3. Complete Phase 3: DTO split with validation
4. **STOP and VALIDATE**: All endpoints work with new request/response types, validation returns field-level errors, partial updates work
5. This alone delivers: correct data types, proper API contracts, input validation, optimistic locking fields (not yet enforced)

### Incremental Delivery

1. Phase 1 + 2 + 3 → Foundation + DTO MVP → Validate
2. + Phase 4 → Transactions + locking enforced → Validate concurrent updates
3. + Phase 5 → Cache optimized → Validate targeted eviction
4. + Phase 6 → API secured → Validate auth flows
5. + Phase 7 + 8 + 9 → State machine + observability + idempotency → Production-ready
6. Phase 10 → Final polish

---

## Notes

- [P] tasks = different files, no dependencies between them
- [Story] label maps task to specific user story for traceability
- US5 (Input Validation) is merged into US3 — validation annotations are added on the new request DTOs
- US7 (Entity Cleanup) is merged into Phase 2 — entity fixes are foundational
- Every task includes exact file path and specific code changes
- `@ToString.Exclude` should be KEPT on bidirectional relationships even after `@JsonIgnore` removal — it prevents Lombok infinite recursion
- When updating MapStruct mappers, keep old methods temporarily until all callers are migrated, then delete in T084
- Commit after each phase completion for safe rollback points
- Run `mvn clean compile` after each phase to catch errors early
