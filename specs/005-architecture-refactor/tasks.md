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

- [x] T001 Add new Maven dependencies to `pom.xml`
- [x] T002 Add new configuration properties to `src/main/resources/application.yml`

---

## Phase 2: Foundational (Entity Layer Fixes)

**Purpose**: Fix data model issues (BigDecimal, @Version, FetchType.LAZY, remove @JsonIgnore, LocalDate) that ALL subsequent user stories depend on. These are cross-cutting changes shared by US1, US2, US6, US7.

**CRITICAL**: No user story work can begin until this phase is complete. All entity changes must be applied together to avoid partial inconsistency.

- [x] T003 Modify `src/main/java/com/app/ecommerce/product/Product.java`
- [x] T004 Modify `src/main/java/com/app/ecommerce/category/Category.java`
- [x] T005 Modify `src/main/java/com/app/ecommerce/order/Order.java`
- [x] T006 Modify `src/main/java/com/app/ecommerce/order/DeliveryInfo.java`
- [x] T007 Modify `src/main/java/com/app/ecommerce/cart/Cart.java`
- [x] T008 Modify `src/main/java/com/app/ecommerce/cart/CartItem.java`
- [x] T009 Modify `src/main/java/com/app/ecommerce/product/ProductRepository.java`
- [x] T010 Modify `src/main/java/com/app/ecommerce/product/ProductSpecifications.java`
- [x] T011 Verify the application compiles and starts after all entity changes

**Checkpoint**: Application compiles. Entities have @Version, BigDecimal prices, LocalDate delivery date, LAZY fetch, no @JsonIgnore. Database columns will be auto-migrated by Hibernate on next startup.

---

## Phase 3: User Story 3 — Separate Input and Output Contracts (Priority: P1) 🎯 MVP

**Goal**: Split single DTOs into separate request (create/update) and response DTOs with proper field exposure. This phase also incorporates US5 (Input Validation) since validation annotations go on the new request DTOs.

**Independent Test**: Send a create request with only writable fields and verify the response includes system-generated fields. Send a create request with an `id` field and verify it is ignored. Send an update with only changed fields and verify other fields are unchanged.

### Implementation for User Story 3

**Comment: We start with responses (rename existing DTOs) then create request DTOs, then update mappers, then update services, then update controllers. This order minimizes compilation errors.**

- [x] T012 [P] [US3] Create `src/main/java/com/app/ecommerce/shared/models/BaseResponse.java`
- [x] T013 [P] [US3] Create `src/main/java/com/app/ecommerce/product/ProductResponse.java`
- [x] T014 [P] [US3] Create `src/main/java/com/app/ecommerce/category/CategoryResponse.java`
- [x] T015 [P] [US3] Create `src/main/java/com/app/ecommerce/order/OrderResponse.java`
- [x] T016 [P] [US3] Create `src/main/java/com/app/ecommerce/order/DeliveryInfoResponse.java`
- [x] T017 [P] [US3] Create `src/main/java/com/app/ecommerce/product/CreateProductRequest.java`
- [x] T018 [P] [US3] Create `src/main/java/com/app/ecommerce/product/UpdateProductRequest.java`
- [x] T019 [P] [US3] Create `src/main/java/com/app/ecommerce/category/CreateCategoryRequest.java`
- [x] T020 [P] [US3] Create `src/main/java/com/app/ecommerce/category/UpdateCategoryRequest.java`
- [x] T021 [P] [US3] Create `src/main/java/com/app/ecommerce/order/CreateOrderRequest.java`
- [x] T022 [P] [US3] Create `src/main/java/com/app/ecommerce/order/UpdateOrderRequest.java`
- [x] T023 [US3] Modify `src/main/java/com/app/ecommerce/product/ProductMapper.java`
- [x] T024 [US3] Modify `src/main/java/com/app/ecommerce/category/CategoryMapper.java`
- [x] T025 [US3] Modify `src/main/java/com/app/ecommerce/order/OrderMapper.java`
- [x] T026 [US3] Modify `src/main/java/com/app/ecommerce/order/DeliveryInfoMapper.java`
- [x] T027 [US3] Modify `src/main/java/com/app/ecommerce/product/ProductService.java`
- [x] T028 [US3] Modify `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java`
- [x] T029 [US3] Modify `src/main/java/com/app/ecommerce/category/CategoryService.java`
- [x] T030 [US3] Modify `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java`
- [x] T031 [US3] Modify `src/main/java/com/app/ecommerce/order/OrderService.java`
- [x] T032 [US3] Modify `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java`
- [x] T033 [US3] Modify `src/main/java/com/app/ecommerce/product/ProductController.java`
- [x] T034 [US3] Modify `src/main/java/com/app/ecommerce/product/ProductControllerImpl.java`
- [x] T035 [US3] Modify `src/main/java/com/app/ecommerce/category/CategoryController.java`
- [x] T036 [US3] Modify `src/main/java/com/app/ecommerce/category/CategoryControllerImpl.java`
- [x] T037 [US3] Modify `src/main/java/com/app/ecommerce/order/OrderController.java`
- [x] T038 [US3] Modify `src/main/java/com/app/ecommerce/order/OrderControllerImpl.java`
- [x] T039 [US3] Delete the old DTO files
- [x] T040 [US3] Update `src/main/java/com/app/ecommerce/cart/CartDto.java` and `src/main/java/com/app/ecommerce/cart/CartItemDto.java`
- [x] T041 [US3] Verify the application compiles

**Checkpoint**: All endpoints use separate request/response DTOs. Create requests accept only writable fields with validation. Update requests support partial updates. Responses include all fields plus version and audit metadata. PUT is replaced by PATCH for updates.

---

## Phase 4: User Story 1 — Data Integrity on Write Operations (Priority: P1)

**Goal**: Add explicit @Transactional boundaries to all service methods and handle optimistic locking conflicts.

**Independent Test**: Simulate concurrent updates to the same product — one should succeed, the other should receive 409 Conflict.

### Implementation for User Story 1

- [x] T042 [P] [US1] Modify `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java`
- [x] T043 [P] [US1] Modify `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java`
- [x] T044 [P] [US1] Modify `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java`
- [x] T045 [P] [US1] Modify `src/main/java/com/app/ecommerce/cart/CartServiceImpl.java`
- [x] T046 [P] [US1] Modify `src/main/java/com/app/ecommerce/cart/CartItemServiceImpl.java`
- [x] T047 [US1] Modify `src/main/java/com/app/ecommerce/shared/exception/RestExceptionHandler.java`
- [x] T048 [US1] Modify `src/main/java/com/app/ecommerce/shared/models/ErrorResponseDto.java`

**Checkpoint**: All service methods have explicit transactional boundaries. Concurrent updates to the same entity trigger 409 Conflict via @Version optimistic locking. Read-only operations use `readOnly = true` for performance.

---

## Phase 5: User Story 6 — Optimized Data Loading and Caching (Priority: P2)

**Goal**: Replace allEntries=true cache eviction with targeted key-based eviction.

**Independent Test**: Update one product, verify other cached products remain without cache miss. Verify no N+1 queries on product list (already handled by @EntityGraph in Phase 2).

### Implementation for User Story 6

- [x] T049 [P] [US6] Modify `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java` cache annotations
- [x] T050 [P] [US6] Modify `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java` cache annotations
- [x] T051 [P] [US6] Modify `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java` cache annotations

**Checkpoint**: Cache eviction is targeted — updating product #1 does not invalidate cache for product #2. Delete evicts only the deleted entry. Save/update put the new value into cache.

---

## Phase 6: User Story 4 — Secure API Access (Priority: P2)

**Goal**: Implement JWT-based authentication with role-based access control. Secure all write endpoints, keep product/category browsing public.

**Independent Test**: Attempt to POST /products without a token — get 401. Login as ADMIN — POST succeeds. Login as CUSTOMER — POST /products returns 403.

### Implementation for User Story 4

- [x] T052 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/Role.java`
- [x] T053 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/User.java`
- [x] T054 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/Token.java`
- [x] T055 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/UserRepository.java`
- [x] T056 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/TokenRepository.java`
- [x] T057 [US4] Create `src/main/java/com/app/ecommerce/shared/security/JwtService.java`
- [x] T058 [US4] Create `src/main/java/com/app/ecommerce/shared/security/SecurityUserDetailsService.java`
- [x] T059 [US4] Create `src/main/java/com/app/ecommerce/shared/security/JwtAuthenticationFilter.java`
- [x] T060 [US4] Create `src/main/java/com/app/ecommerce/shared/config/SecurityConfig.java`
- [x] T061 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/RegisterRequest.java`
- [x] T062 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/LoginRequest.java`
- [x] T063 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/LoginResponse.java`
- [x] T064 [P] [US4] Create `src/main/java/com/app/ecommerce/auth/RefreshTokenRequest.java`
- [x] T065 [US4] Create `src/main/java/com/app/ecommerce/auth/AuthService.java`
- [x] T066 [US4] Create `src/main/java/com/app/ecommerce/auth/AuthServiceImpl.java`
- [x] T067 [US4] Create `src/main/java/com/app/ecommerce/auth/AuthController.java`
- [x] T068 [US4] Create `src/main/java/com/app/ecommerce/auth/AuthControllerImpl.java`
- [x] T069 [US4] Modify `src/main/java/com/app/ecommerce/shared/config/JpaConfig.java`
- [x] T070 [US4] Modify `src/main/java/com/app/ecommerce/shared/exception/RestExceptionHandler.java`

**Checkpoint**: All write endpoints require authentication. GET /products and GET /categories are public. ADMIN can CRUD everything. CUSTOMER can read products/categories and manage own orders. Audit fields record real usernames. JWT tokens issued on login, refresh supported.

---

## Phase 7: User Story 8 — Order Lifecycle State Management (Priority: P3)

**Goal**: Enforce valid order status transitions so invalid changes (e.g., CANCELED → DELIVERED) are rejected.

**Independent Test**: Try to update a CANCELED order to DELIVERED — should receive 400 with allowed transitions listed.

### Implementation for User Story 8

- [x] T071 [P] [US8] Modify `src/main/java/com/app/ecommerce/shared/enums/Status.java`
- [x] T072 [P] [US8] Create `src/main/java/com/app/ecommerce/shared/exception/InvalidStateTransitionException.java`
- [x] T073 [US8] Modify `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java`
- [x] T074 [US8] Modify `src/main/java/com/app/ecommerce/shared/exception/RestExceptionHandler.java`

**Checkpoint**: Invalid order status transitions are rejected with a clear error message listing allowed transitions. Terminal states (DELIVERED, CANCELED) cannot transition to anything.

---

## Phase 8: User Story 9 — Application Health and Observability (Priority: P3)

**Goal**: Expose health check and metrics endpoints via Spring Boot Actuator.

**Independent Test**: Hit `/ecommerce/api/v1/actuator/health` and verify it reports database and Redis status.

### Implementation for User Story 9

- [x] T075 [US9] Verify `spring-boot-starter-actuator` dependency was added in T001
- [x] T076 [US9] Verify actuator configuration was added in T002
- [x] T077 [US9] Verify the security configuration (T060) allows `/actuator/health` as public

**Checkpoint**: `/actuator/health` returns database and Redis health status. `/actuator/metrics` (admin-only) exposes JVM, HTTP, and cache metrics.

---

## Phase 9: User Story 10 — Idempotent Write Operations (Priority: P3)

**Goal**: Prevent duplicate order creation when the same request is retried.

**Independent Test**: Send POST /orders twice with the same Idempotency-Key header — verify only one order is created and both responses are identical.

### Implementation for User Story 10

- [x] T078 [P] [US10] Create `src/main/java/com/app/ecommerce/shared/idempotency/IdempotencyRecord.java`
- [x] T079 [P] [US10] Create `src/main/java/com/app/ecommerce/shared/idempotency/IdempotencyRepository.java`
- [x] T080 [US10] Create `src/main/java/com/app/ecommerce/shared/idempotency/IdempotencyService.java`
- [x] T081 [US10] Enable scheduling in the application
- [x] T082 [US10] Modify `src/main/java/com/app/ecommerce/order/OrderControllerImpl.java`
- [x] T083 [US10] Modify `src/main/java/com/app/ecommerce/order/OrderController.java`

**Checkpoint**: Duplicate POST /orders requests with the same Idempotency-Key create only one order. Expired keys (>24h) are cleaned up hourly.

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Final cleanup, verification, and documentation updates.

- [x] T084 Delete any remaining old DTO files that were replaced but not yet removed in T039. Search the entire codebase for any imports of `ProductDto`, `CategoryDto`, `OrderDto`, `DeliveryInfoDto`, `BaseDto`. Remove the files and fix any remaining references.

- [x] T085 Verify all OpenAPI/Swagger annotations are updated to reference new request/response types. Check that Swagger UI at `/swagger-ui` shows correct schemas for all endpoints including the new `/auth/**` endpoints.

- [x] T086 Review `src/main/java/com/app/ecommerce/shared/config/HttpLoggingConfig.java`: Ensure the `CommonsRequestLoggingFilter` does NOT log the `Authorization` header value. If the current configuration includes all headers, add a header predicate or note that `CommonsRequestLoggingFilter` already masks sensitive headers (verify by checking Spring Boot 3.0 behavior). If it does log Authorization, configure `setHeaderPredicate` to exclude it.

- [x] T087 Run `mvn clean compile` to verify the entire project compiles without errors. Fix any remaining issues.

- [x] T088 Run `mvn spring-boot:run` to verify the application starts successfully. Check that Hibernate creates/alters the expected tables (users, tokens, idempotency_records, plus column type changes on Product.price, Order.total_price, DeliveryInfo.delivery_date, and new version columns).

- [x] T089 Run the quickstart.md verification steps: (1) Register a user via POST /auth/register. (2) Login via POST /auth/login. (3) Use the access token to create a product (ADMIN). (4) Verify GET /products works without a token (public). (5) Verify POST /products without a token returns 401. (6) Verify PATCH /products/{id} with a stale version returns 409. (7) Verify invalid order status transition returns 400. (8) Verify /actuator/health returns component statuses. (9) Verify duplicate order with same Idempotency-Key returns same response.

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

---

## Common Implementation Issues and Fixes

This section documents issues encountered during implementation that weren't explicitly covered in the spec.

### 1. Enum Forward Reference Error (Status.java)
**Issue**: When using constructor parameters in enum that reference other enum values, Java throws "illegal forward reference" error.
**Example (BROKEN)**:
```java
NOT_MOVED_OUT_FROM_WAREHOUSE(Set.of(ON_THE_WAY_TO_CUSTOMER, CANCELED)),
ON_THE_WAY_TO_CUSTOMER(Set.of(DELIVERED, CANCELED)),
```
**Fix**: Use a static initializer block or a static Map to define transitions after all enum constants are declared:
```java
private static final Map<Status, Set<Status>> TRANSITIONS;
static {
    Map<Status, Set<Status>> map = new EnumMap<>(Status.class);
    map.put(NOT_MOVED_OUT_FROM_WAREHOUSE, Set.of(ON_THE_WAY_TO_CUSTOMER, CANCELED));
    // ...
    TRANSITIONS = Collections.unmodifiableMap(map);
}
```

### 2. Missing Import in Repository (IdempotencyRepository.java)
**Issue**: `deleteByExpiresAtBefore(Instant now)` method uses `Instant` but import is missing.
**Fix**: Add import:
```java
import java.time.Instant;
```

### 3. Missing @RequestHeader in Controller Interface (OrderController.java)
**Issue**: Implementation has `@RequestHeader("Idempotency-Key")` but interface doesn't declare it, causing override error.
**Fix**: Ensure both interface and implementation have matching method signatures:
```java
ResponseEntity<ApiResponseDto<OrderResponse>> createNewOrder(
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @Valid @RequestBody CreateOrderRequest request);
```

### 4. Missing MapStruct Import (CategoryMapper.java)
**Issue**: Using `Mappers.getMapper()` but `Mapper` and `Mappers` imports are missing.
**Fix**: Add explicit imports:
```java
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
```

### 5. Missing Lombok Annotation (CreateOrderRequest.java)
**Issue**: `@Setter` annotation missing `lombok.` prefix causing compilation error.
**Fix**: Ensure all Lombok annotations have proper prefixes:
```java
@Getter
@Setter
```

### 6. Warnings About @SuperBuilder Default Values (User.java, Token.java)
**Issue**: `@SuperBuilder` ignores initializing expressions like `enabled = true`.
**Fix**: Either add `@Builder.Default` or make field final:
```java
@Column(nullable = false)
@Builder.Default
private boolean enabled = true;
```

### 7. CartMapper Unmapped "version" Property
**Issue**: Cart entity now has `version` field but mapper doesn't map it.
**Fix**: Either add mapping or add `@Mapping(target = "version", ignore = true)` to ignore it.

### 8. Running Maven Build
**Command**: Always run after implementing tasks to verify compilation:
```bash
mvn clean install -DskipTests
```

### 9. HttpLoggingConfig Header Predicate Type Error
**Issue**: `Predicate<Object>` cannot be converted to `Predicate<String>`.
**Fix**: Use lambda with explicit String type:
```java
filter.setHeaderPredicate(headerName -> {
    return !headerName.equalsIgnoreCase("Authorization") &&
           !headerName.equalsIgnoreCase("Cookie") &&
           !headerName.equalsIgnoreCase("Set-Cookie");
});
```

### 10. Security Exception Handler Imports Missing
**Issue**: AccessDeniedException, BadCredentialsException, AuthenticationException handlers need imports.
**Fix**: Add imports:
```java
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
```

### 11. ErrorResponseDto Missing Factory Methods
**Issue**: Need `forbidden()` and `unauthorized()` factory methods for security handlers.
**Fix**: Add static methods:
```java
public static ErrorResponseDto forbidden(String message, String path) {
    return build(403, "FORBIDDEN", message, path);
}

public static ErrorResponseDto unauthorized(String message, String path) {
    return build(401, "UNAUTHORIZED", message, path);
}
```

### 12. Missing HTTP Method Annotations on Controller Implementation (AuthControllerImpl.java)
**Issue**: `AuthControllerImpl` had no `@PostMapping` annotations on any method. The interface defined only OpenAPI annotations (`@Operation`) with no Spring MVC routing annotations, and the implementation overrode the methods with no routing annotations either. Spring MVC therefore never registered `POST /auth/register`, `POST /auth/login`, or `POST /auth/refresh-token` as handlers.

**Why it caused 403 (not 404)**: In Spring Boot 3 / Spring Security 6, `requestMatchers` uses `MvcRequestMatcher`, which validates paths against **actually registered Spring MVC handlers** via `HandlerMappingIntrospector`. Because no handler was registered for `POST /auth/**`, the `permitAll()` rule in `SecurityConfig` never matched — the request fell through to `anyRequest().authenticated()`. With an invalid JWT (no authentication set) and anonymous authentication from Spring Security's anonymous filter, the framework returned 403 before the request ever reached the `DispatcherServlet`.

**Fix**: Added `@PostMapping("/register")`, `@PostMapping("/login")`, `@PostMapping("/refresh-token")` to the respective methods in `AuthControllerImpl.java`.

```java
@Override
@PostMapping("/register")
public ResponseEntity<ApiResponse<LoginResponse>> register(RegisterRequest request) { ... }

@Override
@PostMapping("/login")
public ResponseEntity<ApiResponse<LoginResponse>> login(LoginRequest request) { ... }

@Override
@PostMapping("/refresh-token")
public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(RefreshTokenRequest request) { ... }
```

**Root rule**: HTTP method annotations (`@GetMapping`, `@PostMapping`, `@PatchMapping`, `@DeleteMapping`) MUST always be present on the controller **implementation** method. Their absence does not cause a compile error or startup warning — it silently prevents the endpoint from being reachable, and in Spring Security 6 it additionally breaks `MvcRequestMatcher`-based `permitAll()` rules, producing 403 instead of the expected response.

### 13. Auth Endpoints Invisible in Swagger / No Self-Contained Token Flow (AuthController.java)
**Issue**: Two separate problems prevented a self-contained login-then-authorize flow in Swagger UI:

1. **Endpoints invisible** — Missing `@PostMapping` on `AuthControllerImpl` (see Issue #12) meant SpringDoc had no registered handlers to discover. The Authentication section simply did not appear in Swagger UI.

2. **Global JWT padlock on auth endpoints** — `OpenApiDocumentationConfig` calls `.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))` which applies the JWT Bearer scheme globally to every endpoint. Even after the handlers became visible (Issue #12 fix), the register/login/refresh-token endpoints showed a padlock in Swagger, implying a token was required to call them. This forced developers to use an external tool (Postman) to obtain a token before they could use Swagger at all.

**Fix**: Added `@SecurityRequirements` (empty — overrides the global security requirement for that endpoint) to each method in `AuthController.java` (the interface, where OpenAPI annotations belong):

```java
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@Operation(summary = "Login", description = "Authenticate a user")
@SecurityRequirements   // ← overrides global JWT requirement; endpoint shows as open
@io.swagger.v3.oas.annotations.responses.ApiResponses(...)
ResponseEntity<ApiResponse<LoginResponse>> login(...);
```

Applied to `register`, `login`, and `refreshToken`.

**Correct Swagger testing flow after fix**:
1. Open Swagger UI (`GET /swagger-ui`)
2. Expand **Authentication → POST /auth/login** (no padlock)
3. Execute with valid credentials → copy `accessToken` from the response body
4. Click **Authorize** (top-right) → paste the token → **Authorize**
5. All protected endpoints are now unlocked for the session — no Postman needed

**Root rule**: When `OpenApiDocumentationConfig` uses `.addSecurityItem(...)` globally, every public endpoint MUST override it with `@SecurityRequirements` (empty) on the controller interface method. Failing to do so misleads developers into thinking the endpoint requires a token.

### Implementation Status
- All tasks T001-T121 completed
- T088: Run application - requires user to execute
- T089: Verification steps - requires user to execute

---

## Phase 11: Dead Code Removal, ResponseEntity Builders, DRY Cleanup

**Purpose**: Post-implementation audit found dead DTOs, inconsistent ResponseEntity construction, duplicated sort logic, missing @Valid, unused exception declarations, Dto→Response naming harmony, critical cascade bug, missing authorization, and token revocation N+1. This phase cleans all of them.

**Research references**: R-014, R-015, R-016, R-017, R-018, R-019, R-020, R-021, R-022, R-023

---

### Sub-Phase 11A: Dead DTO & Mapper Cleanup (R-014)

- [x] T090 [P] Delete dead DTO files: `src/main/java/com/app/ecommerce/product/ProductDto.java`, `src/main/java/com/app/ecommerce/category/CategoryDto.java`, `src/main/java/com/app/ecommerce/order/OrderDto.java`, `src/main/java/com/app/ecommerce/order/DeliveryInfoDto.java`, `src/main/java/com/app/ecommerce/shared/models/BaseDto.java`

- [x] T091 [P] Clean `src/main/java/com/app/ecommerce/product/ProductMapper.java`: Remove ALL dead methods: `mapToEntity(ProductDto)`, `mapToDto(Product)`, `mapToDtos(List)`, `mapToDtos(Set)`, `mapToEntity(Product)` (self-copy), `updateEntityFromEntity(Product, Product)` (without categories). Also remove dead collection methods: `mapToResponseList(List)`, `mapToResponseSet(Set)` (zero callers — services use `Page.map(mapper::mapToResponse)` instead). **Keep only**: `mapToEntity(CreateProductRequest)`, `mapToEntity(Product, Set<Category>)`, `updateEntityFromRequest(UpdateProductRequest, Product)`, `updateEntityFromEntity(Product, Set<Category>, Product)`, `mapToResponse(Product)`.

- [x] T092 [P] Clean `src/main/java/com/app/ecommerce/category/CategoryMapper.java`: Remove `INSTANCE` field + `import Mappers`, dead methods: `mapToEntity(CategoryDto)`, `mapToDto(Category)`, `mapToDtos(List)`, `updateFrom(Category, Category)`. Also remove dead collection method: `mapToResponseList(List)` (zero callers). **Keep only**: `mapToEntity(CreateCategoryRequest)`, `updateEntityFromRequest(UpdateCategoryRequest, Category)`, `mapToResponse(Category)`.

- [x] T093 [P] Clean `src/main/java/com/app/ecommerce/order/OrderMapper.java`: Remove dead methods: `mapToEntity(OrderDto)`, `mapToDto(Order)`, `mapToDtos(List)`, `mapToDtos(Set)`, `updateFrom(Order, Order)`. Also remove dead collection method: `mapToResponseList(List)` (zero callers). **Keep only**: `mapToEntity(CreateOrderRequest)`, `updateEntityFromRequest(UpdateOrderRequest, Order)`, `mapToResponse(Order)`.

- [x] T094 [P] Clean `src/main/java/com/app/ecommerce/order/DeliveryInfoMapper.java`: Remove ALL dead methods: `mapToEntity(DeliveryInfoDto)`, `mapToDto(DeliveryInfo)`, `mapToDtos(List)`, `mapToDtos(Set)`. **Keep only**: `mapToResponse(DeliveryInfo)`.

**Checkpoint**: `mvn clean compile` succeeds. Zero references to old DTOs remain. All mappers contain only actively-used methods — no dead collection methods.

---

### Sub-Phase 11B: Rename Dto-Suffixed Classes for Naming Harmony (R-022)

**Purpose**: Rename `ApiResponseDto` → `ApiResponse`, `ErrorResponseDto` → `ErrorResponse`, `CartDto` → `CartResponse`, `CartItemDto` → `CartItemResponse` to achieve consistent naming across all request/response classes.

- [x] T111 Rename `src/main/java/com/app/ecommerce/shared/models/ApiResponseDto.java` → `ApiResponse.java`: Rename file, rename class to `ApiResponse`, update all static factory method references (`ApiResponse.success()`, `ApiResponse.created()`, `ApiResponse.noContent()`). Update `@Schema(description = ...)` annotation.

- [x] T112 Update all files that import/reference `ApiResponseDto` to use `ApiResponse` instead. Files: `ProductController.java`, `ProductControllerImpl.java`, `CategoryController.java`, `CategoryControllerImpl.java`, `OrderController.java`, `OrderControllerImpl.java`, `AuthController.java`, `AuthControllerImpl.java`. In each file: update import statement and all usages (return types, method calls).

- [x] T113 Rename `src/main/java/com/app/ecommerce/shared/models/ErrorResponseDto.java` → `ErrorResponse.java`: Rename file, rename class to `ErrorResponse`, update all static factory methods (`ErrorResponse.notFound()`, `ErrorResponse.badRequest()`, `ErrorResponse.conflict()`, `ErrorResponse.internalError()`, `ErrorResponse.serviceUnavailable()`, `ErrorResponse.forbidden()`, `ErrorResponse.unauthorized()`, `ErrorResponse.build()`).

- [x] T114 Update all files that import/reference `ErrorResponseDto` to use `ErrorResponse` instead. Files: `RestExceptionHandler.java`, `ProductController.java`, `CategoryController.java`, `OrderController.java`, `AuthController.java`. In each file: update import statement and all usages (return types, variable types).

- [x] T115 Rename `src/main/java/com/app/ecommerce/cart/CartDto.java` → `CartResponse.java`: Rename file, rename class to `CartResponse`. Update `@Schema(description = "Cart response")`.

- [x] T116 Update all files that reference `CartDto` to use `CartResponse`: `CartMapper.java` (rename `mapToDto(Cart)` → `mapToResponse(Cart)`, remove dead methods: `mapToEntity(CartDto)` → delete entirely, `mapToDtos(List)` → delete, `mapToDtos(Set)` → delete — none are called). Update `CartService.java`, `CartServiceImpl.java` (return type + method call). Update `OrderResponse.java` (field type `CartDto cart` → `CartResponse cart`). **CartMapper final state**: keep only `mapToResponse(Cart)`.

- [x] T117 Rename `src/main/java/com/app/ecommerce/cart/CartItemDto.java` → `CartItemResponse.java`: Rename file, rename class to `CartItemResponse`. Update `@Schema(description = "Cart item response")`.

- [x] T118 Update all files that reference `CartItemDto` to use `CartItemResponse`: `CartItemMapper.java` (rename `mapToDto(CartItem)` → `mapToResponse(CartItem)`, remove dead methods: `mapToEntity(CartItemDto)` → delete, `mapToDtos(List)` → delete, `mapToDtos(Set)` → delete — none are called). Update `CartItemService.java`, `CartItemServiceImpl.java` (return type + method call). Update `CartResponse.java` (field type `Set<CartItemDto>` → `Set<CartItemResponse>`). **CartItemMapper final state**: keep only `mapToResponse(CartItem)`.

- [x] T119 Verify no references to `ApiResponseDto`, `ErrorResponseDto`, `CartDto`, or `CartItemDto` remain in the codebase. Run: `grep -r "ApiResponseDto\|ErrorResponseDto\|CartDto\|CartItemDto" src/main/java/` should return zero results (except possibly comments which should also be updated).

**Checkpoint**: All response/wrapper classes use consistent naming — no `Dto` suffix remains on any response class. `ApiResponse`, `ErrorResponse`, `CartResponse`, `CartItemResponse` harmonize with `ProductResponse`, `CategoryResponse`, `OrderResponse`.

---

### Sub-Phase 11C: ResponseEntity Builder Pattern (R-015)
**Note**: These tasks must run AFTER Sub-Phase 11B (class renames) since the file names and class names will have changed.

- [x] T095 [P] Migrate `src/main/java/com/app/ecommerce/product/ProductControllerImpl.java`: Replace 3 `new ResponseEntity<>` usages with builder pattern.
  - `save()`: `new ResponseEntity<>(ApiResponse.created(...), HttpStatus.CREATED)` → `ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(...))`
  - `updateById()`: `new ResponseEntity<>(..., HttpStatus.OK)` → `ResponseEntity.ok(...)`
  - `deleteById()`: `new ResponseEntity<>(..., HttpStatus.NO_CONTENT)` → `ResponseEntity.status(HttpStatus.NO_CONTENT).body(...)`

- [x] T096 [P] Migrate `src/main/java/com/app/ecommerce/category/CategoryControllerImpl.java`: Replace 2 `new ResponseEntity<>` usages with builder pattern.
  - `save()`: → `ResponseEntity.status(HttpStatus.CREATED).body(...)`
  - `deleteById()`: → `ResponseEntity.status(HttpStatus.NO_CONTENT).body(...)`

- [x] T097 [P] Migrate `src/main/java/com/app/ecommerce/order/OrderControllerImpl.java`: Replace 3 `new ResponseEntity<>` usages with builder pattern.
  - `createNewOrder()`: → `ResponseEntity.status(HttpStatus.CREATED).body(...)`
  - `updateOrder()`: → `ResponseEntity.ok(...)`
  - `findOrderById()`: → `ResponseEntity.ok(...)`

- [x] T098 [P] Migrate `src/main/java/com/app/ecommerce/auth/AuthControllerImpl.java`: Replace 1 `new ResponseEntity<>` usage with builder pattern.
  - `register()`: → `ResponseEntity.status(HttpStatus.CREATED).body(...)`

- [x] T099 Migrate `src/main/java/com/app/ecommerce/shared/exception/RestExceptionHandler.java`: Replace all 10 `new ResponseEntity<>` usages with builder pattern.
  - `handleMethodArgumentNotValid()`: → `ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)`
  - `handleInternalServerErrorException()`: → `ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)`
  - `handleDuplicatedUniqueValueException()`: → `ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)`
  - `handleIllegalArgumentException()`: → `ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)`
  - `handleNoSuchElementException()`: → `ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)`
  - `handleFailedDatabaseConnectionException()`: → `ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse)`
  - `handleOptimisticLockingFailureException()`: → `ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)`
  - `handleInvalidStateTransitionException()`: → `ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)`
  - `handleAccessDeniedException()`: → `ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)`
  - `handleBadCredentialsException()`: → `ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)`
  - `handleAuthenticationException()`: → `ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)`

**Checkpoint**: Zero `new ResponseEntity<>` usages remain in the codebase. All ResponseEntity construction uses the fluent builder API.

---

### Sub-Phase 11D: DRY & Validation Fixes (R-016, R-017, R-018)

- [x] T100 Create `src/main/java/com/app/ecommerce/shared/util/SortUtils.java`:
  ```java
  package com.app.ecommerce.shared.util;

  import org.springframework.data.domain.Sort;
  import java.util.Set;

  public final class SortUtils {
      private SortUtils() {}

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

- [x] T101 [P] Update `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java`: Replace private `sanitizeSort` method with `SortUtils.sanitize(pageable.getSort(), Set.of("name", "price", "createdAt"), Sort.Order.desc("createdAt"))`. Delete the private method.

- [x] T102 [P] Update `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java`: Replace private `sanitizeSort` method with `SortUtils.sanitize(pageable.getSort(), Set.of("name", "createdAt"), Sort.Order.asc("name"))`. Delete the private method.

- [x] T103 [P] Update `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java`: Replace private `sanitizeSort` method with `SortUtils.sanitize(pageable.getSort(), Set.of("totalPrice", "createdAt"), Sort.Order.desc("createdAt"))`. Delete the private method. Also remove `throws JsonProcessingException` from `createNewOrder()` signature and remove unused `import com.fasterxml.jackson.core.JsonProcessingException;`.

- [x] T104 Update `src/main/java/com/app/ecommerce/order/OrderService.java`: Remove `throws JsonProcessingException` from `createNewOrder()` method signature. Remove unused `import com.fasterxml.jackson.core.JsonProcessingException;` if present.

- [x] T105 Update `src/main/java/com/app/ecommerce/order/OrderController.java` and `src/main/java/com/app/ecommerce/order/OrderControllerImpl.java`: Add `@Valid` annotation to `CreateOrderRequest` and `UpdateOrderRequest` parameters. Ensure `import jakarta.validation.Valid;` is present in both files.

- [x] T106 Run `mvn clean compile` to verify the entire project compiles without errors after all Phase 11 changes.

**Checkpoint**: `sanitizeSort` exists in only one place (SortUtils). `@Valid` is on all request parameters. No unused throws declarations. Build succeeds.

---

### Sub-Phase 11E: Critical Architecture Fixes (R-019, R-020, R-021)

- [x] T107 **CRITICAL** Fix `src/main/java/com/app/ecommerce/category/Category.java`: Remove `CascadeType.REMOVE` from the `@ManyToMany` annotation on `products` field. Change from `cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}` to `cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}`. This prevents catastrophic cascade deletion of products when a category is removed.

- [x] T108 [P] Add method-level authorization to product controllers. In `src/main/java/com/app/ecommerce/product/ProductController.java` (interface) and `src/main/java/com/app/ecommerce/product/ProductControllerImpl.java`: Add `@PreAuthorize("hasRole('ADMIN')")` to `save()`, `updateById()`, and `deleteById()` methods. Add `import org.springframework.security.access.prepost.PreAuthorize;`. **Note**: Moved @PreAuthorize to impl class for better readability - see Implementation Note 11.1.

- [x] T109 [P] Add method-level authorization to category controllers. In `src/main/java/com/app/ecommerce/category/CategoryController.java` (interface) and `src/main/java/com/app/ecommerce/category/CategoryControllerImpl.java`: Add `@PreAuthorize("hasRole('ADMIN')")` to `save()`, `updateById()`, and `deleteById()` methods. Add `import org.springframework.security.access.prepost.PreAuthorize;`. **Note**: Moved @PreAuthorize to impl class for better readability - see Implementation Note 11.1.

- [x] T110 Optimize token revocation in `src/main/java/com/app/ecommerce/auth/TokenRepository.java`: Add batch update method:
  ```java
  @Modifying
  @Query("UPDATE Token t SET t.revoked = true WHERE t.user = :user AND t.revoked = false AND t.expired = false")
  void revokeAllValidTokensByUser(@Param("user") User user);
  ```
  Then update `src/main/java/com/app/ecommerce/auth/AuthServiceImpl.java` login() method: Replace the loop (lines 84-88) with single call `tokenRepository.revokeAllValidTokensByUser(user);`. Remove the `List<Token> validTokens` variable.

**Checkpoint**: Category deletion no longer cascades to product deletion. Only ADMIN users can create/update/delete products and categories. Token revocation uses a single batch query.

---

### Sub-Phase 11F: HttpStatus Enum Consistency (R-024)

- [x] T120 Update `src/main/java/com/app/ecommerce/shared/models/ErrorResponse.java`: Replace hardcoded status codes and error strings in all factory methods with `HttpStatus` enum values. Change import from `import static org.springframework.http.HttpStatus.BAD_REQUEST;` to `import static org.springframework.http.HttpStatus.*;`. Update `@Schema(example = "NOT_FOUND")` to `@Schema(example = "Not Found")` to match `getReasonPhrase()` output. Methods affected: `notFound()`, `badRequest(String, String, String)`, `conflict()`, `internalError()`, `serviceUnavailable()`, `forbidden()`, `unauthorized()`.

- [x] T121 Update `src/main/java/com/app/ecommerce/shared/models/ApiResponse.java`: Replace hardcoded status codes in factory methods with `HttpStatus` enum values. Update `success(T data, String message)` to use `HttpStatus.OK.value()` instead of `200`. Update `created(T data)` to use `HttpStatus.CREATED.value()` instead of `201`. Update `noContent()` to use `HttpStatus.NO_CONTENT.value()` instead of `204`.

**Checkpoint**: All factory methods use `HttpStatus.XXX.value()` and `HttpStatus.XXX.getReasonPhrase()`. Only `badRequest(String, String)` (line 67) already follows this pattern.

---

## Phase 11 Dependencies

- T107 (cascade fix): No dependencies, **DO FIRST — CRITICAL**
- T090-T094 (dead DTO cleanup): All parallel, no dependencies
- T111-T119 (Dto→Response renames): Depends on T090 (dead DTOs deleted first to avoid renaming dead code). **MUST complete before T095-T099** (ResponseEntity tasks reference renamed classes)
- T095-T099 (ResponseEntity builders): Depends on T111-T119 (renamed classes)
- T100 (SortUtils): Must complete before T101-T103
- T101-T103 (service updates): Parallel, depend on T100
- T104-T105 (validation fixes): Parallel, no dependencies
- T108-T109 (authorization): Parallel, no dependencies
- T110 (token batch): No dependencies
- T120-T121 (HttpStatus consistency): No dependencies, can run anytime
- T106 (verify compile): Depends on all above

```
T107 (CRITICAL) ─────────────────────────────────┐
T090-T094 (dead code) → T111-T119 (renames) ─────┤
                          → T095-T099 (builders) ──┤
T100 → T101-T103 (SortUtils) ────────────────────┤──→ T106
T104-T105 (validation) ──────────────────────────┤
T108-T109 (authorization) ───────────────────────┤
T110 (token batch) ──────────────────────────────┤
T120-T121 (HttpStatus) ──────────────────────────┘
```
