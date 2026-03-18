# Research: Architecture Refactor & Enhancement

**Branch**: `005-architecture-refactor` | **Date**: 2026-03-18

## Research Findings

### R-001: Transactional Integrity Strategy

**Decision**: Add explicit `@Transactional` on all service methods — `@Transactional` for writes, `@Transactional(readOnly = true)` for reads.

**Rationale**: Currently, zero `@Transactional` annotations exist in the codebase. Spring Data JPA provides implicit transactions per repository call, but multi-step service methods (e.g., `ProductServiceImpl.save()` which calls `categoryService.getCategories()` + `productRepository.save()`) execute each repository call in a separate transaction. If the second call fails, the first is already committed — violating atomicity.

`readOnly = true` on reads enables Hibernate flush-mode MANUAL (skip dirty checking) and allows JDBC drivers to route to read replicas.

**Alternatives considered**:
- Programmatic `TransactionTemplate`: More flexible but verbose. Rejected — declarative `@Transactional` is sufficient for current use cases.
- Class-level `@Transactional`: Risks over-broad scope. Rejected — method-level gives precise control.

**Current state (confirmed via codebase scan)**:
- `ProductServiceImpl`: 0 `@Transactional`
- `CategoryServiceImpl`: 0 `@Transactional`
- `OrderServiceImpl`: 0 `@Transactional`
- `CartServiceImpl`: 0 `@Transactional`
- `CartItemServiceImpl`: 0 `@Transactional`

---

### R-002: BigDecimal for Monetary Values

**Decision**: Change `Product.price` (currently `double`) and `Order.totalPrice` (currently `double`) to `BigDecimal`. Map to MySQL `DECIMAL(19,2)` column type.

**Rationale**: IEEE 754 floating-point cannot represent many decimal fractions exactly. `0.1 + 0.2 = 0.30000000000000004`. For an ecommerce application, this causes incorrect totals, failed payment reconciliation, and customer billing disputes.

**Migration approach**: Hibernate `ddl-auto: update` will alter the column type from `DOUBLE` to `DECIMAL(19,2)`. MySQL's implicit `DOUBLE → DECIMAL` cast rounds to 2 decimal places. Since existing prices are likely clean values (e.g., 19.99, not 19.994567), this is safe. A pre-migration query (`SELECT * FROM Product WHERE CAST(price AS DECIMAL(19,2)) != price`) can verify zero data loss.

**Alternatives considered**:
- `long` (cents): Common pattern, avoids BigDecimal verbosity. Rejected — BigDecimal is more idiomatic in Java and aligns with JPA/Jackson ecosystem. Cents-based representation requires manual conversion at every API boundary.
- `MonetaryAmount` (JSR 354): Overly heavy dependency for current scope. Rejected — BigDecimal is sufficient.

**Affected files**:
- `Product.java`: `double price` → `BigDecimal price` + `@Column(precision = 19, scale = 2)`
- `Order.java`: `double totalPrice` → `BigDecimal totalPrice` + `@Column(precision = 19, scale = 2)`
- `ProductSpecifications.java`: `Double minPrice/maxPrice` → `BigDecimal`
- All DTOs referencing price/totalPrice
- All MapStruct mappers (no conversion needed — BigDecimal maps to BigDecimal)

---

### R-003: Request/Response DTO Split Strategy

**Decision**: Create separate request and response DTOs per domain entity. Request DTOs contain only writable fields with bean validation annotations. Response DTOs contain all fields including system-generated metadata.

**Rationale**: Current state — single DTO per entity (e.g., `ProductDto`) extends `BaseDto` (which has audit fields). Clients can send `id`, `createdAt`, etc. in requests — these are silently ignored but create API confusion. Single DTOs also prevent per-operation validation (create requires `name`, update doesn't).

**Naming convention**:
- `Create{Entity}Request` (e.g., `CreateProductRequest`)
- `Update{Entity}Request` (e.g., `UpdateProductRequest`) — all fields nullable for partial updates
- `{Entity}Response` (e.g., `ProductResponse`) — replaces current `{Entity}Dto`
- `BaseResponse` — replaces current `BaseDto` (audit fields base class, for responses only)

**Affected entities and their DTOs**:

| Entity | Current DTO | New Request DTOs | New Response DTO |
|--------|-------------|------------------|------------------|
| Product | `ProductDto` | `CreateProductRequest`, `UpdateProductRequest` | `ProductResponse` |
| Category | `CategoryDto` | `CreateCategoryRequest`, `UpdateCategoryRequest` | `CategoryResponse` |
| Order | `OrderDto` | `CreateOrderRequest`, `UpdateOrderRequest` | `OrderResponse` |
| DeliveryInfo | `DeliveryInfoDto` | (embedded in order requests) | `DeliveryInfoResponse` |
| Cart | `CartDto` | (no create/update — read only) | `CartDto` (kept as-is) |
| CartItem | `CartItemDto` | (no create/update — read only) | `CartItemDto` (kept as-is) |

**Alternatives considered**:
- CQRS with separate read/write models: Overkill for current scope. Rejected.
- Single DTO with `@JsonView`: Adds complexity via Jackson views; doesn't solve the validation-per-operation problem. Rejected.

---

### R-004: Optimistic Locking Implementation

**Decision**: Add `@Version` field (`private Long version`) to `Product`, `Category`, `Order`, and `Cart` entities. Handle `OptimisticLockException` in `RestExceptionHandler` → 409 Conflict.

**Rationale**: No `@Version` fields exist on any entity. Without optimistic locking, concurrent updates silently overwrite each other (last-write-wins). For an ecommerce app, this means: admin A changes product price to $20, admin B changes description — B's save overwrites A's price change.

**Implementation approach**:
1. Add `@Version private Long version;` to entities
2. Expose `version` in response DTOs (clients must send it back on update)
3. Include `version` in update request DTOs
4. Handle `ObjectOptimisticLockingFailureException` (Spring's wrapper for JPA's `OptimisticLockException`) in `RestExceptionHandler` → 409 with message "Resource was modified by another user. Please refresh and try again."

**Alternatives considered**:
- Pessimistic locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`): Causes database-level row locks, reduces throughput. Rejected — optimistic locking is better for low-contention read-heavy workloads.
- ETag-based: Semantic HTTP approach but adds complexity. Rejected — `@Version` is simpler and sufficient.

---

### R-005: Fetch Strategy Optimization

**Decision**: Change `Product.categories` and `Category.products` from `FetchType.EAGER` to `FetchType.LAZY`. Use `@EntityGraph` on repository methods that need related data.

**Rationale**: Current `FetchType.EAGER` on `Product.categories` means every product query (including paginated list of 20) triggers N additional queries for categories. With 20 products × 1 category query each = 21 queries instead of 1-2.

**Implementation approach**:
1. Change `@ManyToMany(fetch = FetchType.EAGER)` → `@ManyToMany(fetch = FetchType.LAZY)` on both sides
2. Add `@EntityGraph(attributePaths = "categories")` on `ProductRepository.findAll(Specification, Pageable)` and `findById(UUID)`
3. Same pattern for `CategoryRepository` if products are needed in the response

**Alternatives considered**:
- `JOIN FETCH` in JPQL: Less reusable than `@EntityGraph`. Rejected.
- Hibernate `@BatchSize`: Reduces N+1 to batched queries but doesn't eliminate extra queries entirely. Rejected as primary strategy (could complement).

---

### R-006: Cache Eviction Strategy

**Decision**: Replace `@CacheEvict(allEntries = true)` with targeted `@CacheEvict(key = "#id")` and `@CachePut(key = "#result.id")` patterns.

**Rationale**: Current state — every write operation (save, update, delete) evicts the ENTIRE cache for that entity type. With 1000 cached products, updating product #1 destroys the cache for products #2-#1000. Cache hit rate drops to near-zero under frequent writes.

**New pattern**:
- `save()`: `@CachePut(value = PRODUCTS, key = "#result.id")` — cache the new entry
- `updateById()`: `@CachePut(value = PRODUCTS, key = "#result.id")` — update the cached entry
- `deleteById()`: `@CacheEvict(value = PRODUCTS, key = "#productId")` — remove only that entry
- `findById()`: `@Cacheable(value = PRODUCTS, key = "#productId")` — keep as-is
- `findAll()`: No caching (paginated results are hard to invalidate correctly)

**Alternatives considered**:
- Cache-aside with explicit RedisTemplate: More control but more boilerplate. Rejected — Spring Cache abstraction is sufficient.
- Cache paginated results with composite key: Complex invalidation logic when underlying data changes. Rejected.

---

### R-007: Spring Security + JWT Integration

**Decision**: Add `spring-boot-starter-security` and `jjwt 0.11.5` dependencies. Implement `SecurityConfig` with `SecurityFilterChain`, `JwtAuthenticationFilter`, `JwtService`, and `SecurityUserDetailsService`.

**Rationale**: Zero security infrastructure exists. No `spring-boot-starter-security` in pom.xml, no JWT library, no security config, no auth controller. The OpenAPI config references JWT Bearer authentication but it's documentation-only.

**Architecture**:
1. `SecurityConfig`: Defines filter chain, whitelisted endpoints, CORS, CSRF disabled (stateless)
2. `JwtAuthenticationFilter` extends `OncePerRequestFilter`: Extracts JWT from `Authorization` header, validates, sets `SecurityContext`
3. `JwtService`: Generates access tokens (15min) and refresh tokens (7d), validates signatures, extracts claims
4. `SecurityUserDetailsService` implements `UserDetailsService`: Loads user by username from `UserRepository`
5. `AuthControllerImpl`: `/auth/register`, `/auth/login`, `/auth/refresh-token`
6. `JpaConfig.auditorAware()`: Updated to pull username from `SecurityContextHolder`

**Whitelisted endpoints** (per constitution):
- `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh-token`
- `GET /products/**`, `GET /categories/**` (public browsing)
- `/swagger-ui/**`, `/api-docs/**`, `/webjars/**`
- `/actuator/health` (infrastructure monitoring)

**Alternatives considered**:
- Spring Security OAuth2 Resource Server: Requires external auth server. Rejected — self-contained JWT is simpler for current scope.
- Session-based authentication: Violates constitution's stateless mandate. Rejected.

---

### R-008: Order State Machine

**Decision**: Implement a state transition map in the `Status` enum itself, with an `OrderStatusTransition` utility class.

**Rationale**: Currently any status can be set on any order without validation. The state machine is simple (4 states, ~5 transitions), so a full state machine library (Spring Statemachine) is overkill.

**State transitions**:
```
NOT_MOVED_OUT_FROM_WAREHOUSE → ON_THE_WAY_TO_CUSTOMER
NOT_MOVED_OUT_FROM_WAREHOUSE → CANCELED
ON_THE_WAY_TO_CUSTOMER → DELIVERED
ON_THE_WAY_TO_CUSTOMER → CANCELED
DELIVERED → (terminal, no transitions)
CANCELED → (terminal, no transitions)
```

**Implementation**: Add `getAllowedTransitions()` method to `Status` enum that returns a `Set<Status>`. `OrderServiceImpl.updateOrder()` checks `currentStatus.getAllowedTransitions().contains(newStatus)` before applying.

**Alternatives considered**:
- Spring Statemachine: Full state machine framework. Rejected — overkill for 4 states. Adds significant dependency.
- Strategy pattern with transition classes: Extensible but over-engineered for current needs. Could upgrade later.

---

### R-009: DeliveryInfo.date Type Migration

**Decision**: Change `DeliveryInfo.date` from `String` to `LocalDate`.

**Rationale**: String-typed dates prevent date comparison, sorting, and range queries. They also invite format inconsistency (is it "2026-03-18" or "03/18/2026" or "18-Mar-2026"?).

**Migration**: Hibernate `ddl-auto: update` will alter the column. Existing `String` values must be in a parseable format. If current data uses ISO-8601 (`yyyy-MM-dd`), MySQL will convert automatically. Otherwise, a manual data migration script is needed.

---

### R-010: Actuator & Observability

**Decision**: Add `spring-boot-starter-actuator` dependency. Expose `/actuator/health` (with details) and `/actuator/metrics`. Include Micrometer auto-configuration for JVM, HTTP, and cache metrics.

**Rationale**: No health or metrics endpoints exist. `spring-boot-starter-actuator` is not in pom.xml.

**Configuration**:
- Expose: `health`, `info`, `metrics`, `prometheus` (optional)
- Health indicators: `db` (DataSource), `redis` (RedisConnectionFactory), `diskSpace`
- Security: `/actuator/health` is public (infrastructure monitoring), all others require ADMIN role

---

### R-011: Idempotency Key Implementation

**Decision**: Custom `IdempotencyRecord` entity stored in MySQL with a `@Scheduled` cleanup job for expired keys (>24 hours).

**Rationale**: Network retries and client double-clicks can create duplicate orders. An idempotency key (UUID, sent via `Idempotency-Key` HTTP header) is checked before processing: if found, return cached response; if not, process and store.

**Implementation**:
1. `IdempotencyRecord` entity: `key` (unique), `responseBody` (JSON), `httpStatus`, `createdAt`
2. `IdempotencyService`: `check(key)` → Optional, `store(key, response)`
3. `OrderControllerImpl.createNewOrder()`: Check header, call service, store result
4. Scheduled cleanup: Delete records where `createdAt < now - 24h`

**Alternatives considered**:
- Redis-based idempotency: Faster but loses durability on Redis restart. Rejected — MySQL is the primary store and guarantees persistence.
- Database unique constraint on (idempotency_key, endpoint): Simpler but doesn't return cached response. Rejected — need to return same response for both calls.

---

### R-012: Input Validation Strategy

**Decision**: Add Jakarta Bean Validation annotations to all request DTOs. Validation handled by Spring's `@Valid` + existing `RestExceptionHandler.handleMethodArgumentNotValid()`.

**Rationale**: Current DTOs have zero validation annotations. The `@Valid` annotations exist on controller parameters but have nothing to validate against. The `RestExceptionHandler` already handles `MethodArgumentNotValidException` → 400 with field errors.

**Validation rules per entity**:

| Entity | Field | Validation |
|--------|-------|-----------|
| Product | name | `@NotBlank`, `@Size(max = 255)` |
| Product | price | `@NotNull`, `@DecimalMin("0.00")` |
| Product | quantity | `@NotNull`, `@Min(0)` |
| Product | categoryIds | `@NotEmpty` (at least one category) |
| Category | name | `@NotBlank`, `@Size(max = 100)` |
| Order | paymentType | `@NotNull` |
| Order | cart | `@NotNull`, `@Valid` (nested validation) |

**Cross-field validation** (minPrice ≤ maxPrice, startDate ≤ endDate) stays in the service layer as business logic — it's not expressible with standard bean validation annotations without a custom validator, which is unnecessary complexity.

---

### R-013: Entity Layer Cleanup

**Decision**: Remove all `@JsonIgnore` annotations from entities. Remove all `@JsonProperty` annotations. Entities must have zero Jackson annotations.

**Rationale**: Found `@JsonIgnore` on:
- `Product.cartItem` (line 52)
- `Category.products` (line 35)
- `Cart.order` (line 33)
- `CartItem.cart` (line 33)

These annotations exist to prevent circular serialization — but entities should NEVER be serialized directly. They go through MapStruct → DTO. The presence of `@JsonIgnore` is a code smell indicating possible entity leakage to the API layer. MapStruct ignores unmapped fields by default, so removing `@JsonIgnore` has no functional impact.

**Risk**: If any code path serializes entities directly (e.g., returning entity from controller without mapping), this will cause infinite recursion. Mitigation: search for all controller return types and verify they use DTOs.

---

### R-014: Dead DTO Classes & Mapper Methods (Post-Implementation Cleanup)

**Decision**: Delete 5 old DTO files and remove ~20 dead mapper methods that were superseded by the new Request/Response DTOs but never cleaned up.

**Rationale**: Task T039/T084 was supposed to delete old DTOs, but they still exist in the codebase alongside their replacements. No service or controller code references them — only the old mapper methods do, creating a circular dead-code dependency.

**Files to delete**:
- `src/main/java/com/app/ecommerce/product/ProductDto.java`
- `src/main/java/com/app/ecommerce/category/CategoryDto.java`
- `src/main/java/com/app/ecommerce/order/OrderDto.java`
- `src/main/java/com/app/ecommerce/order/DeliveryInfoDto.java`
- `src/main/java/com/app/ecommerce/shared/dto/BaseDto.java`

**Dead mapper methods to remove**:

| Mapper | Dead Methods |
|--------|-------------|
| `ProductMapper` | `mapToEntity(ProductDto)`, `mapToDto(Product)`, `mapToDtos(List)`, `mapToDtos(Set)`, `mapToEntity(Product)` (self-copy), `updateEntityFromEntity(Product, Product)` |
| `CategoryMapper` | `mapToEntity(CategoryDto)`, `mapToDto(Category)`, `mapToDtos(List)`, `updateFrom(Category, Category)`, `INSTANCE` field |
| `OrderMapper` | `mapToEntity(OrderDto)`, `mapToDto(Order)`, `mapToDtos(List)`, `mapToDtos(Set)`, `updateFrom(Order, Order)` |
| `DeliveryInfoMapper` | `mapToEntity(DeliveryInfoDto)`, `mapToDto(DeliveryInfo)`, `mapToDtos(List)`, `mapToDtos(Set)` |

**Note**: `CartDto`, `CartItemDto`, `CartMapper`, `CartItemMapper` are still actively used (cart domain was kept as-is per spec R-003). Do NOT delete these.

---

### R-015: ResponseEntity Builder Pattern

**Decision**: Replace all `new ResponseEntity<>(body, HttpStatus.XXX)` with `ResponseEntity.status(HttpStatus.XXX).body(body)` builder pattern across controllers and exception handler.

**Rationale**: The builder pattern is more readable, fluent, and aligns with Spring best practices. Currently 20+ usages of `new ResponseEntity<>()` exist across 4 controllers and 1 exception handler. Some methods already use `ResponseEntity.ok()` inconsistently — this creates an inconsistent code style.

**Pattern mapping**:

| Current Pattern | Builder Replacement |
|----------------|---------------------|
| `new ResponseEntity<>(body, HttpStatus.CREATED)` | `ResponseEntity.status(HttpStatus.CREATED).body(body)` |
| `new ResponseEntity<>(body, HttpStatus.OK)` | `ResponseEntity.ok(body)` |
| `new ResponseEntity<>(body, HttpStatus.NO_CONTENT)` | `ResponseEntity.status(HttpStatus.NO_CONTENT).body(body)` |
| `new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST)` | `ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)` |
| `new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND)` | `ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)` |
| `new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT)` | `ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)` |
| etc. | etc. |

**Affected files**:
- `ProductControllerImpl.java` (3 usages: save, updateById, deleteById)
- `CategoryControllerImpl.java` (2 usages: save, deleteById)
- `OrderControllerImpl.java` (3 usages: createNewOrder, updateOrder, findOrderById)
- `AuthControllerImpl.java` (1 usage: register)
- `RestExceptionHandler.java` (10 usages: all exception handler methods)

**Alternatives considered**:
- Keep `new ResponseEntity<>`: Functional but verbose and inconsistent with the `ResponseEntity.ok()` calls already present. Rejected.

---

### R-016: Duplicated `sanitizeSort` Utility Method

**Decision**: Extract the duplicated `sanitizeSort` method into a shared utility class `SortUtils` in `com.app.ecommerce.shared.util`.

**Rationale**: Three service implementations contain near-identical `sanitizeSort` methods:
- `ProductServiceImpl.sanitizeSort()` — allowed: `name, price, createdAt`
- `CategoryServiceImpl.sanitizeSort()` — allowed: `name, createdAt`
- `OrderServiceImpl.sanitizeSort()` — allowed: `totalPrice, createdAt`

The logic is identical: validate sort fields against an allow-list, fallback to a default. Only the allowed fields and default differ. This violates DRY and makes maintenance error-prone.

**Implementation**:
```java
public final class SortUtils {
    public static Sort sanitize(Sort sort, Set<String> allowedFields, Sort.Order defaultOrder) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.by(defaultOrder);
        }
        Sort.Order[] orders = sort.get()
            .map(order -> allowedFields.contains(order.getProperty()) ? order : defaultOrder)
            .toArray(Sort.Order[]::new);
        return Sort.by(orders);
    }
}
```

**Alternatives considered**:
- Leave duplicated: Simple but violates DRY. Rejected — 3 copies is over the threshold.
- Abstract base service class: Over-engineered. Rejected — a static utility is simpler.

---

### R-017: Missing `@Valid` on Order Controller Methods

**Decision**: Add `@Valid` annotation to `CreateOrderRequest` and `UpdateOrderRequest` parameters in `OrderControllerImpl`.

**Rationale**: Both `createNewOrder` and `updateOrder` accept request bodies without `@Valid`, meaning bean validation annotations on `CreateOrderRequest` (e.g., `@NotNull` on `paymentType`, `cartId`) and `UpdateOrderRequest` (`@NotNull` on `version`) are never triggered. This is a validation gap — invalid requests reach the service layer unchecked.

**Affected methods**:
- `OrderControllerImpl.createNewOrder()`: `@RequestBody CreateOrderRequest` → `@Valid @RequestBody CreateOrderRequest`
- `OrderControllerImpl.updateOrder()`: `@RequestBody UpdateOrderRequest` → `@Valid @RequestBody UpdateOrderRequest`
- `OrderController.java` (interface): Same changes to match implementation

---

### R-018: Unused `JsonProcessingException` in OrderServiceImpl

**Decision**: Remove `throws JsonProcessingException` from `OrderServiceImpl.createNewOrder()` and `OrderService.createNewOrder()`.

**Rationale**: `JsonProcessingException` was needed when the service handled idempotency internally. After the refactor, idempotency handling moved to the controller layer (`OrderControllerImpl`). The service method no longer performs any JSON processing, but the throws declaration persists — it forces all callers to handle it unnecessarily.

**Affected files**:
- `OrderService.java` (interface): Remove `throws JsonProcessingException` from `createNewOrder`
- `OrderServiceImpl.java`: Remove `throws JsonProcessingException` from `createNewOrder` + remove unused import

---

### R-022: Rename Remaining Dto-Suffixed Classes for Naming Harmony

**Decision**: Rename `ApiResponseDto` → `ApiResponse`, `ErrorResponseDto` → `ErrorResponse`, `CartDto` → `CartResponse`, `CartItemDto` → `CartItemResponse`. This aligns all response/wrapper classes with the naming convention established during the DTO split (R-003) where `ProductDto` → `ProductResponse`, `CategoryDto` → `CategoryResponse`, etc.

**Rationale**: After the DTO split in Phase 3, response classes use the `*Response` suffix (e.g., `ProductResponse`, `OrderResponse`), but 4 classes retained the legacy `Dto` suffix:
- `ApiResponseDto` — generic API wrapper used in every controller return type
- `ErrorResponseDto` — error response wrapper used in every exception handler
- `CartDto` — cart response (already extends `BaseResponse`, not `BaseDto`)
- `CartItemDto` — cart item response (already extends `BaseResponse`)

This creates inconsistent naming: `ResponseEntity<ApiResponseDto<ProductResponse>>` mixes `Dto` and `Response` in the same generic signature.

**Rename mapping**:

| Current Name | New Name | File Location | References |
|-------------|----------|---------------|------------|
| `ApiResponseDto` | `ApiResponse` | `shared/dto/ApiResponseDto.java` → `shared/dto/ApiResponse.java` | 9 files, ~85 occurrences |
| `ErrorResponseDto` | `ErrorResponse` | `shared/dto/ErrorResponseDto.java` → `shared/dto/ErrorResponse.java` | 6 files, ~66 occurrences |
| `CartDto` | `CartResponse` | `cart/CartDto.java` → `cart/CartResponse.java` | 10 files |
| `CartItemDto` | `CartItemResponse` | `cart/CartItemDto.java` → `cart/CartItemResponse.java` | 10 files |

**Full list of files requiring import/reference updates**:

For `ApiResponseDto` → `ApiResponse`:
- `shared/dto/ApiResponseDto.java` → rename file + class + all static methods
- `product/ProductController.java`, `product/ProductControllerImpl.java`
- `category/CategoryController.java`, `category/CategoryControllerImpl.java`
- `order/OrderController.java`, `order/OrderControllerImpl.java`
- `auth/AuthController.java`, `auth/AuthControllerImpl.java`

For `ErrorResponseDto` → `ErrorResponse`:
- `shared/dto/ErrorResponseDto.java` → rename file + class + all factory methods
- `shared/exception/RestExceptionHandler.java`
- `product/ProductController.java`
- `category/CategoryController.java`
- `order/OrderController.java`
- `auth/AuthController.java`

For `CartDto` → `CartResponse`:
- `cart/CartDto.java` → rename file + class
- `cart/CartMapper.java` (mapToDto → mapToResponse, mapToDtos → mapToResponseList/Set)
- `cart/CartService.java`, `cart/CartServiceImpl.java`
- `order/OrderResponse.java` (field type)
- `order/OrderDto.java` (dead — being deleted in T090)

For `CartItemDto` → `CartItemResponse`:
- `cart/CartItemDto.java` → rename file + class
- `cart/CartItemMapper.java` (mapToDto → mapToResponse, mapToDtos → mapToResponseList/Set)
- `cart/CartItemService.java`, `cart/CartItemServiceImpl.java`
- `cart/CartResponse.java` (field type — after CartDto rename)

**Alternatives considered**:
- Keep the Dto suffix on shared wrapper classes only: Creates a distinction between "domain response" and "infrastructure wrapper". Rejected — the `Dto` suffix is legacy and the user explicitly wants naming harmony.
- Rename to `ApiResponseWrapper` / `ErrorResponseWrapper`: More descriptive but longer. Rejected — `ApiResponse` / `ErrorResponse` is clean and consistent.

---

### R-023: Mapper Method Naming Convention — Architect's Analysis

**Question**: Should collection mapper methods like `mapToResponseList(List<Product>)` and `mapToResponseSet(Set<Product>)` use overloaded names like `mapToResponse(List<Product>)` or `mapToResponses(List<Product>)` for OOP consistency?

**Architect's Decision**: **Remove all explicit collection mapper methods entirely.** They are dead code — zero callers exist in the codebase. The codebase correctly uses `Page.map(mapper::mapToResponse)` with method references, which doesn't need declared collection methods.

**Rationale (Architect's perspective)**:

1. **Dead code analysis**: Grep confirmed zero calls to `mapToResponseList`, `mapToResponseSet`, or `mapToDtos` from any `*ServiceImpl.java`. The services use `repository.findAll(spec, pageable).map(mapper::mapToResponse)` — Spring Data's `Page.map()` applies the single-item mapper to each element via method reference.

2. **MapStruct behavior**: When MapStruct sees a single-item mapping method like `ProductResponse mapToResponse(Product product)`, it **automatically generates collection mapping implementations** when referenced by other mappers (e.g., `CategoryMapper` using `ProductMapper`). You don't need to declare them explicitly.

3. **OOP overloading evaluation**:
   - `mapToResponse(Product)` + `mapToResponse(List<Product>)` + `mapToResponse(Set<Product>)` = valid Java overloading (parameter types differ). MapStruct supports this.
   - `mapToResponse(Product)` + `mapToResponses(List<Product>)` = plural naming convention. Less "pure OOP" but avoids confusion.
   - **Neither is needed** because no code calls collection methods directly.

4. **When you WOULD need collection methods**: If a service method accepted `List<Entity>` and needed to return `List<Response>` in one call (not via Page.map()). Currently this never happens in the codebase — all list operations go through pagination.

**Decision matrix**:

| Approach | OOP Purity | Readability | Practical Need |
|----------|-----------|-------------|---------------|
| Overloaded `mapToResponse` for all | High | Medium (can confuse at call site) | None — dead code |
| Plural `mapToResponses` for collections | Medium | High (clear intent) | None — dead code |
| **Remove collection methods entirely** | N/A | Cleanest (less noise) | **Correct — no callers** |

**Recommendation**: Remove `mapToResponseList`, `mapToResponseSet`, `mapToResponseList` from all mappers. If a future use case needs explicit collection mapping, add it then with overloaded `mapToResponse` (OOP-correct approach). Don't pre-declare unused methods.

**Also apply to**: The old `mapToDtos` collection methods are being deleted in T091-T094 (already planned). The `mapToEntity` collection methods in `CartMapper` and `CartItemMapper` (`mapToEntity(CartDto)`, `mapToDtos`) should also follow this pattern after rename — keep only the actively-used single-item methods.

**Summary of mapper methods to KEEP per mapper** (after all cleanup):

| Mapper | Keep | Purpose |
|--------|------|---------|
| `ProductMapper` | `mapToEntity(CreateProductRequest)` | Create |
| | `mapToEntity(Product, Set<Category>)` | Set categories on entity |
| | `updateEntityFromRequest(UpdateProductRequest, Product)` | Partial update |
| | `updateEntityFromEntity(Product, Set<Category>, Product)` | Update categories |
| | `mapToResponse(Product)` | Entity → Response |
| `CategoryMapper` | `mapToEntity(CreateCategoryRequest)` | Create |
| | `updateEntityFromRequest(UpdateCategoryRequest, Category)` | Partial update |
| | `mapToResponse(Category)` | Entity → Response |
| `OrderMapper` | `mapToEntity(CreateOrderRequest)` | Create |
| | `updateEntityFromRequest(UpdateOrderRequest, Order)` | Partial update |
| | `mapToResponse(Order)` | Entity → Response |
| `DeliveryInfoMapper` | `mapToResponse(DeliveryInfo)` | Entity → Response |
| `CartMapper` | `mapToResponse(Cart)` | Entity → Response (renamed from mapToDto) |
| `CartItemMapper` | `mapToResponse(CartItem)` | Entity → Response (renamed from mapToDto) |

---

### R-019: CascadeType.REMOVE on Inverse Side of ManyToMany (CRITICAL)

**Decision**: Remove `CascadeType.REMOVE` from `Category.products` relationship.

**Rationale**: `Category.java` line 36 has:
```java
@ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY,
            cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
```
`CascadeType.REMOVE` on the **inverse (mappedBy) side** of a `@ManyToMany` is dangerous: deleting a category will cascade-delete all associated products. In an ecommerce system, this means removing the "Electronics" category would delete every product tagged as "Electronics" — catastrophic data loss.

The `mappedBy` side should never have `CascadeType.REMOVE`. Only `MERGE`, `DETACH`, and `REFRESH` are safe here.

**Fix**: Change to `cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}`.

---

### R-020: Missing Method-Level Authorization (@PreAuthorize)

**Decision**: Add `@PreAuthorize("hasRole('ADMIN')")` to all write endpoints (POST, PATCH, DELETE) on products and categories.

**Rationale**: `SecurityConfig.java` has `@EnableMethodSecurity` and URL-based rules that require authentication for all non-whitelisted endpoints. However, any authenticated user (CUSTOMER or ADMIN) can currently POST/PATCH/DELETE products and categories, because there is no **method-level role check**. The spec (FR-015) requires: "ADMIN (full CRUD on all resources) and CUSTOMER (read products/categories, manage own cart, place and view own orders)".

URL-based rules only distinguish between authenticated vs. unauthenticated — they don't check roles on specific HTTP methods. Adding `@PreAuthorize` on controller methods enforces the role distinction.

**Affected files**:
- `ProductController.java` / `ProductControllerImpl.java`: `save()`, `updateById()`, `deleteById()` → `@PreAuthorize("hasRole('ADMIN')")`
- `CategoryController.java` / `CategoryControllerImpl.java`: `save()`, `updateById()`, `deleteById()` → `@PreAuthorize("hasRole('ADMIN')")`

**Note**: Order endpoints are intentionally NOT restricted to ADMIN — customers can create and view their own orders (further ownership checks can be added later).

---

### R-021: Token Revocation Batch Update

**Decision**: Replace the loop-based token revocation with a single batch update query.

**Rationale**: `AuthServiceImpl.login()` (lines 84-88) revokes existing tokens in a loop:
```java
List<Token> validTokens = tokenRepository.findAllByUserAndRevokedFalseAndExpiredFalse(user);
for (Token token : validTokens) {
    token.setRevoked(true);
    tokenRepository.save(token);  // N individual saves
}
```
With N valid tokens, this executes N separate UPDATE queries. A single `@Modifying @Query("UPDATE Token t SET t.revoked = true WHERE t.user = :user AND t.revoked = false AND t.expired = false")` in `TokenRepository` reduces this to 1 query.

**Affected files**:
- `TokenRepository.java`: Add `revokeAllValidTokensByUser(User user)` method with `@Modifying @Query`
- `AuthServiceImpl.java`: Replace the loop with a single `tokenRepository.revokeAllValidTokensByUser(user)` call
