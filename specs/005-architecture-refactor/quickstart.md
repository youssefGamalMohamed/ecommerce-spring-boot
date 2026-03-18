# Quickstart: Architecture Refactor & Enhancement

**Branch**: `005-architecture-refactor` | **Date**: 2026-03-18

## Implementation Order

This refactor has strict dependency ordering. Follow the phases below — each phase builds on the previous.

---

### Phase 1: Entity Layer Foundation (No breaking API changes yet)

**Goal**: Fix data model issues that all subsequent work depends on.

**Steps**:
1. **Add `@Version` to entities** — Add `private Long version;` with `@Version` annotation to Product, Category, Order, Cart. This is non-breaking (Hibernate auto-manages the column).

2. **Change `double` → `BigDecimal`** — Update `Product.price` and `Order.totalPrice` to `BigDecimal` with `@Column(precision = 19, scale = 2)`. Run pre-migration verification queries (see data-model.md) before restarting the app.

3. **Change `DeliveryInfo.date`** — `String` → `LocalDate`. Verify existing data format with the query in data-model.md.

4. **Switch to `FetchType.LAZY`** — Change `Product.categories` and `Category.products` from `EAGER` to `LAZY`. Add `@EntityGraph(attributePaths = "categories")` to relevant repository methods.

5. **Remove `@JsonIgnore`** — Remove all `@JsonIgnore` and `@ToString.Exclude` annotations from entities that were masking circular references. Since entities are never serialized directly (MapStruct handles mapping), these are unnecessary.

**Verify**: Application starts, existing tests pass (if any), Swagger UI loads.

---

### Phase 2: DTO Split & Validation

**Goal**: Separate request/response contracts and add input validation.

**Steps**:
1. **Create request DTOs** — `CreateProductRequest`, `UpdateProductRequest`, `CreateCategoryRequest`, `UpdateCategoryRequest`, `CreateOrderRequest`, `UpdateOrderRequest` with bean validation annotations.

2. **Rename response DTOs** — `ProductDto` → `ProductResponse`, `CategoryDto` → `CategoryResponse`, `OrderDto` → `OrderResponse`, `DeliveryInfoDto` → `DeliveryInfoResponse`, `BaseDto` → `BaseResponse`. Add `version` field to responses.

3. **Update MapStruct mappers** — Add new mapping methods: `mapToEntity(CreateXxxRequest)`, `updateEntity(UpdateXxxRequest, @MappingTarget Entity)`, keep `mapToResponse(Entity)`.

4. **Update controller interfaces** — Change method signatures to use new request/response types. Change `PUT` to `PATCH` for update operations.

5. **Update controller implementations** — Wire new types through.

6. **Update service interfaces and implementations** — Accept request types, return response types.

**Verify**: All endpoints work via Swagger UI with new request/response shapes. Validation errors return field-level messages.

---

### Phase 3: Transactional Integrity & Optimistic Locking

**Goal**: Add transaction boundaries and concurrency safety.

**Steps**:
1. **Add `@Transactional`** — On all service implementation write methods. Add `@Transactional(readOnly = true)` on all read methods.

2. **Handle `OptimisticLockException`** — Add handler in `RestExceptionHandler` for `ObjectOptimisticLockingFailureException` → 409 Conflict with user-friendly message.

3. **Add `InvalidStateTransitionException`** — New custom exception for order state machine violations.

4. **Implement order state machine** — Add `getAllowedTransitions()` to `Status` enum. Validate transitions in `OrderServiceImpl.updateOrder()`.

**Verify**: Concurrent update test — send two updates to same product with same version, verify one succeeds and one gets 409. Invalid order transitions return 400.

---

### Phase 4: Cache Optimization

**Goal**: Replace full-cache eviction with targeted invalidation.

**Steps**:
1. **Replace `@CacheEvict(allEntries = true)`** with `@CachePut(key = "#result.id")` on save/update methods and `@CacheEvict(key = "#id")` on delete methods.

2. **Verify cache behavior** — Update one product, verify other cached products are still available without cache miss.

**Verify**: Redis MONITOR shows only targeted key operations, not FLUSHDB or full key scans.

---

### Phase 5: Security (Spring Security + JWT)

**Goal**: Secure the API with authentication and authorization.

**Steps**:
1. **Add dependencies** — `spring-boot-starter-security`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson` to pom.xml.

2. **Create User and Token entities** — With repositories.

3. **Create `JwtService`** — Token generation, validation, claim extraction.

4. **Create `SecurityUserDetailsService`** — Loads user from DB.

5. **Create `JwtAuthenticationFilter`** — Extracts token from header, validates, sets SecurityContext.

6. **Create `SecurityConfig`** — Filter chain with whitelisted endpoints, CORS, CSRF disabled, stateless sessions.

7. **Create `AuthControllerImpl`** — Register, login, refresh-token endpoints.

8. **Update `JpaConfig.auditorAware()`** — Pull username from `SecurityContextHolder` instead of hardcoded "SYSTEM_USER".

9. **Update OpenAPI config** — Security scheme already configured, verify it works with real JWT.

**Verify**: Register user → login → use token to access protected endpoints. Verify 401/403 for unauthorized access. Verify audit fields show real username.

---

### Phase 6: Idempotency & Observability

**Goal**: Add production-readiness features.

**Steps**:
1. **Create `IdempotencyRecord` entity** — With repository and service.

2. **Add idempotency to order creation** — Check `Idempotency-Key` header in `OrderControllerImpl.createNewOrder()`.

3. **Add scheduled cleanup** — `@Scheduled` job to delete expired idempotency records.

4. **Add Actuator** — `spring-boot-starter-actuator` dependency. Configure exposed endpoints in application.yml.

5. **Secure Actuator** — `/actuator/health` public, `/actuator/metrics` admin-only.

**Verify**: Duplicate order requests with same idempotency key create only one order. Health endpoint reports DB and Redis status. Metrics endpoint shows request counts.

---

### Phase 11: Dead Code Removal, ResponseEntity Builders, DRY Cleanup

**Goal**: Remove dead DTOs, migrate to ResponseEntity builder pattern, extract duplicated sort utility, fix missing @Valid.

**Steps**:
0. **CRITICAL: Fix CascadeType.REMOVE on Category.products** — In `Category.java`, remove `CascadeType.REMOVE` from the `@ManyToMany` cascade list. Currently, deleting a category cascades to delete all associated products — catastrophic in ecommerce.

1. **Delete dead DTO files (ProductDto, CategoryDto, OrderDto, DeliveryInfoDto, BaseDto)** — `ProductDto.java`, `CategoryDto.java`, `OrderDto.java`, `DeliveryInfoDto.java`, `BaseDto.java`. These were replaced by `*Response.java` and `BaseResponse.java` but never deleted.

1b. **Rename remaining Dto-suffixed classes for naming harmony** — `ApiResponseDto` → `ApiResponse`, `ErrorResponseDto` → `ErrorResponse`, `CartDto` → `CartResponse`, `CartItemDto` → `CartItemResponse`. Update all imports, references, and mapper method names across the codebase (~151 occurrences in 11 files).

2. **Remove dead mapper methods** — Delete all `mapToDto()`, `mapToDtos()`, `mapToEntity(*Dto)`, `updateFrom()` methods from `ProductMapper`, `CategoryMapper`, `OrderMapper`, `DeliveryInfoMapper` that reference the deleted DTOs. Also remove `CategoryMapper.INSTANCE` field.

3. **Migrate `new ResponseEntity<>` to builder pattern** — In all controllers (`ProductControllerImpl`, `CategoryControllerImpl`, `OrderControllerImpl`, `AuthControllerImpl`) and `RestExceptionHandler`, replace `new ResponseEntity<>(body, HttpStatus.XXX)` with `ResponseEntity.status(HttpStatus.XXX).body(body)` or `ResponseEntity.ok(body)`.

4. **Extract `SortUtils`** — Create `src/main/java/com/app/ecommerce/shared/util/SortUtils.java` with a static `sanitize(Sort, Set<String>, Sort.Order)` method. Update `ProductServiceImpl`, `CategoryServiceImpl`, `OrderServiceImpl` to use it. Delete the private `sanitizeSort` methods.

5. **Add missing `@Valid`** — Add `@Valid` to `CreateOrderRequest` and `UpdateOrderRequest` parameters in both `OrderController.java` (interface) and `OrderControllerImpl.java`.

6. **Remove unused `throws JsonProcessingException`** — From `OrderService.createNewOrder()` and `OrderServiceImpl.createNewOrder()`.

**Verify**: `mvn clean compile` succeeds. No references to deleted DTOs remain. All ResponseEntity usages use builder pattern. Swagger UI loads correctly.

---

## Key Files to Modify (Summary)

| File | Changes |
|------|---------|
| `pom.xml` | Add spring-boot-starter-security, jjwt, spring-boot-starter-actuator |
| `application.yml` | Add JWT secret, actuator config, security logging |
| All entities | `@Version`, BigDecimal, LocalDate, remove @JsonIgnore, LAZY fetch |
| All DTOs | Split into request/response, add validation annotations |
| All mappers | New mapping methods for request/response types |
| All controllers | New request/response types, PATCH for updates |
| All services | `@Transactional`, new method signatures |
| `RestExceptionHandler` | Add OptimisticLock, AccessDenied, InvalidStateTransition handlers |
| `JpaConfig` | AuditorAware from SecurityContext |
| `ErrorResponseDto` | Add conflict() factory method |

## New Files

| File | Purpose |
|------|---------|
| `auth/*` | Entire auth domain (User, Token, AuthController, AuthService, JwtService, etc.) |
| `shared/config/SecurityConfig.java` | Spring Security filter chain |
| `shared/security/*` | JWT filter, JWT service, UserDetailsService |
| `shared/idempotency/*` | IdempotencyRecord, repository, service |
| `shared/exception/InvalidStateTransitionException.java` | Order state machine errors |
| `**/Create*Request.java` | Create request DTOs per domain |
| `**/Update*Request.java` | Update request DTOs per domain |
| `**/*Response.java` | Response DTOs (renamed from *Dto) |
