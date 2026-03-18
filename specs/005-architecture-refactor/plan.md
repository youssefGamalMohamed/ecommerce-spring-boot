# Implementation Plan: Architecture Refactor — Phase 2 (Cleanup & Polish)

**Branch**: `005-architecture-refactor` | **Date**: 2026-03-18 | **Spec**: [spec.md](spec.md)
**Input**: Post-implementation audit of the architecture refactor (T001-T089 complete). This plan covers dead code removal, naming harmonization, mapper cleanup, ResponseEntity builder migration, DRY violations, missing validation, and critical architecture fixes.

## Summary

After completing the initial architecture refactor (10 user stories, 89 tasks), a comprehensive codebase audit identified **10 categories of remaining issues**: dead DTO classes, inconsistent Dto/Response naming, dead mapper methods (including unused collection methods), `new ResponseEntity<>` patterns, duplicated utility logic, missing `@Valid` annotations, unused exception declarations, a critical cascade bug, missing method-level authorization, and inefficient token revocation. This plan addresses all findings.

## Technical Context

**Language/Version**: Java 17 + Spring Boot 3.0.0
**Primary Dependencies**: Spring Data JPA, Spring Security, JJWT 0.11.5, MapStruct 1.6.0, Lombok, SpringDoc OpenAPI 2.0.2
**Storage**: MySQL (primary) + Redis (cache)
**Testing**: `mvn test` (Maven Surefire)
**Target Platform**: Linux/Docker server
**Project Type**: REST web-service (ecommerce backend)

## Constitution Check

*Pre-Phase 0 Gate*:

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Layered Architecture | PASS | Changes are within existing layers |
| II. DTO-First Communication | PASS | Naming now matches role (Response suffix) |
| III. JWT Stateless Auth | PASS | Adding @PreAuthorize strengthens auth |
| IV. Interface-Driven Design | PASS | Interfaces updated alongside implementations |
| V. Async Messaging | N/A | No MQ changes |
| VI. Observability | N/A | No logging changes |

## Project Structure

### Documentation (this feature)

```text
specs/005-architecture-refactor/
├── plan.md              # This file (Phase 2 cleanup plan)
├── research.md          # Updated with R-014 through R-023
├── data-model.md        # No changes needed
├── quickstart.md        # Updated with Phase 11 steps
├── contracts/           # No changes needed
└── tasks.md             # Updated with T090-T119
```

### Source Code (repository root)

```text
src/main/java/com/app/ecommerce/
├── auth/                — AuthControllerImpl: ApiResponse rename
├── cart/                — CartDto→CartResponse, CartItemDto→CartItemResponse, mapper cleanup
├── category/            — CategoryDto DELETE, mapper cleanup, @PreAuthorize
├── order/               — OrderDto/DeliveryInfoDto DELETE, mapper cleanup
├── product/             — ProductDto DELETE, mapper cleanup, @PreAuthorize
├── shared/
│   ├── dto/             — BaseDto DELETE, ApiResponseDto→ApiResponse, ErrorResponseDto→ErrorResponse
│   ├── exception/       — RestExceptionHandler: ErrorResponse rename + builder migration
│   └── util/            — NEW: SortUtils.java
```

## Findings Summary

| # | Research | Category | Severity |
|---|----------|----------|----------|
| R-019 | **CascadeType.REMOVE on inverse ManyToMany** | **DATA LOSS BUG** | **CRITICAL** |
| R-020 | Missing @PreAuthorize on admin endpoints | Security gap | High |
| R-017 | Missing @Valid on OrderController | Validation gap | High |
| R-022 | Rename ApiResponseDto/ErrorResponseDto/CartDto/CartItemDto | Naming harmony | Medium (user-requested) |
| R-023 | **Remove dead collection mapper methods** | Dead code + OOP design | Medium |
| R-014 | Dead DTO classes & old mapper methods | Dead code | Medium |
| R-016 | Duplicated `sanitizeSort` | DRY violation | Medium |
| R-021 | Token revocation N+1 | Performance | Medium |
| R-015 | ResponseEntity builder pattern | Code style | Low |
| R-018 | Unused `JsonProcessingException` | Dead code | Low |

## Mapper Method Naming Convention (Architect's Decision — R-023)

**Question**: Should `mapToResponseList`/`mapToResponseSet` use overloading?

**Answer**: **Remove them entirely.** They have zero callers. The codebase correctly uses:
```java
Page<Response> result = repository.findAll(spec, pageable).map(mapper::mapToResponse);
```
This uses Java method references with the single-item `mapToResponse(Entity)` method — no explicit collection methods needed. MapStruct auto-generates collection mapping implementations when referenced by other mappers.

**If a future need arises**: Use overloaded `mapToResponse(List<Entity>)` — this is the OOP-correct approach. MapStruct supports it. But don't pre-declare unused methods.

**Final mapper state** (per mapper, only actively-used methods):

| Mapper | Methods |
|--------|---------|
| ProductMapper | `mapToEntity(CreateProductRequest)`, `mapToEntity(Product, Set<Category>)`, `updateEntityFromRequest(...)`, `updateEntityFromEntity(...)`, `mapToResponse(Product)` |
| CategoryMapper | `mapToEntity(CreateCategoryRequest)`, `updateEntityFromRequest(...)`, `mapToResponse(Category)` |
| OrderMapper | `mapToEntity(CreateOrderRequest)`, `updateEntityFromRequest(...)`, `mapToResponse(Order)` |
| DeliveryInfoMapper | `mapToResponse(DeliveryInfo)` |
| CartMapper | `mapToResponse(Cart)` |
| CartItemMapper | `mapToResponse(CartItem)` |

## Naming Convention After Refactor

| Purpose | Pattern | Examples |
|---------|---------|---------|
| Create request | `Create{Entity}Request` | `CreateProductRequest` |
| Update request | `Update{Entity}Request` | `UpdateProductRequest` |
| Domain response | `{Entity}Response` | `ProductResponse`, `CartResponse`, `CartItemResponse` |
| API wrapper | `ApiResponse<T>` | Was `ApiResponseDto` |
| Error wrapper | `ErrorResponse` | Was `ErrorResponseDto` |
| Base response | `BaseResponse` | Abstract parent for domain responses |
| Single mapper | `mapToResponse(Entity)` | Only method needed |

## Complexity Tracking

> No constitution violations — all changes are simplifications.

| Change | Rationale |
|--------|-----------|
| New `SortUtils` class | 3 identical methods → 1 utility. Net code reduction. |
| File renames (4 files) | Naming consistency. No new patterns. |
| Method removal (~30 methods) | Net code reduction. Removing dead code. |
