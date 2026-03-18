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

- [x] T012 [P] [US3] Create `src/main/java/com/app/ecommerce/shared/dto/BaseResponse.java`
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
- [x] T048 [US1] Modify `src/main/java/com/app/ecommerce/shared/dto/ErrorResponseDto.java`

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

### Implementation Status
- All tasks T001-T087 completed
- T088: Run application - requires user to execute
- T089: Verification steps - requires user to execute
