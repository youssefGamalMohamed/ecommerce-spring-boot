# Research: Extend Search and Pagination to Remaining Entities

**Branch**: `004-extend-search-pagination` | **Date**: 2026-03-17

---

## Decision 1: Reuse Feature-003 Pattern Unchanged

**Decision**: Apply the exact same `Specification<T>` + `JpaSpecificationExecutor<T>` + `sanitizeSort()` + `@PageableDefault` pattern from feature 003 to Category and Order. No new libraries or conventions.

**Rationale**: Feature 003 already established and validated this pattern for the Product entity. Reusing it keeps the codebase consistent, minimises the learning curve for maintainers, and avoids introducing conflicting conventions. The spec (SC-006) explicitly requires consistency with feature 003.

**Alternatives considered**:
- QueryDSL — rejected: requires additional code-generation dependency not present in the project.
- Custom JPQL `@Query` with optional parameters — rejected: cannot cleanly handle arbitrary combinations of optional filters; harder to maintain.
- Spring Data `Example`/`ExampleMatcher` — rejected: does not support range queries (`createdAfter`/`createdBefore`) or embedded-field filtering (`deliveryInfo.status`).

---

## Decision 2: Category `findAll()` — Replace List with Page (Breaking Change)

**Decision**: Replace `List<CategoryDto> findAll()` in `CategoryService` with `Page<CategoryDto> findAll(String name, Pageable pageable)`. The existing `GET /categories` controller endpoint is updated to match; its return type changes from `ResponseEntity<ApiResponseDto<?>>` to `ResponseEntity<ApiResponseDto<Page<CategoryDto>>>`.

**Rationale**: Returning an unbounded `List` on a collection endpoint is a performance hazard. The `?` wildcard return type on the current controller interface is already a code smell. The breaking change is intentional and documented in the spec.

**Impact**: Any client currently consuming `GET /categories` and expecting a flat JSON array will receive a `Page` object instead (with `content`, `totalElements`, `totalPages`, etc.). This is a breaking API change.

---

## Decision 3: Order Collection Endpoint — New `GET /orders`

**Decision**: Add `Page<OrderDto> findAll(Status status, PaymentType paymentType, Instant createdAfter, Instant createdBefore, Pageable pageable)` to `OrderService` and a corresponding `GET /orders` to `OrderController`.

**Rationale**: No list endpoint currently exists for orders. The spec requires pagination + filters. Using `Instant` for `createdAfter`/`createdBefore` matches the `BaseEntity.createdAt` field type (already `Instant`) — no type conversion layer needed.

**Date parameter format**: ISO-8601 instant strings (e.g., `2026-01-01T00:00:00Z`) passed as `@RequestParam`. Spring's `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)` handles deserialization.

**Alternatives considered**:
- `LocalDate` with time-zone truncation — rejected: `createdAt` is `Instant`; mapping `LocalDate` to an `Instant` range requires an assumed time zone, introducing ambiguity.
- Separate `/orders/search` endpoint — rejected: same rationale as Decision 7 from feature 003 — REST best practice is query parameters on the collection resource.

---

## Decision 4: Order Filters — Embedded Field Access for `deliveryInfo.status`

**Decision**: In `OrderSpecifications.hasStatus(Status status)`, access the embedded `DeliveryInfo.status` field via `root.get("deliveryInfo").get("status")` (Criteria API path traversal through embeddable).

**Rationale**: `DeliveryInfo` is `@Embeddable` mapped into the `order` table with `@AttributeOverride(name="status", column=@Column(name="delivery_status"))`. Criteria API path traversal through an embeddable produces a direct column predicate — no join required. This is efficient and correct.

---

## Decision 5: Sort Field Whitelists

**Decision**:
- **Category**: Allowed sort fields: `name`, `createdAt`. Default: `name,asc`.
- **Order**: Allowed sort fields: `totalPrice`, `createdAt`. Default: `createdAt,desc`.

Invalid fields fall back to the entity-specific default (same `sanitizeSort()` pattern as feature 003).

**Rationale**: Limiting sort fields to those with an index (or low cardinality) prevents unbounded table scans. `name` on Category is `UNIQUE` (indexed by default). `createdAt` is audited on all entities. `totalPrice` on Order is a natural business sort field.

---

## Decision 6: Authentication on `GET /orders`

**Decision**: `GET /orders` is a protected endpoint requiring a valid JWT token per Constitution Principle III.

**Rationale**: Orders contain financial data (`totalPrice`, `paymentType`) and delivery PII (`address`). This cannot be public.

**Note on current state**: The Spring Security dependency (`spring-boot-starter-security`) is **not present** in `pom.xml` as of this branch. The JWT filter and `SecurityConfiguration` referenced in the constitution exist in the codebase only as partial stubs. Until security is fully activated, the order endpoint will be accessible without auth in practice. The spec (FR-013) states the requirement; implementation must enforce it when security is enabled.

---

## Decision 7: No Cache Changes

**Decision**: No `@Cacheable` annotations on any of the new paginated `findAll()` methods.

**Rationale**: Paginated + filtered queries are dynamic — the cache key would need to encode the full filter+page combination, which is ineffective and memory-wasteful. Feature 003 made the same decision for Product search. Redis caching remains on `findById()` only.

---

### Post-Implementation Note (2026-03-17)

Despite this decision, `@Cacheable(value = CacheConstants.CATEGORIES, key = "'all'")` was inadvertently added to `CategoryServiceImpl.findAll()` during T007. The hardcoded `'all'` key caused all paginated+filtered category queries to return the same cached result (the first request wins). The annotation was removed in a follow-up fix. `OrderServiceImpl.findAll()` and `ProductServiceImpl.findAll()` were not affected.

---

## Decision 8: Enum Validation on Order Filters

**Decision**: Reject unknown `status` and `paymentType` string values with HTTP 400.

**Rationale**: Spring's `@RequestParam` with an enum type automatically throws a `MethodArgumentTypeMismatchException` when the value does not match any enum constant. The existing `RestExceptionHandler` maps this to HTTP 400. No additional validation code is needed.

**Known enum values**:
- `PaymentType`: `CASH`, `VISA`
- `Status` (delivery): `DELIVERED`, `ON_THE_WAY_TO_CUSTOMER`, `NOT_MOVED_OUT_FROM_WAREHOUSE`, `CANCELED`
