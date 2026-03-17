# Implementation Plan: Architecture Refactor & Enhancement

**Branch**: `005-architecture-refactor` | **Date**: 2026-03-18 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/005-architecture-refactor/spec.md`

## Summary

A comprehensive refactoring of the ecommerce application to establish production-grade foundations: transactional integrity via `@Transactional` annotations, monetary precision via `BigDecimal`, separated request/response DTOs with bean validation, JWT-based authentication with role-based access, optimistic locking for concurrency safety, lazy-loading with `@EntityGraph`, targeted cache eviction, order state machine, health/metrics endpoints, and idempotent order creation.

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3.0.0, Spring Data JPA (Hibernate), Spring Security, JJWT 0.11.5, MapStruct 1.6.0, Lombok, SpringDoc OpenAPI 2.0.2, Spring Boot Actuator
**Storage**: MySQL 8.0 (primary), Redis (cache layer via Lettuce)
**Testing**: spring-boot-starter-test (JUnit 5, Mockito), Testcontainers (future)
**Target Platform**: Linux/Windows server (JVM 17+)
**Project Type**: REST web-service (Spring Boot)
**Performance Goals**: Standard web-service latency (<500ms p95 for CRUD, <1s for paginated search)
**Constraints**: Stateless auth (no server-side sessions), eventual consistency on cache within TTL
**Scale/Scope**: Single-service monolith, ~65 Java files, 4 domain aggregates (Product, Category, Order, Cart)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Layered Architecture | PASS | All changes respect controller/service/repository separation. No business logic in controllers. |
| II. DTO-First Communication | PASS | Splitting single DTOs into request/response models strengthens this principle. MapStruct remains the mapping layer. |
| III. JWT Stateless Authentication | PASS | Adding Spring Security + JWT fulfills this previously-documented-but-unimplemented principle. SessionCreationPolicy.STATELESS will be enforced. |
| IV. Interface-Driven Design | PASS | All new services/controllers will follow interface + impl pattern. Existing pattern preserved. |
| V. Async Messaging for Side Effects | N/A | No messaging changes in scope. ActiveMQ integration is out of scope. |
| VI. Observability | PASS | Adding Actuator health/metrics aligns with observability requirements. SLF4J logging preserved. No System.out.println. |

**Gate Result**: PASS — no violations. Proceed to Phase 0.

### Post-Phase 1 Design Re-Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Layered Architecture | PASS | Request DTOs validated in controller layer, business logic stays in services, repositories handle data access. No layer violations in design. |
| II. DTO-First Communication | PASS | Strengthened — separate Create/Update request DTOs and Response DTOs. Entities cleaned of all Jackson annotations. MapStruct handles all mapping. |
| III. JWT Stateless Authentication | PASS | SecurityConfig enforces STATELESS sessions. JwtAuthenticationFilter is OncePerRequestFilter. Whitelisted endpoints match constitution table (plus /products/**, /categories/** for public browsing, /actuator/health). |
| IV. Interface-Driven Design | PASS | New AuthService/AuthController follow interface + impl pattern. All existing interfaces updated with new signatures. |
| V. Async Messaging for Side Effects | N/A | No changes. |
| VI. Observability | PASS | Actuator health/metrics added. SLF4J via @Slf4j. No System.out.println. Authorization header excluded from logging by CommonsRequestLoggingFilter config. |

**Post-Design Gate Result**: PASS — no violations detected in Phase 1 design artifacts.

## Project Structure

### Documentation (this feature)

```text
specs/005-architecture-refactor/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── product-api.md
│   ├── category-api.md
│   ├── order-api.md
│   └── auth-api.md
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
src/main/java/com/app/ecommerce/
├── auth/                        # NEW: Authentication domain
│   ├── AuthController.java      # Interface
│   ├── AuthControllerImpl.java  # Login, register, refresh
│   ├── AuthService.java         # Interface
│   ├── AuthServiceImpl.java     # Credential validation, token issuance
│   ├── User.java                # JPA entity
│   ├── UserRepository.java      # Spring Data JPA
│   ├── Token.java               # JPA entity (token tracking)
│   ├── TokenRepository.java     # Spring Data JPA
│   ├── Role.java                # Enum (ADMIN, CUSTOMER)
│   ├── RegisterRequest.java     # Request DTO
│   ├── LoginRequest.java        # Request DTO
│   ├── LoginResponse.java       # Response DTO
│   └── RefreshTokenRequest.java # Request DTO
├── cart/
│   ├── Cart.java                # MODIFIED: add @Version, remove @JsonIgnore
│   ├── CartItem.java            # MODIFIED: remove @JsonIgnore
│   ├── CartDto.java             # KEPT: read-only responses (no create/update DTOs needed)
│   ├── CartItemDto.java         # KEPT
│   ├── CartMapper.java          # KEPT
│   ├── CartItemMapper.java      # KEPT
│   ├── CartService.java         # KEPT
│   ├── CartServiceImpl.java     # MODIFIED: add @Transactional
│   ├── CartRepository.java      # KEPT
│   └── CartItemRepository.java  # KEPT
├── category/
│   ├── Category.java            # MODIFIED: add @Version, remove @JsonIgnore, LAZY fetch
│   ├── CategoryDto.java         # RENAMED → CategoryResponse.java
│   ├── CreateCategoryRequest.java   # NEW
│   ├── UpdateCategoryRequest.java   # NEW
│   ├── CategoryMapper.java          # MODIFIED: new mapping methods
│   ├── CategoryController.java      # MODIFIED: new request/response types
│   ├── CategoryControllerImpl.java  # MODIFIED: PATCH for update
│   ├── CategoryService.java         # MODIFIED: new method signatures
│   ├── CategoryServiceImpl.java     # MODIFIED: @Transactional, validation
│   ├── CategoryRepository.java      # KEPT
│   └── CategorySpecifications.java  # KEPT
├── order/
│   ├── Order.java               # MODIFIED: BigDecimal totalPrice, @Version, remove @JsonIgnore
│   ├── DeliveryInfo.java        # MODIFIED: String date → LocalDate date
│   ├── OrderDto.java            # RENAMED → OrderResponse.java
│   ├── DeliveryInfoDto.java     # RENAMED → DeliveryInfoResponse.java
│   ├── CreateOrderRequest.java  # NEW
│   ├── UpdateOrderRequest.java  # NEW
│   ├── OrderMapper.java         # MODIFIED: new mapping methods
│   ├── DeliveryInfoMapper.java  # MODIFIED: LocalDate mapping
│   ├── OrderController.java     # MODIFIED: new request/response types, idempotency header
│   ├── OrderControllerImpl.java # MODIFIED: PATCH for update, idempotency
│   ├── OrderService.java        # MODIFIED: new method signatures
│   ├── OrderServiceImpl.java    # MODIFIED: @Transactional, state machine, BigDecimal
│   ├── OrderRepository.java     # MODIFIED: @EntityGraph
│   ├── OrderSpecifications.java # KEPT
│   ├── OrderStatusTransition.java  # NEW: state machine validation
│   └── IdempotencyRecord.java   # NEW: JPA entity for idempotency keys
├── product/
│   ├── Product.java             # MODIFIED: BigDecimal price, @Version, remove @JsonIgnore, LAZY fetch
│   ├── ProductDto.java          # RENAMED → ProductResponse.java
│   ├── CreateProductRequest.java    # NEW
│   ├── UpdateProductRequest.java    # NEW
│   ├── ProductMapper.java           # MODIFIED: new mapping methods
│   ├── ProductController.java       # MODIFIED: new request/response types
│   ├── ProductControllerImpl.java   # MODIFIED: PATCH for update
│   ├── ProductService.java          # MODIFIED: new method signatures
│   ├── ProductServiceImpl.java      # MODIFIED: @Transactional, BigDecimal, targeted eviction
│   ├── ProductRepository.java       # MODIFIED: @EntityGraph
│   └── ProductSpecifications.java   # MODIFIED: BigDecimal price params
├── shared/
│   ├── config/
│   │   ├── ApplicationConfig.java           # KEPT
│   │   ├── CacheConfig.java                 # KEPT (RedisCacheErrorHandler handles degradation)
│   │   ├── HttpLoggingConfig.java           # KEPT
│   │   ├── JpaConfig.java                   # MODIFIED: AuditorAware from SecurityContext
│   │   ├── OpenApiDocumentationConfig.java  # KEPT
│   │   ├── RedisCacheErrorHandler.java      # KEPT
│   │   └── SecurityConfig.java              # NEW: Spring Security filter chain
│   ├── constants/
│   │   └── CacheConstants.java              # KEPT
│   ├── dto/
│   │   ├── BaseDto.java                     # RENAMED → BaseResponse.java
│   │   ├── ApiResponseDto.java              # KEPT
│   │   └── ErrorResponseDto.java            # MODIFIED: add OptimisticLock error factory
│   ├── entity/
│   │   └── BaseEntity.java                  # KEPT (already has audit fields)
│   ├── enums/
│   │   ├── Status.java                      # KEPT
│   │   └── PaymentType.java                 # KEPT
│   ├── exception/
│   │   ├── RestExceptionHandler.java                # MODIFIED: add OptimisticLockException, AccessDenied handlers
│   │   ├── DuplicatedUniqueColumnValueException.java # KEPT
│   │   └── InvalidStateTransitionException.java     # NEW
│   ├── idempotency/
│   │   ├── IdempotencyRecord.java           # NEW: JPA entity
│   │   ├── IdempotencyRepository.java       # NEW
│   │   └── IdempotencyService.java          # NEW
│   └── security/
│       ├── JwtAuthenticationFilter.java     # NEW: OncePerRequestFilter
│       ├── JwtService.java                  # NEW: token generation/validation
│       └── SecurityUserDetailsService.java  # NEW: UserDetailsService impl
```

**Structure Decision**: The project follows a domain-driven package structure (product/, category/, order/, cart/) with shared infrastructure in shared/. New domains (auth/) follow the same pattern. No monorepo or multi-module changes — this remains a single Maven module.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| IdempotencyRecord entity + service | Prevents duplicate orders (FR-028) | Client-side dedup is unreliable for network-level retries; DB-level idempotency key is the standard approach |
| OrderStatusTransition (state machine) | Enforces valid status transitions (FR-024) | A simple if/else in the service method is fragile and violates OCP when new states are added |
