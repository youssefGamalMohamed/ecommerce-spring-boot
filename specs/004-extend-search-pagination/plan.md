# Implementation Plan: Extend Search and Pagination to Remaining Entities

**Branch**: `004-extend-search-pagination` | **Date**: 2026-03-17 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/004-extend-search-pagination/spec.md`

## Summary

Extend the `Specification<T>` + `JpaSpecificationExecutor<T>` + paginated `findAll()` pattern (established in feature 003 for Product) to two remaining entities:

1. **Category** — replace the existing unbounded `List<CategoryDto> findAll()` with `Page<CategoryDto> findAll(String name, Pageable pageable)`. Filters: `name` (case-insensitive partial match). Sort: `name` or `createdAt`, default `name,asc`.
2. **Order** — add a new `GET /orders` paginated collection endpoint that does not currently exist. Filters: `status`, `paymentType`, `createdAfter`, `createdBefore` (all optional, AND logic). Sort: `totalPrice` or `createdAt`, default `createdAt,desc`.

No new DTOs, no new libraries, no new conventions. Every change mirrors feature 003 exactly.

---

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3.0.0, Spring Data JPA (Hibernate), MapStruct 1.6.0, Lombok, SpringDoc OpenAPI 2.0.2
**Storage**: MySQL (primary), Redis (cache layer — existing; no cache changes for search endpoints)
**Testing**: Maven (`mvn test`), JUnit 5 (Spring Boot managed)
**Target Platform**: Linux/Windows server (Spring Boot embedded Tomcat)
**Project Type**: REST web service (single-module Maven mono-repo)
**Performance Goals**: Paginated queries < 500ms for datasets up to 10,000 rows (SC-005)
**Constraints**: Page size capped at 100 via `spring.data.web.pageable.max-page-size`; sort fields whitelisted in `sanitizeSort()`
**Scale/Scope**: Two entities (Category, Order); same pattern reusable for future entities

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Layered Architecture | PASS | Controllers handle HTTP only; services own all business logic; repositories are the sole DB access point. `CategorySpecifications` and `OrderSpecifications` are static helper classes in their respective feature packages. No HTTP types in service layer. |
| II. DTO-First Communication | PASS | Existing `CategoryDto` and `OrderDto` reused as page item types. `Page<CategoryDto>` and `Page<OrderDto>` are the response data. No entity is serialized directly to HTTP. MapStruct mappers handle entity→DTO conversion. |
| III. JWT Stateless Auth | PASS (with note) | `GET /categories` is public (FR-005). `GET /orders` requires auth (FR-013). **Note**: Spring Security dependency (`spring-boot-starter-security`) is not present in `pom.xml` as of this branch — the auth requirement for orders is a spec-level constraint that will be enforced when security is enabled. No whitelist changes needed for categories (no security active). |
| IV. Interface-Driven Design | PASS | `CategoryService` interface is updated (replace `List<CategoryDto> findAll()` with `Page<CategoryDto> findAll(String, Pageable)`). `OrderService` interface gets the new `findAll()` method. Both controller interfaces are updated. Implementations follow. |
| V. Async Messaging | N/A | No side effects triggered by read-only search endpoints. |
| VI. Observability | PASS | `@Slf4j` on all `ServiceImpl` and `ControllerImpl` classes. No `System.out.println`. Inline logging in existing methods is pre-existing (not introduced here). |

**Constitution Check Result**: All gates pass. The Category `findAll()` signature change is a deliberate breaking change documented in the spec. No new packages introduced.

---

## Project Structure

### Documentation (this feature)

```text
specs/004-extend-search-pagination/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   └── search-api-contract.md   ← Phase 1 output
├── checklists/
│   └── requirements.md ← spec quality checklist
└── tasks.md             ← Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
src/main/java/com/app/ecommerce/
├── category/
│   ├── Category.java                       — existing entity (unchanged)
│   ├── CategoryController.java             — MODIFIED: replace findAll() signature;
│   │                                         return type → ResponseEntity<ApiResponseDto<Page<CategoryDto>>>
│   ├── CategoryControllerImpl.java         — MODIFIED: replace @GetMapping findAll() impl;
│   │                                         add @ParameterObject @PageableDefault
│   ├── CategoryDto.java                    — existing (reused as Page item type — unchanged)
│   ├── CategoryMapper.java                 — existing (unchanged)
│   ├── CategoryRepository.java             — MODIFIED: add JpaSpecificationExecutor<Category>
│   ├── CategoryService.java                — MODIFIED: replace List<CategoryDto> findAll()
│   │                                         with Page<CategoryDto> findAll(String, Pageable)
│   ├── CategoryServiceImpl.java            — MODIFIED: implement new findAll() + sanitizeSort()
│   └── CategorySpecifications.java         — NEW: nameLike(String) predicate factory
│
└── order/
    ├── Order.java                          — existing entity (unchanged)
    ├── OrderController.java                — MODIFIED: add findAll() endpoint declaration
    ├── OrderControllerImpl.java            — MODIFIED: implement GET /orders with filters + Pageable
    ├── OrderDto.java                       — existing (reused as Page item type — unchanged)
    ├── OrderMapper.java                    — existing (unchanged)
    ├── OrderRepository.java                — MODIFIED: add JpaSpecificationExecutor<Order>
    ├── OrderService.java                   — MODIFIED: add findAll(Status, PaymentType,
    │                                         Instant, Instant, Pageable)
    ├── OrderServiceImpl.java               — MODIFIED: implement findAll() + sanitizeSort()
    └── OrderSpecifications.java            — NEW: hasStatus, hasPaymentType,
                                              createdAfter, createdBefore predicate factory
```

**Structure Decision**: Feature-based package layout matching existing codebase conventions. Two new files (`CategorySpecifications.java`, `OrderSpecifications.java`) in their respective feature packages. All other changes modify existing files in-place.

**Breaking change note**: `GET /categories` response shape changes from a flat JSON array to a `Page<CategoryDto>` object. Any existing client consuming the raw array will break. This is intentional and documented in the spec (Clarifications, 2026-03-17).

---

## Detailed Implementation Steps

### Phase 1: Category — Foundation (Blocking for Category US1)

#### Step 1.1 — Add `JpaSpecificationExecutor<Category>` to `CategoryRepository`

**File**: `src/main/java/com/app/ecommerce/category/CategoryRepository.java`

Add `JpaSpecificationExecutor<Category>` to the `extends` clause alongside the existing `JpaRepository<Category, UUID>`. No other changes.

**Result**: `categoryRepository.findAll(Specification<Category>, Pageable)` becomes available.

---

#### Step 1.2 — Create `CategorySpecifications.java`

**File**: `src/main/java/com/app/ecommerce/category/CategorySpecifications.java` *(new file)*

Create a `public class CategorySpecifications` with one static factory method:

```java
public static Specification<Category> nameLike(String name) {
    return (root, query, cb) -> {
        if (name == null || name.isBlank()) {
            return null;
        }
        return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    };
}
```

No `query.distinct(true)` needed — `Category` has no collection join in these specs.

---

#### Step 1.3 — Update `CategoryService` interface

**File**: `src/main/java/com/app/ecommerce/category/CategoryService.java`

Replace:
```java
List<CategoryDto> findAll();
```

With:
```java
Page<CategoryDto> findAll(String name, Pageable pageable);
```

Add necessary imports: `org.springframework.data.domain.Page`, `org.springframework.data.domain.Pageable`.

---

#### Step 1.4 — Update `CategoryServiceImpl`

**File**: `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java`

Replace the existing `findAll()` implementation with:

```java
@Override
public Page<CategoryDto> findAll(String name, Pageable pageable) {
    log.info("findAll categories with name={}", name);
    Specification<Category> spec = Specification.where(CategorySpecifications.nameLike(name));
    Sort safeSort = sanitizeSort(pageable.getSort());
    Pageable safePage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    return categoryRepository.findAll(spec, safePage).map(categoryMapper::toDto);
}

private Sort sanitizeSort(Sort sort) {
    Set<String> allowed = Set.of("name", "createdAt");
    if (sort == null || sort.isUnsorted()) {
        return Sort.by(Sort.Direction.ASC, "name");
    }
    List<Sort.Order> orders = sort.stream()
        .map(o -> allowed.contains(o.getProperty())
            ? o
            : Sort.Order.asc("name"))
        .toList();
    return Sort.by(orders);
}
```

Add imports: `Set`, `List`, `Sort`, `PageRequest`, `Specification`, `CategorySpecifications`.

---

#### Step 1.5 — Update `CategoryController` interface

**File**: `src/main/java/com/app/ecommerce/category/CategoryController.java`

Replace the existing `findAll()` declaration:

```java
// REMOVE:
ResponseEntity<ApiResponseDto<?>> findAll();

// ADD:
@Operation(summary = "List Categories", description = "Retrieve paginated categories with optional name filter")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
        content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
})
ResponseEntity<ApiResponseDto<Page<CategoryDto>>> findAll(
    @RequestParam(required = false) String name,
    @ParameterObject Pageable pageable
);
```

Add imports: `Page`, `Pageable`, `ParameterObject`.

---

#### Step 1.6 — Update `CategoryControllerImpl`

**File**: `src/main/java/com/app/ecommerce/category/CategoryControllerImpl.java`

Replace the existing `@GetMapping` `findAll()` implementation:

```java
@GetMapping
@Override
public ResponseEntity<ApiResponseDto<Page<CategoryDto>>> findAll(
    @RequestParam(required = false) String name,
    @ParameterObject @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
) {
    log.info("findAll categories - name={}, page={}, size={}", name, pageable.getPageNumber(), pageable.getPageSize());
    Page<CategoryDto> page = categoryService.findAll(name, pageable);
    return ResponseEntity.ok(ApiResponseDto.success(page));
}
```

Add imports: `Page`, `Pageable`, `PageableDefault`, `ParameterObject`, `Sort`.

**Checkpoint**: `GET /categories?page=0&size=5` returns paginated `Page<CategoryDto>` with metadata. `GET /categories?name=elec` returns only matching categories.

---

### Phase 2: Order — Foundation (Blocking for Order US2)

#### Step 2.1 — Add `JpaSpecificationExecutor<Order>` to `OrderRepository`

**File**: `src/main/java/com/app/ecommerce/order/OrderRepository.java`

Add `JpaSpecificationExecutor<Order>` to the `extends` clause alongside `JpaRepository<Order, UUID>`. No other changes.

---

#### Step 2.2 — Create `OrderSpecifications.java`

**File**: `src/main/java/com/app/ecommerce/order/OrderSpecifications.java` *(new file)*

```java
public class OrderSpecifications {

    public static Specification<Order> hasStatus(Status status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("deliveryInfo").get("status"), status);
        };
    }

    public static Specification<Order> hasPaymentType(PaymentType paymentType) {
        return (root, query, cb) -> {
            if (paymentType == null) return null;
            return cb.equal(root.get("paymentType"), paymentType);
        };
    }

    public static Specification<Order> createdAfter(Instant after) {
        return (root, query, cb) -> {
            if (after == null) return null;
            return cb.greaterThanOrEqualTo(root.get("createdAt"), after);
        };
    }

    public static Specification<Order> createdBefore(Instant before) {
        return (root, query, cb) -> {
            if (before == null) return null;
            return cb.lessThanOrEqualTo(root.get("createdAt"), before);
        };
    }
}
```

Imports: `Status` (`com.app.ecommerce.shared.enums`), `PaymentType` (`com.app.ecommerce.shared.enums`), `Instant`, `Specification`, `Order`.

---

#### Step 2.3 — Update `OrderService` interface

**File**: `src/main/java/com/app/ecommerce/order/OrderService.java`

Add (do not remove existing methods):

```java
Page<OrderDto> findAll(Status status,
                       PaymentType paymentType,
                       Instant createdAfter,
                       Instant createdBefore,
                       Pageable pageable);
```

Imports: `Page`, `Pageable`, `Status`, `PaymentType`, `Instant`.

---

#### Step 2.4 — Update `OrderServiceImpl`

**File**: `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java`

Add the implementation:

```java
@Override
public Page<OrderDto> findAll(Status status,
                              PaymentType paymentType,
                              Instant createdAfter,
                              Instant createdBefore,
                              Pageable pageable) {
    log.info("findAll orders - status={}, paymentType={}, createdAfter={}, createdBefore={}",
        status, paymentType, createdAfter, createdBefore);
    if (createdAfter != null && createdBefore != null && createdAfter.isAfter(createdBefore)) {
        throw new IllegalArgumentException("createdAfter must be less than or equal to createdBefore");
    }
    Specification<Order> spec = Specification
        .where(OrderSpecifications.hasStatus(status))
        .and(OrderSpecifications.hasPaymentType(paymentType))
        .and(OrderSpecifications.createdAfter(createdAfter))
        .and(OrderSpecifications.createdBefore(createdBefore));
    Sort safeSort = sanitizeSort(pageable.getSort());
    Pageable safePage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    return orderRepository.findAll(spec, safePage).map(orderMapper::toDto);
}

private Sort sanitizeSort(Sort sort) {
    Set<String> allowed = Set.of("totalPrice", "createdAt");
    if (sort == null || sort.isUnsorted()) {
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
    List<Sort.Order> orders = sort.stream()
        .map(o -> allowed.contains(o.getProperty())
            ? o
            : Sort.Order.desc("createdAt"))
        .toList();
    return Sort.by(orders);
}
```

---

#### Step 2.5 — Update `OrderController` interface

**File**: `src/main/java/com/app/ecommerce/order/OrderController.java`

Add the new endpoint declaration:

```java
@Operation(summary = "List Orders", description = "Retrieve paginated orders with optional filters")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
        content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
    @ApiResponse(responseCode = "400", description = "Invalid filter or pagination parameters",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
})
ResponseEntity<ApiResponseDto<Page<OrderDto>>> findAll(
    @RequestParam(required = false) Status status,
    @RequestParam(required = false) PaymentType paymentType,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
    @ParameterObject Pageable pageable
);
```

---

#### Step 2.6 — Update `OrderControllerImpl`

**File**: `src/main/java/com/app/ecommerce/order/OrderControllerImpl.java`

Add the `@GetMapping` implementation:

```java
@GetMapping
@Override
public ResponseEntity<ApiResponseDto<Page<OrderDto>>> findAll(
    @RequestParam(required = false) Status status,
    @RequestParam(required = false) PaymentType paymentType,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
    @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
) {
    log.info("findAll orders - status={}, paymentType={}, createdAfter={}, createdBefore={}, page={}, size={}",
        status, paymentType, createdAfter, createdBefore, pageable.getPageNumber(), pageable.getPageSize());
    Page<OrderDto> page = orderService.findAll(status, paymentType, createdAfter, createdBefore, pageable);
    return ResponseEntity.ok(ApiResponseDto.success(page));
}
```

**Checkpoint**: `GET /orders?page=0&size=5` returns paginated `Page<OrderDto>` with metadata. `GET /orders?status=DELIVERED` returns only delivered orders. `GET /orders?createdAfter=2026-01-01T00:00:00Z&createdBefore=2026-01-01T00:00:00Z` with inverted range → HTTP 400.

---

### Phase 3: Polish & Verification

#### Step 3.1 — Verify `application.yml` pageable defaults still apply

**File**: `src/main/resources/application.yml`

Confirm these entries are present (added in feature 003):

```yaml
spring:
  data:
    web:
      pageable:
        default-page-size: 20
        max-page-size: 100
```

No changes needed if already present.

#### Step 3.2 — Verify Swagger UI

Navigate to `http://localhost:8080/swagger-ui.html` and confirm:
- `GET /categories` lists `name`, `page`, `size`, `sort` as separate query params.
- `GET /orders` lists `status`, `paymentType`, `createdAfter`, `createdBefore`, `page`, `size`, `sort`.

#### Step 3.3 — Run quickstart verification

Execute all 15 `curl` commands in `specs/004-extend-search-pagination/quickstart.md` and confirm each response matches the contract in `specs/004-extend-search-pagination/contracts/search-api-contract.md`.

---

## Complexity Tracking

> No constitution violations requiring justification.

---

## Dependencies & Execution Order

| Step | Depends on | Can parallelize |
|------|-----------|-----------------|
| 1.1 — CategoryRepository | — | Yes, with 2.1 |
| 1.2 — CategorySpecifications | — | Yes, with 2.1, 2.2 |
| 1.3 — CategoryService interface | 1.1, 1.2 | No |
| 1.4 — CategoryServiceImpl | 1.3 | No |
| 1.5 — CategoryController interface | 1.3 | No |
| 1.6 — CategoryControllerImpl | 1.4, 1.5 | No |
| 2.1 — OrderRepository | — | Yes, with 1.1 |
| 2.2 — OrderSpecifications | — | Yes, with 1.1, 1.2 |
| 2.3 — OrderService interface | 2.1, 2.2 | No |
| 2.4 — OrderServiceImpl | 2.3 | No |
| 2.5 — OrderController interface | 2.3 | No |
| 2.6 — OrderControllerImpl | 2.4, 2.5 | No |
| 3.x — Polish | Phase 1 + 2 complete | — |

**Parallel launch**: Steps 1.1, 1.2, 2.1, 2.2 can all run in parallel (different files, no shared dependencies).
**Sequential chains**: 1.3 → 1.4 → 1.5 → 1.6 and 2.3 → 2.4 → 2.5 → 2.6.

---

## Resolve TODO(PRODUCTS_WHITELIST)

The constitution (Principle III) carries a `TODO(PRODUCTS_WHITELIST)` note — a pending decision on whether `GET /products/**` should be public. Feature 003 intended to resolve this but Spring Security is not currently active in `pom.xml`. This feature does not add or activate Spring Security. The TODO remains open and should be addressed in a dedicated security-activation feature.
