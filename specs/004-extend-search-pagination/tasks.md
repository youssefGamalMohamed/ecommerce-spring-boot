# Tasks: Extend Search and Pagination to Remaining Entities

**Input**: Design documents from `/specs/004-extend-search-pagination/`
**Prerequisites**: plan.md ✅ spec.md ✅ research.md ✅ data-model.md ✅ contracts/ ✅ quickstart.md ✅

**Key constraints from spec documents**:
- Pattern: Reuse `Specification<T>` + `JpaSpecificationExecutor<T>` + `sanitizeSort()` + `@PageableDefault` from feature 003 exactly (SC-006)
- No new DTOs — reuse existing `CategoryDto` and `OrderDto`; responses are `Page<T>` in `ApiResponseDto` (FR-016)
- Category default sort: `name,asc` — Order default sort: `createdAt,desc` (data-model.md)
- `createdAfter` > `createdBefore` → HTTP 400 via `IllegalArgumentException` (FR-010)
- Enum validation (`Status`, `PaymentType`) is handled automatically by Spring's `@RequestParam` type binding → HTTP 400 (research.md Decision 8)
- No `@Cacheable` on any new `findAll()` methods (research.md Decision 7)
- Breaking change: `GET /categories` response changes from `List` to `Page` — intentional (spec Clarifications)

**Tests**: Not requested in spec — no test tasks generated.

---

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2 — required for user story phases only)

---

## Phase 1: Setup

**Purpose**: Verify shared pageable configuration is in place. No new configuration is needed; feature 003 already added the required `application.yml` entries.

- [x] T001 Verify `src/main/resources/application.yml` already contains `spring.data.web.pageable.default-page-size: 20` and `spring.data.web.pageable.max-page-size: 100` (added in feature 003). If either entry is missing, add it now.

**Checkpoint**: `mvn clean install -DskipTests` passes.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add `JpaSpecificationExecutor` to both repositories and create both `Specification` factory classes. All four tasks operate on different files and can run in parallel. No user story work can begin until this phase is complete.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T002 [P] Add `JpaSpecificationExecutor<Category>` to the `extends` clause of `CategoryRepository` (keep existing `JpaRepository<Category, UUID>` unchanged) in `src/main/java/com/app/ecommerce/category/CategoryRepository.java`

- [x] T003 [P] Add `JpaSpecificationExecutor<Order>` to the `extends` clause of `OrderRepository` (keep existing `JpaRepository<Order, UUID>` unchanged) in `src/main/java/com/app/ecommerce/order/OrderRepository.java`

- [x] T004 [P] Create `CategorySpecifications` class with one static factory method: `nameLike(String name)` — returns `LOWER(name) LIKE LOWER('%name%')` predicate; returns `null` when `name` is `null` or blank — in `src/main/java/com/app/ecommerce/category/CategorySpecifications.java`

- [x] T005 [P] Create `OrderSpecifications` class with four static factory methods: `hasStatus(Status status)` (accesses embedded field via `root.get("deliveryInfo").get("status")`), `hasPaymentType(PaymentType paymentType)`, `createdAfter(Instant after)` (`createdAt >= after`), `createdBefore(Instant before)` (`createdAt <= before`) — each returns `null` when its argument is `null` — in `src/main/java/com/app/ecommerce/order/OrderSpecifications.java`

**Checkpoint**: Project compiles. `categoryRepository.findAll(Specification<Category>, Pageable)` and `orderRepository.findAll(Specification<Order>, Pageable)` are available.

---

## Phase 3: User Story 1 — Paginated Category Listing (Priority: P1) 🎯 MVP

**Goal**: `GET /categories?page=0&size=10` returns a paginated `Page<CategoryDto>` with metadata. Optional `name` filter works. Old unbounded `List<CategoryDto>` response is gone. Accessible without auth.

**Independent Test**: `curl "http://localhost:8080/categories?page=0&size=5"` → HTTP 200, `data.content` is an array, `data.totalElements` >= 0, `data.totalPages` >= 1, `data.size` = 5.

### Implementation for User Story 1

- [x] T006 [US1] In `CategoryService` interface, **replace** `List<CategoryDto> findAll()` with `Page<CategoryDto> findAll(String name, Pageable pageable)` — add imports `org.springframework.data.domain.Page` and `org.springframework.data.domain.Pageable` — in `src/main/java/com/app/ecommerce/category/CategoryService.java`

- [x] T007 [US1] In `CategoryServiceImpl`, **replace** the existing `findAll()` implementation with: (a) compose `Specification.where(CategorySpecifications.nameLike(name))`; (b) implement private `sanitizeSort(Sort sort)` — allowed fields: `{"name", "createdAt"}`, default when unsorted or invalid: `Sort.by(Sort.Direction.ASC, "name")`; (c) build `PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort)`; (d) return `categoryRepository.findAll(spec, safePage).map(categoryMapper::toDto)` — in `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java`

- [x] T008 [US1] In `CategoryController` interface, **replace** `ResponseEntity<ApiResponseDto<?>> findAll()` with `ResponseEntity<ApiResponseDto<Page<CategoryDto>>> findAll(@RequestParam(required=false) String name, @ParameterObject Pageable pageable)` — update `@Operation(summary="List Categories")`, add `@ApiResponse(200)` and `@ApiResponse(400)` — in `src/main/java/com/app/ecommerce/category/CategoryController.java`

- [x] T009 [US1] In `CategoryControllerImpl`, **replace** the existing `@GetMapping` `findAll()` method with: `@GetMapping @Override public ResponseEntity<ApiResponseDto<Page<CategoryDto>>> findAll(@RequestParam(required=false) String name, @ParameterObject @PageableDefault(size=20, sort="name", direction=Sort.Direction.ASC) Pageable pageable)` — delegate to `categoryService.findAll(name, pageable)` — return `ResponseEntity.ok(ApiResponseDto.success(page))` — in `src/main/java/com/app/ecommerce/category/CategoryControllerImpl.java`

**Checkpoint**: `GET /categories?page=0&size=5` returns paginated `Page<CategoryDto>` with correct metadata. `GET /categories?name=elec` returns only categories whose name contains "elec" (case-insensitive). User Story 1 fully functional.

---

## Phase 4: User Story 2 — Paginated Order Listing (Priority: P2)

**Goal**: `GET /orders?page=0&size=10` returns a paginated `Page<OrderDto>` with metadata. Optional filters `status`, `paymentType`, `createdAfter`, `createdBefore` work individually and in AND combination. Invalid date range (`createdAfter > createdBefore`) returns HTTP 400. Invalid enum value returns HTTP 400.

**Independent Test**: `curl "http://localhost:8080/orders?page=0&size=5"` → HTTP 200, `data.content` is an array, `data.totalElements` >= 0, `data.totalPages` >= 1.

### Implementation for User Story 2

- [x] T010 [US2] In `OrderService` interface, **add** (do not remove existing methods): `Page<OrderDto> findAll(Status status, PaymentType paymentType, Instant createdAfter, Instant createdBefore, Pageable pageable)` — add imports `org.springframework.data.domain.Page`, `org.springframework.data.domain.Pageable`, `java.time.Instant`, `com.app.ecommerce.shared.enums.Status`, `com.app.ecommerce.shared.enums.PaymentType` — in `src/main/java/com/app/ecommerce/order/OrderService.java`

- [x] T011 [US2] In `OrderServiceImpl`, **add** the `findAll()` implementation: (a) guard: throw `IllegalArgumentException("createdAfter must be less than or equal to createdBefore")` when both are non-null and `createdAfter.isAfter(createdBefore)`; (b) compose `Specification.where(OrderSpecifications.hasStatus(status)).and(OrderSpecifications.hasPaymentType(paymentType)).and(OrderSpecifications.createdAfter(createdAfter)).and(OrderSpecifications.createdBefore(createdBefore))`; (c) implement private `sanitizeSort(Sort sort)` — allowed fields: `{"totalPrice", "createdAt"}`, default when unsorted or invalid: `Sort.by(Sort.Direction.DESC, "createdAt")`; (d) return `orderRepository.findAll(spec, safePage).map(orderMapper::toDto)` — in `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java`

- [x] T012 [US2] In `OrderController` interface, **add** a new endpoint declaration: `ResponseEntity<ApiResponseDto<Page<OrderDto>>> findAll(@RequestParam(required=false) Status status, @RequestParam(required=false) PaymentType paymentType, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant createdAfter, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant createdBefore, @ParameterObject Pageable pageable)` — annotate with `@Operation(summary="List Orders")`, `@ApiResponse(200)`, `@ApiResponse(400)`, `@ApiResponse(401)` — in `src/main/java/com/app/ecommerce/order/OrderController.java`

- [x] T013 [US2] In `OrderControllerImpl`, **add** the `@GetMapping` implementation: `@GetMapping @Override public ResponseEntity<ApiResponseDto<Page<OrderDto>>> findAll(Status status, PaymentType paymentType, @DateTimeFormat(...) Instant createdAfter, @DateTimeFormat(...) Instant createdBefore, @ParameterObject @PageableDefault(size=20, sort="createdAt", direction=Sort.Direction.DESC) Pageable pageable)` — delegate to `orderService.findAll(status, paymentType, createdAfter, createdBefore, pageable)` — return `ResponseEntity.ok(ApiResponseDto.success(page))` — in `src/main/java/com/app/ecommerce/order/OrderControllerImpl.java`

**Checkpoint**: `GET /orders?page=0&size=5` returns paginated `Page<OrderDto>`. `GET /orders?status=DELIVERED` returns only delivered orders. `GET /orders?createdAfter=2026-12-01T00:00:00Z&createdBefore=2026-01-01T00:00:00Z` → HTTP 400. `GET /orders?status=INVALID` → HTTP 400. User Story 2 fully functional.

---

## Phase 5: Polish & Verification

**Purpose**: End-to-end verification against the contract and Swagger UI check.

- [x] T014 [P] Verify Swagger UI at `http://localhost:8080/swagger-ui.html` shows `GET /categories` with `name`, `page`, `size`, `sort` as separate query parameters (enabled by `@ParameterObject` on `Pageable`)

- [x] T015 [P] Verify Swagger UI at `http://localhost:8080/swagger-ui.html` shows `GET /orders` with `status`, `paymentType`, `createdAfter`, `createdBefore`, `page`, `size`, `sort` as separate query parameters

- [x] T016 Run all 15 verification `curl` commands in `specs/004-extend-search-pagination/quickstart.md` against the running application and confirm each response shape matches `specs/004-extend-search-pagination/contracts/search-api-contract.md`

---

## Dependencies & Execution Order

### Phase Dependencies

| Phase | Depends on | Notes |
|---|---|---|
| Phase 1 — Setup | — | Start immediately |
| Phase 2 — Foundational | Phase 1 | Blocks all US phases |
| Phase 3 — US1 (Category) | Phase 2 | First story; independent of US2 |
| Phase 4 — US2 (Order) | Phase 2 | Second story; independent of US1 |
| Phase 5 — Polish | Phase 3 + Phase 4 | Final verification |

### Within Each Phase

- **T002 ‖ T003 ‖ T004 ‖ T005** (Phase 2): all different files — run all four in parallel
- **T006 → T007 → T008 → T009** (Phase 3): sequential — interface before implementation, service before controller
- **T010 → T011 → T012 → T013** (Phase 4): sequential — interface before implementation, service before controller
- **T014 ‖ T015** (Phase 5): independent — run in parallel; T016 requires running app

### User Story Independence

- **US1 (Category)** has no dependency on US2. Once Phase 2 is complete, US1 can be built and validated in isolation.
- **US2 (Order)** has no dependency on US1. Once Phase 2 is complete, US2 can be built and validated in isolation.
- US1 and US2 can be implemented in parallel by different developers after Phase 2.

---

## Parallel Execution Examples

### Phase 2 (all four can start at once)

```
Launch together (different files, no shared dependency):
  T002 — CategoryRepository.java: add JpaSpecificationExecutor
  T003 — OrderRepository.java: add JpaSpecificationExecutor
  T004 — CategorySpecifications.java: create nameLike() factory
  T005 — OrderSpecifications.java: create 4 predicate factories
```

### Phase 3 + Phase 4 (after Phase 2 completes)

```
Two parallel chains (different entity packages):
  Chain A (US1): T006 → T007 → T008 → T009  (all in category/)
  Chain B (US2): T010 → T011 → T012 → T013  (all in order/)
```

---

## Implementation Strategy

### MVP First (User Story 1 — Tasks T001–T009)

1. T001 — Verify application.yml defaults
2. T002, T003, T004, T005 — Foundational in parallel
3. T006 → T007 → T008 → T009 — Category search/pagination
4. **VALIDATE**: `curl "http://localhost:8080/categories?page=0&size=5"` returns `Page<CategoryDto>` with metadata

### Incremental Delivery

| Step | Tasks | Deliverable |
|---|---|---|
| Foundation | T001–T005 | Both repositories + spec factories ready |
| MVP | T006–T009 | `GET /categories` paginated listing live ✅ |
| Order search | T010–T013 | `GET /orders` paginated listing with filters live ✅ |
| Polish | T014–T016 | Swagger UI verified, all quickstart curls pass ✅ |

---

## Format Validation

All 16 tasks follow `- [ ] T### [P?] [Story?] Description with file path`:

| Task | Format | Story Label | File Path |
|---|---|---|---|
| T001 | ✅ | Setup (none) | application.yml |
| T002 | ✅ [P] | Foundational (none) | CategoryRepository.java |
| T003 | ✅ [P] | Foundational (none) | OrderRepository.java |
| T004 | ✅ [P] | Foundational (none) | CategorySpecifications.java |
| T005 | ✅ [P] | Foundational (none) | OrderSpecifications.java |
| T006 | ✅ | [US1] | CategoryService.java |
| T007 | ✅ | [US1] | CategoryServiceImpl.java |
| T008 | ✅ | [US1] | CategoryController.java |
| T009 | ✅ | [US1] | CategoryControllerImpl.java |
| T010 | ✅ | [US2] | OrderService.java |
| T011 | ✅ | [US2] | OrderServiceImpl.java |
| T012 | ✅ | [US2] | OrderController.java |
| T013 | ✅ | [US2] | OrderControllerImpl.java |
| T014 | ✅ [P] | Polish (none) | Swagger UI (runtime) |
| T015 | ✅ [P] | Polish (none) | Swagger UI (runtime) |
| T016 | ✅ | Polish (none) | quickstart.md + contracts/ |

---

## Notes

- T004 and T005 are new files — `CategorySpecifications.java` and `OrderSpecifications.java` in their respective feature packages
- `sanitizeSort()` is a private method on each `ServiceImpl`; it is not shared between Category and Order (different allowed fields and defaults)
- No `@Cacheable` on any new `findAll()` method — paginated+filtered queries are not cacheable effectively (research.md Decision 7)
- The `Order` table name is backtick-quoted (SQL reserved word: `` `order` ``) — no change needed, the entity already handles this
- `DeliveryInfo.status` is accessed via Criteria API path `root.get("deliveryInfo").get("status")` — no join required (embedded field, same table)
- Breaking change in T006–T009: the `GET /categories` response shape changes; existing clients consuming a flat array will need to update to read `data.content`

---

## Post-Implementation Fixes

- [x] **BUGFIX**: Removed `@Cacheable(value = CacheConstants.CATEGORIES, key = "'all'")` from `CategoryServiceImpl.findAll(String name, Pageable pageable)` — introduced during T007 implementation, contradicted Decision 7, caused all paginated category queries to return identical stale data. Fixed 2026-03-17.
