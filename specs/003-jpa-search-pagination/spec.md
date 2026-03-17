# Feature Specification: JPA Search with Pagination, Filtering, and Sorting

**Feature Branch**: `003-jpa-search-pagination`
**Created**: 2026-03-17
**Status**: Draft
**Input**: User description: "i want you to create to me a search functionality for my project with JPA to allow me to return data from JPA paginated and also when i pass to it other values it should filter with them and also sort functionallity should be available, please work with the functionlity that spring eco-systems provides"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Paginated Product Listing (Priority: P1)

A client browses the product catalog and receives a paginated list of products. The client specifies a page number and page size; the system returns only that page along with metadata (total items, total pages, current page).

**Why this priority**: Pagination is the foundational requirement — without it, every other search/filter/sort feature is unusable at scale. All other stories depend on this working first.

**Independent Test**: Can be fully tested by calling the products endpoint with only `page` and `size` parameters and verifying the response contains paginated data with correct metadata.

**Acceptance Scenarios**:

1. **Given** products exist in the catalog, **When** a request is made with `page=0` and `size=10`, **Then** the response contains up to 10 products plus pagination metadata (totalElements, totalPages, currentPage, pageSize).
2. **Given** 25 products exist, **When** a request is made with `page=2` and `size=10`, **Then** the response contains 5 products on the third page.
3. **Given** no parameters are provided, **When** the endpoint is called, **Then** a default page (page=0, size=20) is applied automatically.
4. **Given** an out-of-range page number is requested, **When** the request is processed, **Then** an empty result set is returned with valid pagination metadata.

---

### User Story 2 - Filtered Product Search (Priority: P2)

A client searches for products using one or more filter criteria (e.g., name keyword, price range, category). The system returns only matching products, paginated.

**Why this priority**: Filtering is the core value of the search feature. It enables clients to find relevant products without browsing the entire catalog.

**Independent Test**: Can be fully tested by passing filter parameters (e.g., `name=shirt`, `minPrice=10`, `maxPrice=100`) and verifying only matching products are returned.

**Acceptance Scenarios**:

1. **Given** products with various names exist, **When** a request is made with `name=phone`, **Then** only products whose name contains "phone" (case-insensitive) are returned.
2. **Given** products at various price points exist, **When** a request includes `minPrice=50` and `maxPrice=200`, **Then** only products within that price range are returned.
3. **Given** products belonging to different categories exist, **When** a request includes `categoryId=<uuid>`, **Then** only products belonging to that category are returned.
4. **Given** multiple filter parameters are provided simultaneously, **When** the request is processed, **Then** all filters are applied together (AND logic) and only products satisfying all criteria are returned.
5. **Given** no products match the provided filters, **When** the request is processed, **Then** an empty page is returned with zero totalElements.

---

### User Story 3 - Sorted Product Results (Priority: P3)

A client requests products sorted by a specific field (e.g., price ascending, name descending). The system returns the paginated results in the requested order.

**Why this priority**: Sorting enhances usability after pagination and filtering are in place. It is independently deployable as it only changes result ordering, not filtering logic.

**Independent Test**: Can be fully tested by passing `sort=price,asc` on `GET /products` and verifying the returned products are ordered by price from lowest to highest.

**Acceptance Scenarios**:

1. **Given** products with varied prices exist, **When** a request specifies `sort=price,asc`, **Then** products are returned in ascending price order.
2. **Given** products with varied names exist, **When** a request specifies `sort=name,desc`, **Then** products are returned in reverse alphabetical order by name.
3. **Given** an invalid sort field is provided, **When** the request is processed, **Then** the system falls back to the default sort (by creation date descending) and returns results without error.
4. **Given** no sort parameters are provided, **When** the request is processed, **Then** a default sort order (creation date descending) is applied.

---

### Edge Cases

- What happens when both filter and sort parameters are combined? All filters are applied first, then sorting is applied to the filtered result set.
- What happens when `page` or `size` are negative or zero? The system returns a validation error with a descriptive message.
- What happens when `size` exceeds the maximum allowed page size (100)? The system caps the page size at the maximum or returns a validation error.
- How does the system handle a `name` filter with special characters? Input is treated as a safe parameterized value; no raw string concatenation occurs.
- What happens when `minPrice` is greater than `maxPrice`? A validation error is returned indicating the price range is invalid.
- What happens when `categoryId` does not match any existing category? An empty result set is returned — not a 404.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST return products in a paginated format, including metadata: total number of items, total pages, current page number, and page size.
- **FR-002**: The system MUST accept `page` (0-based) and `size` query parameters to control pagination; defaults are `page=0` and `size=20` when not provided.
- **FR-003**: The system MUST enforce a maximum page size of 100 items per page.
- **FR-004**: The system MUST allow filtering products by `name` (case-insensitive partial match).
- **FR-005**: The system MUST allow filtering products by `minPrice` and/or `maxPrice` (inclusive range).
- **FR-006**: The system MUST allow filtering products by `categoryId` (exact match on category UUID). The JOIN used to apply this filter MUST NOT cause duplicate rows in either the data query or the COUNT query — `DISTINCT` must be enforced so that `totalElements` and `totalPages` remain accurate when a product belongs to multiple categories.
- **FR-007**: When multiple filter parameters are provided, the system MUST apply all filters with AND logic.
- **FR-008**: The system MUST allow sorting results by any of the following fields: `name`, `price`, `createdAt`. The sort field and direction are expressed together as a single `sort` parameter using the format `sort=field,direction` (e.g., `sort=price,asc`). Multiple `sort` parameters may be supplied.
- **FR-009**: Default sort direction is descending when not specified. The default sort field is `createdAt`.
- **FR-010**: When an invalid or unrecognized sort field is provided, the system MUST fall back to the default sort order (createdAt descending) without returning an error.
- **FR-011**: The system MUST validate that `page` >= 0 and `size` >= 1; invalid values MUST return a descriptive validation error.
- **FR-012**: The system MUST validate that `minPrice` <= `maxPrice` when both are provided; otherwise a validation error MUST be returned.
- **FR-013**: All filter parameters are optional; omitting a filter applies no restriction for that field.
- **FR-014**: The search endpoint MUST be accessible without authentication (public catalog browsing).

### Key Entities

- **Product**: The primary searchable entity. Key attributes: `id`, `name`, `description`, `price`, `quantity`, `categories`, `createdAt`. Represented in API responses by the existing `ProductDto` — no new DTO is created.
- **Category**: Used as a filter dimension. A product may belong to multiple categories. Key attributes: `id`, `name`.
- **Paginated Response**: The framework-native paginated structure that wraps a list of `ProductDto` items and includes metadata (page number, page size, total elements, total pages, whether it is the last page). No custom wrapper class is introduced; the framework's built-in page type is used directly.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Clients can retrieve a paginated product list without filters; the response includes correct metadata reflecting total catalog size.
- **SC-002**: Filtering by name, price range, and category individually and in combination returns only matching products with no false positives or false negatives.
- **SC-003**: Sorted results are consistently ordered — requesting the same sort parameters returns products in the same deterministic order on repeated calls.
- **SC-004**: Invalid input (bad pagination values, inverted price range) always returns a clear, descriptive error message rather than a server error.
- **SC-005**: The search endpoint handles concurrent requests without returning inconsistent or incorrect results.
- **SC-006**: Filtered and paginated queries respond in under 500ms for a catalog of up to 10,000 products.

## Assumptions

- The feature applies primarily to the **Product** entity; the same pattern can be reused for other entities later, but is out of scope here.
- Category filtering uses exact match on `categoryId` (UUID). Text-based category name search is out of scope.
- No full-text search engine is required; database-level filtering is sufficient for the expected catalog size.
- Authentication is not required to access the search endpoint; it is a public-facing API.
- Default sort order is creation date descending (newest first) when no sort parameters are given.
- No new DTO or response wrapper classes are introduced. The existing `ProductDto` is reused as the item type. The response leverages the framework's native paginated structure directly.

## Clarifications

### Session 2026-03-17

- Q: Should the search feature introduce custom response wrapper classes (e.g., a dedicated paginated response DTO) or reuse the framework's native pagination structures and existing DTOs? → A: Reuse the existing `ProductDto` and the framework's native `Page<T>` type directly. No custom `ProductPageResponse` or `ProductSearchRequest` classes are created. Pagination, sorting, and page metadata are handled entirely through the framework's built-in `Pageable` and `Page` support.

### Code Review 2026-03-17

Bugs identified during code review of the initial implementation. Tasks T014–T015 were added to Phase 7 to address these.

- **Bug (HIGH — T014)**: `ProductSpecifications.hasCategory` uses `root.join("categories")` (INNER JOIN) without calling `query.distinct(true)`. When `JpaSpecificationExecutor.findAll(Specification, Pageable)` executes a COUNT query, duplicate rows produced by the join corrupt `totalElements` and `totalPages`. Fix: add `query.distinct(true)` inside the `hasCategory` lambda. This is a correctness requirement for FR-006 and FR-001.
- **Dead code (MEDIUM — T015)**: The no-arg `List<ProductDto> findAll()` method remained in `ProductService` and `ProductServiceImpl` after the paginated overload was added. No controller endpoint calls it. It performs an unbounded full-table scan. Fix: remove it from both the interface and implementation.
