# Research: JPA Search with Pagination, Filtering, and Sorting

**Branch**: `003-jpa-search-pagination` | **Date**: 2026-03-17

---

## Decision 1: Dynamic Query Strategy

**Decision**: Use Spring Data JPA `Specification<T>` (Criteria API) + `JpaSpecificationExecutor<T>`

**Rationale**: Spring Data's `Specification` API is the idiomatic way to build dynamic, composable WHERE clauses without writing JPQL or native SQL. It integrates natively with `JpaRepository` via `JpaSpecificationExecutor`, works seamlessly with `Pageable`, and avoids string concatenation SQL injection risk.

**Alternatives considered**:
- `@Query` with JPQL — rejected: requires a fixed query per combination; cannot handle optional filters cleanly.
- QueryDSL — rejected: requires additional codegen dependency not currently in the project.
- Custom `@Query` with `IS NULL OR x = :param` tricks — rejected: poor performance (cannot use indexes efficiently) and hard to maintain.

---

## Decision 2: Pagination & Sorting

**Decision**: Use Spring Data's `Pageable` / `PageRequest` and `Sort` APIs; return `Page<T>` from the repository.

**Rationale**: `PageRequest.of(page, size, Sort)` constructs a type-safe pageable object. `Page<T>` carries `.getTotalElements()`, `.getTotalPages()`, `.getNumber()`, `.getSize()`, `.isLast()` automatically. No manual COUNT query needed.

**Sort field whitelist**: Allowed fields are `name`, `price`, `createdAt`. Any unrecognized value falls back to `createdAt DESC` without error (FR-010). The service layer's `sanitizeSort()` helper handles this.

**Alternatives considered**:
- Returning `List<T>` with manual count — rejected: duplicates work already built into Spring Data.

---

## Decision 3: Request Parameters

**Decision**: Accept all search parameters as `@RequestParam` query parameters on a `GET` endpoint. Pagination and sort are delegated to Spring's `Pageable` argument resolver (`page`, `size`, `sort=field,direction`).

**Alternatives considered**:
- Request body on GET — rejected: non-standard HTTP, unsupported by many HTTP clients/proxies.
- Custom `sortBy`/`sortDir` params — rejected: Spring's `Pageable` resolver already handles `sort=field,dir` natively; duplicating it adds unnecessary code.

---

## Decision 4: Response Type

**Decision**: Return Spring Data JPA's `Page<ProductDto>` directly as the `data` field inside the existing `ApiResponseDto<Page<ProductDto>>` envelope.

**Rationale**: Reuses the existing `ProductDto` and `ApiResponseDto` — no new DTO or wrapper class. The serialized `Page<T>` includes `content`, `totalElements`, `totalPages`, `number`, `size`, `last`, `first`, etc. automatically.

**Clarification**: User explicitly requested this approach (session 2026-03-17): "use the spring data jpa Page and Pageable to return the data into it."

**Alternatives considered**:
- Custom `ProductPageResponse` wrapper — rejected by user (would re-invent the wheel).

---

## Decision 5: `Specification` composition pattern

**Decision**: Static factory class `ProductSpecifications` with individual predicate methods; service composes with `Specification.where(null).and(...)`.

**Rationale**: Each filter rule is isolated and independently testable. Null arguments produce no predicate, so composition is safe with any combination of provided/omitted filters.

---

## Decision 6: Security — Public Endpoint

**Decision**: Whitelist `GET /products/**` in `SecurityConfiguration`.

**Rationale**: FR-014 requires public access. The constitution already has a pending TODO(PRODUCTS_WHITELIST) explicitly noting this decision must be made. This feature resolves it. Only GET is opened; write operations remain protected.

---

## Decision 7: Endpoint Design — REST Best Practice (Updated)

**Decision**: Map the search/pagination/filter endpoint to `GET /products` (the collection resource), not `GET /products/search`.

**Rationale**: REST best practice is to filter/sort/paginate a collection resource via query parameters on the collection URL itself. A `/search` suffix implies a separate resource or RPC-style action, which violates the resource-oriented REST model. The industry standard (used by GitHub API, Stripe, Shopify, etc.) is:

```
GET /products?name=phone&minPrice=50&sort=price,asc&page=0&size=10
```

**Impact on existing code**: The existing `GET /products` is currently mapped to `findProductsByCategoryName`, which accepts a **required** `category` query parameter and returns an untyped, non-paginated list. This method is **superseded** by the new general-purpose `GET /products` endpoint — the category filter becomes one of several optional query params (`categoryId`).

**Alternatives considered**:
- `GET /products/search` — rejected: `/search` suffix is RPC-style, not resource-oriented; violates REST conventions.
- Keep both endpoints — rejected: two endpoints for the same collection causes confusion; the old one is strictly less capable.

---

## Decision 8: Method Naming Convention

**Decision**: Rename the controller/service method from `findProductsByCategoryName` / `findAllByCategoryName` to `findAll` (controller interface + implementation) and `findAll` (service interface + implementation).

**Rationale**: `findAll` is the idiomatic Spring Data name for a collection listing with optional parameters. It matches the REST endpoint semantics of `GET /products`.
