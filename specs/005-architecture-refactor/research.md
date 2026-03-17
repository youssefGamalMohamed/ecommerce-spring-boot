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
