# Feature Specification: Extend Search and Pagination to Remaining Entities

**Feature Branch**: `004-extend-search-pagination`
**Created**: 2026-03-17
**Status**: Implemented
**Input**: User description: "for the spec @specs/003-jpa-search-pagination\ can you update it to allow to create search functionality for the remaining functionality for category, orders...etc as an example if i want to return the categories it's not make any sense to return all of the data in db and it should be paginated, so check for all of endpoints that contains search functionality and add what is required to add"

## Context

Feature 003 introduced paginated, filtered, and sorted search for the **Product** catalog. The same pattern must now be applied to every other entity that exposes a collection endpoint. Currently:

- **Category** — `GET /categories` returns the entire category table as an unbounded list with no filtering, sorting, or pagination.
- **Order** — no collection endpoint exists at all; clients can only look up a single order by ID.

Cart and CartItem have no list endpoints and are scoped to a single user session — they are out of scope for this feature.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Paginated Category Listing (Priority: P1)

An admin or client browses the category catalog and receives a paginated list of categories. The client may filter by name and control page size and sort order; the system returns only that page along with metadata.

**Why this priority**: Categories are already exposed via `GET /categories`, which today returns every row unbounded. Fixing this is the highest-value change because it prevents a full-table scan on every request and directly mirrors the pattern already live for products.

**Independent Test**: `GET /categories?page=0&size=10` returns at most 10 categories plus pagination metadata (`totalElements`, `totalPages`, `currentPage`, `pageSize`). No auth header needed.

**Acceptance Scenarios**:

1. **Given** categories exist, **When** `GET /categories?page=0&size=5` is called, **Then** the response contains up to 5 categories and correct metadata.
2. **Given** 30 categories exist, **When** `GET /categories?page=1&size=10` is called, **Then** the second page of 10 categories is returned.
3. **Given** no query parameters are provided, **When** `GET /categories` is called, **Then** defaults (`page=0`, `size=20`, sort `name,asc`) are applied automatically.
4. **Given** categories named "Electronics" and "Food" exist, **When** `GET /categories?name=elec` is called, **Then** only "Electronics" is returned (case-insensitive partial match).
5. **Given** an invalid sort field is provided, **When** the request is processed, **Then** the system falls back to `name,asc` without returning an error.
6. **Given** no categories match the provided name filter, **When** the request is processed, **Then** an empty page with `totalElements=0` is returned — not a 404.

---

### User Story 2 — Paginated Order Listing (Priority: P2)

An admin queries all orders and receives a paginated, filterable list. The admin can narrow results by delivery status, payment type, or creation date range.

**Why this priority**: Orders currently have no collection endpoint. Adding the paginated base endpoint is foundational; filters are layered on top once the base works.

**Independent Test**: `GET /orders?page=0&size=10` with a valid auth token returns at most 10 orders plus pagination metadata.

**Acceptance Scenarios**:

1. **Given** orders exist, **When** `GET /orders?page=0&size=10` is called with a valid auth token, **Then** the response contains up to 10 orders with correct pagination metadata.
2. **Given** orders in various delivery statuses exist, **When** `GET /orders?status=DELIVERED` is called, **Then** only orders with status `DELIVERED` are returned.
3. **Given** orders paid by different methods exist, **When** `GET /orders?paymentType=CREDIT_CARD` is called, **Then** only credit-card orders are returned.
4. **Given** orders from various dates exist, **When** `GET /orders?createdAfter=2026-01-01&createdBefore=2026-03-01` is called, **Then** only orders whose creation date falls within that range (inclusive) are returned.
5. **Given** multiple filters are provided simultaneously, **When** the request is processed, **Then** all filters are applied with AND logic.
6. **Given** no parameters are provided, **When** `GET /orders` is called, **Then** defaults (`page=0`, `size=20`, sort `createdAt,desc`) are applied.

---

### Edge Cases

- What happens when `page` or `size` are negative or zero? A descriptive HTTP 400 validation error is returned.
- What happens when `size` exceeds 100? The page size is capped at 100.
- What happens when no items match the filters? An empty page with `totalElements=0` is returned — not a 404.
- What happens when an invalid `status` enum value is passed to the orders endpoint? An HTTP 400 validation error with a descriptive message is returned.
- What happens when `createdAfter` is after `createdBefore`? An HTTP 400 validation error indicating the date range is invalid is returned.
- What happens when `name` is an empty string on the categories endpoint? Blank is treated as omitted — no name filter is applied.
- What happens when an out-of-range page number is requested? An empty result set with valid pagination metadata is returned.

---

## Requirements *(mandatory)*

### Functional Requirements

#### Category Search & Pagination

- **FR-001**: The system MUST return categories in a paginated format including metadata: total items, total pages, current page number, and page size.
- **FR-002**: The system MUST accept `page` (0-based, default 0) and `size` (default 20, max 100) query parameters for category listing.
- **FR-003**: The system MUST allow filtering categories by `name` (case-insensitive partial match). This filter is optional; omitting it returns all categories.
- **FR-004**: The system MUST allow sorting category results by `name` or `createdAt`. Default sort is `name,asc`. Invalid sort fields MUST fall back to the default without returning an error.
- **FR-005**: The category listing endpoint MUST be accessible without authentication.

#### Order Search & Pagination

- **FR-006**: The system MUST expose `GET /orders` as a paginated collection endpoint returning orders with metadata: total items, total pages, current page, and page size.
- **FR-007**: The system MUST accept `page` (0-based, default 0) and `size` (default 20, max 100) query parameters for order listing.
- **FR-008**: The system MUST allow filtering orders by `status` (exact match on delivery status). This filter is optional.
- **FR-009**: The system MUST allow filtering orders by `paymentType` (exact match on payment type). This filter is optional.
- **FR-010**: The system MUST allow filtering orders by date range using `createdAfter` and `createdBefore` (ISO-8601 date, inclusive on both ends). Both are individually optional. When both are provided and `createdAfter` > `createdBefore`, the system MUST return HTTP 400.
- **FR-011**: When multiple filter parameters are provided for orders, the system MUST apply all filters with AND logic.
- **FR-012**: The system MUST allow sorting order results by `totalPrice` or `createdAt`. Default sort is `createdAt,desc`. Invalid sort fields MUST fall back to the default without returning an error.
- **FR-013**: The order listing endpoint MUST require authentication.

#### Shared / Cross-Cutting

- **FR-014**: All new paginated endpoints MUST enforce a maximum page size of 100.
- **FR-015**: All new paginated endpoints MUST validate that `page >= 0` and `size >= 1`; invalid values MUST return a descriptive HTTP 400 response.
- **FR-016**: No new DTO or response wrapper classes are introduced. Existing `CategoryDto` and `OrderDto` are reused. Responses use the framework's native paginated type wrapped in the existing `ApiResponseDto`.

### Key Entities

- **Category**: Represents a product grouping. Key attributes: `id` (UUID), `name` (unique String), `createdAt`. Filterable by name.
- **Order**: Represents a customer purchase. Key attributes: `id` (UUID), `paymentType` (enum), `totalPrice` (double), delivery `status` (enum), `createdAt`. Filterable by status, payment type, and creation date range.
- **Paginated Response**: The framework-native paginated structure wrapping a list of DTOs plus metadata (page number, page size, total elements, total pages, last-page flag). No custom wrapper class.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `GET /categories` no longer returns an unbounded list; every response is paginated with correct metadata.
- **SC-002**: `GET /orders` (new endpoint) returns paginated order results with correct metadata and respects all filter parameters.
- **SC-003**: Filtering by name on categories and by status, payment type, and date range on orders — individually and in combination — returns only matching records with no false positives or false negatives.
- **SC-004**: Invalid input (bad pagination values, inverted date range, unknown enum value) always returns a clear HTTP 400 response — never an HTTP 500.
- **SC-005**: Paginated queries for both entities respond in under 500ms for datasets up to 10,000 rows.
- **SC-006**: The implementation is consistent with the Product search pattern from feature 003 — no new conventions, wrapper types, or DTO classes are introduced.

---

## Assumptions

- Category listing is public (no auth required), consistent with the existing Product catalog endpoint.
- Order listing requires authentication because orders contain financial and personal delivery data.
- Allowed sort fields are limited to indexed or low-cardinality fields to avoid unbounded sort scans.
- `createdAfter` / `createdBefore` filter on the `createdAt` field inherited from the `BaseEntity` superclass, which is present on all entities.
- The existing global exception handler already covers `IllegalArgumentException` → HTTP 400 (confirmed in feature 003 code review). Date-range validation reuses the same mechanism.
- Cart and CartItem are user-session-scoped; they have no public collection endpoints and are out of scope.
- No full-text or fuzzy search is required; database-level `LIKE` filtering is sufficient.
- Existing `CategoryDto` and `OrderDto` are sufficient to represent the paginated items; no new DTOs are needed.

---

## Clarifications

### Session 2026-03-17

- **Scope**: Category and Order are the only two entities with collection-level exposure gaps. Cart/CartItem are explicitly out of scope.
- **Consistency**: All implementation patterns — Specification composition, `sanitizeSort`, `@PageableDefault`, `JpaSpecificationExecutor` — must mirror feature 003 exactly.
- **Breaking change**: `GET /categories` changing from `List` to `Page` response is a breaking API change for existing clients. No backward-compatibility shim is added; the change is intentional.

---

## Post-Implementation Notes

### Caching Bug (Discovered 2026-03-17, Fixed Same Day)

During implementation, `@Cacheable(value = CacheConstants.CATEGORIES, key = "'all'")` was inadvertently added to `CategoryServiceImpl.findAll(String name, Pageable pageable)` — a hardcoded static key that violates Decision 7.

**Bug behaviour**: All paginated category queries (`GET /categories?name=elec`, `GET /categories?page=1&size=20&sort=createdAt,desc`, etc.) returned the same cached response — the result of whichever query executed first — regardless of query parameters.

**Fix**: Removed the `@Cacheable` annotation from `findAll(String, Pageable)`. The following remain unchanged and correct:
- `@Cacheable(key="#categoryId")` on `findById()` — single-entity caching, correct
- `@CacheEvict(allEntries=true)` on `save()`, `deleteById()`, `updateById()` — correct
