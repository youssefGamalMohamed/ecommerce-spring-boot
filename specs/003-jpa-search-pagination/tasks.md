# Tasks: JPA Search with Pagination, Filtering, and Sorting

**Input**: Design documents from `/specs/003-jpa-search-pagination/`
**Prerequisites**: plan.md ✅ spec.md ✅ research.md ✅ data-model.md ✅ contracts/ ✅ quickstart.md ✅

**Key constraints from spec documents**:
- Endpoint: `GET /products` — REST collection resource, no `/search` suffix (research.md Decision 7)
- No new DTOs — reuse existing `ProductDto`; response is `Page<ProductDto>` in `ApiResponseDto` (spec.md Clarifications, data-model.md)
- `Pageable` argument resolver for `page`, `size`, `sort=field,dir` — default sort `createdAt,desc` (data-model.md, contracts/)
- Replaces existing `GET /products?category=<name>` — `findProductsByCategoryName` / `findAllByCategoryName` are removed (plan.md)
- Sort field whitelist: `name`, `price`, `createdAt` — invalid field → silent fallback to `createdAt,desc` (FR-010)
- `minPrice > maxPrice` → HTTP 400 (FR-012, contracts/)

**Tests**: Not requested in spec — no test tasks generated.

---

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3 — required for user story phases only)

---

## Phase 1: Setup

**Purpose**: Configure Spring `Pageable` defaults so `page=0`, `size=20`, and `max-page-size=100` are enforced at the framework level (FR-002, FR-003, FR-011).

- [ ] T001 Add `spring.data.web.pageable.default-page-size=20` and `spring.data.web.pageable.max-page-size=100` to `src/main/resources/application.properties`

**Checkpoint**: `mvn clean install -DskipTests` passes.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Enable dynamic query composition on `ProductRepository` and create the Criteria API predicate factory. Required by all three user stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T002 [P] Add `JpaSpecificationExecutor<Product>` to the `extends` clause of `ProductRepository`, keeping `JpaRepository<Product, UUID>` unchanged, in `src/main/java/com/app/ecommerce/product/ProductRepository.java`
- [ ] T003 [P] Create class `ProductSpecifications` with four `public static Specification<Product>` factory methods — `nameLike(String name)` (`LOWER(name) LIKE LOWER('%name%')`), `priceGte(Double minPrice)` (`price >= minPrice`), `priceLte(Double maxPrice)` (`price <= maxPrice`), `hasCategory(UUID categoryId)` (INNER JOIN on `categories` by `id`) — in `src/main/java/com/app/ecommerce/product/ProductSpecifications.java`

**Checkpoint**: Project compiles. `ProductRepository.findAll(Specification<Product>, Pageable)` is available. `ProductSpecifications` is ready to use.

---

## Phase 3: User Story 1 — Paginated Product Listing (Priority: P1) 🎯 MVP

**Goal**: `GET /products?page=0&size=10` returns the full product catalog paginated, with Spring `Page` metadata (`totalElements`, `totalPages`, `number`, `size`, `last`). No filters applied yet. Accessible without an auth token. The existing non-paginated `GET /products?category=<name>` method is removed.

**Independent Test**: `curl "http://localhost:8080/products?page=0&size=5"` → HTTP 200, `data.content` is an array, `data.totalElements` is a number, `data.totalPages` >= 1. No `Authorization` header needed.

### Implementation for User Story 1

- [ ] T004 [US1] In `ProductService` interface, **remove** `findAllByCategoryName(String name)` and **add** `Page<ProductDto> findAll(String name, Double minPrice, Double maxPrice, UUID categoryId, Pageable pageable)` in `src/main/java/com/app/ecommerce/product/ProductService.java`
- [ ] T005 [US1] In `ProductServiceImpl`, **remove** the `findAllByCategoryName` implementation and **add** `findAll()`: (a) guard: throw `IllegalArgumentException("minPrice must be less than or equal to maxPrice")` when both price params are non-null and `minPrice > maxPrice`; (b) compose `Specification.where(null)` — filter wiring added in Phase 4; (c) add private `sanitizeSort(Sort sort)` returning `Sort.by(DESC,"createdAt")` when unsorted, otherwise mapping each order — keep if property is in `{"name","price","createdAt"}`, else replace with `Sort.Order.desc("createdAt")`; (d) apply `sanitizeSort` and build `PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort)`; (e) return `productRepository.findAll(spec, safePage).map(productMapper::toDto)` in `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java`
- [ ] T006 [US1] In `ProductController` interface, **remove** `findProductsByCategoryName` and **add** `ResponseEntity<ApiResponseDto<Page<ProductDto>>> findAll(@RequestParam(required=false) String name, @RequestParam(required=false) Double minPrice, @RequestParam(required=false) Double maxPrice, @RequestParam(required=false) UUID categoryId, @ParameterObject Pageable pageable)` with `@Operation(summary="List Products")`, `@ApiResponse(200)`, `@ApiResponse(400)` in `src/main/java/com/app/ecommerce/product/ProductController.java`
- [ ] T007 [US1] In `ProductControllerImpl`, **remove** the `@GetMapping` bare method for `findProductsByCategoryName` and **add** `@GetMapping @Override public ResponseEntity<ApiResponseDto<Page<ProductDto>>> findAll(...)` — annotate `Pageable` with `@ParameterObject @PageableDefault(size=20, sort="createdAt", direction=Sort.Direction.DESC)`; delegate to `productService.findAll(...)`; return `ResponseEntity.ok(ApiResponseDto.success(page))`; add `log.info("findAll(...)")` in `src/main/java/com/app/ecommerce/product/ProductControllerImpl.java`
- [ ] T008 [US1] Locate the `SecurityFilterChain` bean in `src/main/java/com/app/ecommerce/shared/config/` and add `.requestMatchers(HttpMethod.GET, "/products/**").permitAll()` to whitelist all `GET /products` requests — resolves TODO(PRODUCTS_WHITELIST) from the constitution

**Checkpoint**: `GET /products?page=0&size=5` returns paginated products without an auth token. User Story 1 is fully functional.

---

## Phase 4: User Story 2 — Filtered Product Search (Priority: P2)

**Goal**: Optional `name`, `minPrice`, `maxPrice`, `categoryId` narrow the paginated result set using AND logic. `minPrice > maxPrice` returns HTTP 400 (already validated in T005).

**Independent Test**: `curl "http://localhost:8080/products?name=phone&minPrice=50&maxPrice=300"` → only products whose name contains "phone" (case-insensitive) priced between 50 and 300 inclusive. `curl "http://localhost:8080/products?minPrice=300&maxPrice=50"` → HTTP 400.

### Implementation for User Story 2

- [ ] T009 [US2] Update the `Specification.where(null)` composition in `ProductServiceImpl.findAll()` to chain `.and(ProductSpecifications.nameLike(name))` when `name != null`, `.and(ProductSpecifications.priceGte(minPrice))` when `minPrice != null`, `.and(ProductSpecifications.priceLte(maxPrice))` when `maxPrice != null`, `.and(ProductSpecifications.hasCategory(categoryId))` when `categoryId != null`, and pass the resulting spec to `productRepository.findAll(spec, safePage)` in `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java`

**Checkpoint**: All four filter params work individually and in combination. User Story 2 is fully functional.

---

## Phase 5: User Story 3 — Sorted Product Results (Priority: P3)

**Goal**: `sort=field,direction` (e.g., `sort=price,asc`) in the request controls result ordering via Spring's `Pageable` resolver. Unrecognised fields fall back silently to `createdAt,desc` (FR-010). Default when no `sort` param is `createdAt,desc` (FR-009, via `@PageableDefault` in T007 and `sanitizeSort` in T005).

**Independent Test**: `curl "http://localhost:8080/products?sort=price,asc"` → products in ascending price order. `curl "http://localhost:8080/products?sort=unknown,asc"` → HTTP 200 with products sorted by `createdAt` descending (no error).

### Implementation for User Story 3

- [ ] T010 [US3] Verify the `sanitizeSort(Sort sort)` private method in `ProductServiceImpl` (introduced in T005) handles all three cases correctly: (1) unsorted/empty → `Sort.by(DESC, "createdAt")`; (2) recognised property (`name`, `price`, `createdAt`) → keep the order unchanged; (3) unrecognised property → replace that specific order with `Sort.Order.desc("createdAt")`; confirm the helper is called before `PageRequest` construction inside `findAll()` in `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java`

**Checkpoint**: All three user stories are complete. `GET /products` supports any combination of pagination, filters, and sort.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Error handling for validation edge cases and end-to-end verification against the contract.

- [ ] T011 [P] Check `RestExceptionHandler` for a `@ExceptionHandler(IllegalArgumentException.class)` method that returns a 400 response with the exception message; add one returning `ErrorResponseDto` if absent in `src/main/java/com/app/ecommerce/shared/exception/RestExceptionHandler.java`
- [ ] T012 [P] Verify Swagger UI at `http://localhost:8080/swagger-ui.html` shows `GET /products` with `name`, `minPrice`, `maxPrice`, `categoryId`, `page`, `size`, `sort` as separate query parameters (enabled by `@ParameterObject` on `Pageable`)
- [ ] T013 Run all ten verification `curl` commands in the **Verification** section of `specs/003-jpa-search-pagination/quickstart.md` against the running application and confirm each response shape matches `specs/003-jpa-search-pagination/contracts/search-api-contract.md`

---

## Dependencies & Execution Order

### Phase Dependencies

| Phase | Depends on | Notes |
|---|---|---|
| Phase 1 — Setup | — | Start immediately |
| Phase 2 — Foundational | Phase 1 | Blocks all US phases |
| Phase 3 — US1 | Phase 2 | First story; enables US2 and US3 |
| Phase 4 — US2 | Phase 3 complete | Extends same service method |
| Phase 5 — US3 | Phase 3 complete | Verifies sanitizeSort wired in T005 |
| Phase 6 — Polish | All US phases | Final verification |

### Within Each Phase

- **T002 ‖ T003** (Phase 2): different files — run in parallel
- **T004 → T005 → T006 → T007** (Phase 3): sequential — interface before implementation for both service and controller
- **T008** (Phase 3): independent — can run in parallel with T004–T007 (different file)
- **T011 ‖ T012** (Phase 6): independent — can run in parallel

---

## Parallel Execution Examples

### Phase 2

```
Launch together (different files, no shared dependency):
  T002 — ProductRepository.java: add JpaSpecificationExecutor
  T003 — ProductSpecifications.java: create predicate factory
```

### Phase 3 partial

```
T004–T007 are sequential (interface before impl, service before controller)
T008 (SecurityConfiguration) is independent — can proceed in parallel:
  T004 → T005 → T006 → T007  (sequential chain)
  T008                        (parallel with the chain above)
```

---

## Implementation Strategy

### MVP First (User Story 1 — Tasks T001–T008)

1. T001 — Pageable defaults in `application.properties`
2. T002, T003 — foundation in parallel
3. T004 → T005 → T006 → T007 → T008 — replace old category method with full paginated `findAll`
4. **VALIDATE**: `curl "http://localhost:8080/products?page=0&size=5"` returns paginated data without auth token

### Incremental Delivery

| Step | Tasks | Deliverable |
|---|---|---|
| Foundation | T001–T003 | Repository + predicate factory ready |
| MVP | T004–T008 | `GET /products` paginated listing live ✅ |
| Filtering | T009 | Name/price/category filters work ✅ |
| Sort fallback | T010 | Invalid sort fields gracefully handled ✅ |
| Polish | T011–T013 | Error handling verified, quickstart curls pass ✅ |

---

## Format Validation

All 13 tasks follow `- [ ] T### [P?] [Story?] Description with file path`:

| Task | Format | Story Label | File Path |
|---|---|---|---|
| T001 | ✅ | Setup (none) | application.properties |
| T002 | ✅ [P] | Foundational (none) | ProductRepository.java |
| T003 | ✅ [P] | Foundational (none) | ProductSpecifications.java |
| T004 | ✅ | [US1] | ProductService.java |
| T005 | ✅ | [US1] | ProductServiceImpl.java |
| T006 | ✅ | [US1] | ProductController.java |
| T007 | ✅ | [US1] | ProductControllerImpl.java |
| T008 | ✅ | [US1] | SecurityConfiguration (shared/config/) |
| T009 | ✅ | [US2] | ProductServiceImpl.java |
| T010 | ✅ | [US3] | ProductServiceImpl.java |
| T011 | ✅ [P] | Polish (none) | RestExceptionHandler.java |
| T012 | ✅ [P] | Polish (none) | Swagger UI (runtime verification) |
| T013 | ✅ | Polish (none) | quickstart.md + contracts/ |

---

## Notes

- `findProductsByCategoryName` (controller/interface) and `findAllByCategoryName` (service/interface + impl) are **removed** in T004–T007 — not deprecated, deleted
- `sanitizeSort()` is introduced in T005 (Phase 3) and verified in T010 (Phase 5); both touch `ProductServiceImpl.java`
- `@PageableDefault` (T007) sets the default sort when no `sort` query param is supplied; `sanitizeSort()` handles the case where an invalid field is explicitly passed
- T008 (SecurityConfiguration) must not be deferred — it is required for manual US1 testing
- No new DTO or response wrapper class anywhere in this feature
