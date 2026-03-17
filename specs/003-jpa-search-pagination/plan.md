# Implementation Plan: JPA Search with Pagination, Filtering, and Sorting

**Branch**: `003-jpa-search-pagination` | **Date**: 2026-03-17 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/003-jpa-search-pagination/spec.md`

## Summary

Replace the existing `GET /products?category=` (required param, no pagination) with a fully-featured `GET /products` collection endpoint that accepts optional filter parameters (name, price range, categoryId), Spring-native sort (`sort=field,dir`) and pagination (page/size) via `Pageable`. This follows REST API best practice: query parameters on the collection resource, not a `/search` suffix. Uses `JpaSpecificationExecutor` + `Specification<Product>` for dynamic query composition. Returns `Page<ProductDto>` wrapped in the existing `ApiResponseDto` — no new DTO classes. Also resolves the pending TODO(PRODUCTS_WHITELIST) by whitelisting `GET /products/**` in the security configuration.

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3.0.0, Spring Data JPA (Hibernate), MapStruct 1.6.0, Lombok, SpringDoc OpenAPI 2.0.2
**Storage**: MySQL (primary), Redis (cache layer — existing; no cache changes needed for search)
**Testing**: Maven (`mvn test`), JUnit 5 (Spring Boot managed)
**Target Platform**: Linux/Windows server (Spring Boot embedded Tomcat)
**Project Type**: REST web service (single-module Maven mono-repo)
**Performance Goals**: Search queries < 500ms for catalog of up to 10,000 products (SC-006)
**Constraints**: Page size capped at 100 via `spring.data.web.pageable.max-page-size`; sort fields whitelisted in service layer
**Scale/Scope**: Single entity search (Product); pattern can be reused for other entities later

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Layered Architecture | PASS | Controller handles HTTP only; service owns all business logic; repository is the sole DB access point. `ProductSpecifications` static helpers live in the product package. |
| II. DTO-First Communication | PASS | Existing `ProductDto` is reused as the page item type. `Page<ProductDto>` is the response — no new wrapper DTOs. `Product` entity is never serialized directly. MapStruct `productMapper::toDto` handles conversion. |
| III. JWT Stateless Auth | PASS (with action) | FR-014 requires a public endpoint. Resolves TODO(PRODUCTS_WHITELIST): `GET /products/**` whitelisted in SecurityConfiguration. Only GET is open; POST/PUT/DELETE on `/products/**` remain protected. |
| IV. Interface-Driven Design | PASS | Existing `findProductsByCategoryName` in both interface and impl is replaced by the new `findAll()` method. Both interface and implementation are updated together. |
| V. Async Messaging | N/A | No side effects triggered by search. |
| VI. Observability | PASS | `@Slf4j` already on `ProductServiceImpl`; no inline `System.out.println`. |

**Constitution Check Result**: All gates pass. The replacement of `findProductsByCategoryName` with the general-purpose paginated `findAll` is a deliberate, non-breaking improvement to the collection endpoint.

## Project Structure

### Documentation (this feature)

```text
specs/003-jpa-search-pagination/
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
├── product/
│   ├── Product.java                        — existing entity (unchanged)
│   ├── ProductController.java              — MODIFIED: replace findProductsByCategoryName()
│   │                                         with findAll(filters, Pageable)
│   ├── ProductControllerImpl.java          — MODIFIED: replace @GetMapping bare method
│   │                                         with paginated findAll() implementation
│   ├── ProductDto.java                     — existing (reused as Page item type — unchanged)
│   ├── ProductMapper.java                  — existing (unchanged)
│   ├── ProductRepository.java              — MODIFIED: add JpaSpecificationExecutor<Product>
│   ├── ProductService.java                 — MODIFIED: replace findAllByCategoryName()
│   │                                         with findAll(filters, Pageable)
│   ├── ProductServiceImpl.java             — MODIFIED: implement findAll() + sanitizeSort()
│   └── ProductSpecifications.java          — NEW: Criteria API predicate factory
└── shared/
    └── config/
        └── SecurityConfiguration.java      — MODIFIED: whitelist GET /products/**
src/main/resources/
└── application.properties                  — MODIFIED: spring.data.web.pageable defaults
```

**Structure Decision**: Feature-based package layout matching existing codebase conventions. The sole new file is `ProductSpecifications.java` in `product/`. All other changes modify existing files.

**Breaking change note**: `GET /products?category=name` (required param, non-paginated) is replaced by `GET /products` (all params optional, paginated). Any existing API consumer relying on the required `category` param must migrate to `GET /products?categoryId=<uuid>` or will receive a different response shape. Acceptable since this is an in-progress feature branch.

## Complexity Tracking

> No constitution violations requiring justification.
