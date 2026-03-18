<!--
SYNC IMPACT REPORT
==================
Version change: 1.0.2 → 2.0.0 (MAJOR — Package conventions rewritten to reflect
005-architecture-refactor domain-based structure; Interface-Driven Design principle
updated to reflect actual naming pattern XxxService/XxxController, not IXxxService/IXxxController;
Added Monetary Precision, Transactional Integrity, State Machine, and Idempotency principles;
Removed references to obsolete packages: controller/framework, controller/impl,
service/framework, service/impl, dtos/, models/, entity/ (top-level), repository/ (top-level),
mq/activemq/, logging/)

Modified principles:
  - Principle I (Layered Architecture): Updated package paths to actual structure
  - Principle II (DTO-First): Updated package references to actual locations
  - Principle III (JWT Auth): Updated security package to shared/security/
  - Principle IV (Interface-Driven Design): Updated naming convention and package locations
  - Principle VI (Observability): No change (still valid)
  - Added: Principle VII (Monetary Precision)
  - Added: Principle VIII (Transactional Integrity)

Added sections: Monetary Precision, Transactional Integrity, State Machine, Idempotency
Removed sections: Principle V (Async Messaging — MQ out of scope post-005-architecture-refactor)
Package & Directory Conventions: Complete rewrite

Templates requiring updates:
  ✅ CLAUDE.md — Full rewrite to reflect domain-based structure and new patterns
  ✅ specs/005-architecture-refactor/tasks.md — All tasks already completed and documented
-->

# Ecommerce API Constitution

## Core Principles

### I. Domain-Based Layered Architecture (NON-NEGOTIABLE)

The application uses **domain-based packaging**. Each business domain owns all its classes
in a single flat package. Shared infrastructure lives in `com.app.ecommerce.shared`.

**Domain packages** (`com.app.ecommerce.<domain>/`):
- Each domain package contains: entity, controller interface + impl, service interface + impl,
  mapper, request DTOs, response DTOs, repository, specifications (if applicable).
- No cross-domain imports except through service interfaces or shared types.
- Domains: `auth`, `cart`, `category`, `order`, `product`.

**Shared package** (`com.app.ecommerce.shared/`):
- `config/` — Spring `@Configuration` classes (Security, JPA, Cache, HttpLogging, OpenAPI)
- `constants/` — Cache key constants
- `dto/` — Shared response wrappers (`ApiResponse<T>`, `ErrorResponse`, `BaseResponse`)
- `entity/` — `BaseEntity` (auditing base for all JPA entities)
- `enums/` — Shared enum types (`PaymentType`, `Status`)
- `exception/` — Global `@RestControllerAdvice` + custom exception types
- `idempotency/` — Idempotency record, repository, and service
- `security/` — JWT filter, JWT service, `SecurityUserDetailsService`
- `util/` — Shared utility classes (`SortUtils`)

Layer responsibilities (within each domain):
- **Controller** handles HTTP concerns only: request parsing, response shaping, HTTP status codes.
- **Service** owns all business logic. Controllers MUST NOT contain business logic;
  services MUST NOT reference HTTP types (`HttpServletRequest`, `ResponseEntity`, etc.).
- **Repository** is the sole point of database access.

Cross-cutting concerns (logging, timing) MUST be handled via AOP, not scattered inline.

### II. DTO-First Communication

All data crossing a public API boundary MUST use dedicated Data Transfer Objects.

- **JPA `@Entity` objects MUST NOT be serialized directly** to or from HTTP responses.
- **Request DTOs**: `CreateXxxRequest` (all fields required for creation, with `@Valid` constraints)
  and `UpdateXxxRequest` (all fields optional, supporting partial updates). Defined in the domain package.
- **Response DTOs**: `XxxResponse extends BaseResponse` (includes `id`, `version`, audit timestamps).
  Defined in the domain package.
- **Wrappers**: `ApiResponse<T>` for successful responses, `ErrorResponse` for errors. Defined in `shared/dto/`.
- Entity ↔ DTO conversion MUST use MapStruct mappers in the domain package; manual field-by-field copying is prohibited.
- Request bodies MUST be validated with Jakarta Bean Validation (`@Valid`) before reaching service methods.
- No `Dto`-suffixed class names on response classes — use `Response` suffix consistently.

### III. JWT Stateless Authentication (NON-NEGOTIABLE)

The API is stateless. `SessionCreationPolicy.STATELESS` is mandatory and MUST NOT be changed.

- Every protected endpoint MUST require a valid JWT Bearer token in the `Authorization` header.
- Token validation passes through `JwtAuthenticationFilter` (`OncePerRequestFilter`) in `shared/security/`.
- Tokens are cross-checked against the `Token` entity (`TokenRepository` in `auth/`) to reject revoked tokens.
- Roles: `ADMIN` (full CRUD on all resources), `CUSTOMER` (read catalog, manage own cart and orders).
- Method-level authorization via `@PreAuthorize` on write endpoints in controller implementations.
- Password storage MUST use `BCryptPasswordEncoder`.

**Whitelisted endpoints** (no JWT required):

| Pattern | Purpose |
|---|---|
| `POST /auth/register` | New account creation |
| `POST /auth/login` | Credential exchange for tokens |
| `POST /auth/refresh-token` | Silent token refresh |
| `GET /products/**` | Public product browsing |
| `GET /categories/**` | Public category browsing |
| `GET /actuator/health` | Health check |
| `/swagger-ui/**`, `/api-docs/**`, `/webjars/**` | API documentation |

### IV. Interface-Driven Design

Every service and controller implementation MUST implement a corresponding interface
in the same domain package:

- `XxxService` (interface) + `XxxServiceImpl` (implementation) — both in `com.app.ecommerce.<domain>/`
- `XxxController` (interface) + `XxxControllerImpl` (implementation) — both in `com.app.ecommerce.<domain>/`

OpenAPI `@Operation` / `@Tag` annotations belong on the **interface**.
`@PreAuthorize` annotations belong on the **implementation**.

This ensures testability and allows alternative implementations without breaking callers.
The `I`-prefix naming (`IXxxService`) is NOT used — this project uses plain name for interface,
`Impl` suffix for the concrete class.

### V. Monetary Precision (NON-NEGOTIABLE)

All monetary values (product price, order total, cart item subtotal) MUST use `BigDecimal`.
`double` and `float` are prohibited for monetary fields. This applies to:

- Entity fields: `BigDecimal` type + `@Column(precision = 10, scale = 2)`
- Request DTOs: `BigDecimal` with `@DecimalMin("0.00")`
- Response DTOs: `BigDecimal`
- Arithmetic: use `BigDecimal` methods (`add`, `multiply`, `setScale`) — no casting to `double`

### VI. Transactional Integrity

All write operations (create, update, delete) MUST be wrapped in database transactions.
All read-only operations SHOULD use `@Transactional(readOnly = true)`.

- Write methods: `@Transactional` on service implementation methods
- Read methods: `@Transactional(readOnly = true)` on service implementation methods
- Optimistic locking: entities that can be concurrently modified (`Product`, `Category`, `Order`, `Cart`)
  MUST have a `@Version Long version` field. Concurrent update conflicts result in HTTP 409 Conflict.

### VII. Observability

- HTTP **request** logging via `CommonsRequestLoggingFilter` in `shared/config/HttpLoggingConfig`.
  Controlled by `logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=DEBUG`.
- The `Authorization` header MUST NOT appear in any log line (excluded via `setHeaderPredicate`).
- Health and metrics exposed via Spring Boot Actuator at `/actuator/health` and `/actuator/metrics`.
- New services MUST NOT use `System.out.println`; use SLF4J via `@Slf4j`.

## Technology Stack

| Concern | Choice | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.0.0 |
| Build | Maven | 3.x |
| ORM | Spring Data JPA + Hibernate | Spring Boot managed |
| Database | MySQL | 8.0.31 |
| Cache | Redis (Lettuce, `spring-boot-starter-data-redis`) | Spring Boot managed |
| Security | Spring Security + JJWT | 0.11.5 |
| DTO Mapping | MapStruct | 1.6.0 |
| API Documentation | SpringDoc OpenAPI (Swagger UI) | 2.0.2 |
| Boilerplate | Lombok | Spring Boot managed |
| Validation | Jakarta Bean Validation | Spring Boot managed |
| Monitoring | Spring Boot Actuator | Spring Boot managed |

Dependency upgrades that change major versions MUST be treated as a MAJOR constitution
amendment and require a migration plan.

## Package & Directory Conventions

The repository follows a **single-module Maven mono-repo** layout. All source lives
under one Maven artifact (`com.app:EcommerceApp`). Packaging is **domain-first**.

```
src/main/java/com/app/ecommerce/
├── auth/
│   ├── User.java              — JPA entity + UserDetails impl
│   ├── Token.java             — JPA entity for JWT token tracking
│   ├── Role.java              — Enum: ADMIN, CUSTOMER
│   ├── UserRepository.java
│   ├── TokenRepository.java
│   ├── AuthService.java       — Interface
│   ├── AuthServiceImpl.java   — Implementation
│   ├── AuthController.java    — Interface (OpenAPI annotations here)
│   ├── AuthControllerImpl.java — Implementation
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── LoginResponse.java
│   └── RefreshTokenRequest.java
│
├── cart/
│   ├── Cart.java, CartItem.java — JPA entities
│   ├── CartRepository.java, CartItemRepository.java
│   ├── CartService.java, CartServiceImpl.java
│   ├── CartMapper.java, CartItemMapper.java
│   └── CartResponse.java, CartItemResponse.java
│
├── category/
│   ├── Category.java           — JPA entity (@Version for optimistic locking)
│   ├── CategoryRepository.java
│   ├── CategorySpecifications.java
│   ├── CategoryService.java, CategoryServiceImpl.java
│   ├── CategoryController.java, CategoryControllerImpl.java
│   ├── CategoryMapper.java
│   ├── CreateCategoryRequest.java, UpdateCategoryRequest.java
│   └── CategoryResponse.java
│
├── order/
│   ├── Order.java, DeliveryInfo.java — JPA entities
│   ├── OrderRepository.java
│   ├── OrderSpecifications.java
│   ├── OrderService.java, OrderServiceImpl.java
│   ├── OrderController.java, OrderControllerImpl.java
│   ├── OrderMapper.java, DeliveryInfoMapper.java
│   ├── CreateOrderRequest.java, UpdateOrderRequest.java
│   └── OrderResponse.java, DeliveryInfoResponse.java
│
├── product/
│   ├── Product.java            — JPA entity (@Version, BigDecimal price)
│   ├── ProductRepository.java
│   ├── ProductSpecifications.java
│   ├── ProductService.java, ProductServiceImpl.java
│   ├── ProductController.java, ProductControllerImpl.java
│   ├── ProductMapper.java
│   ├── CreateProductRequest.java, UpdateProductRequest.java
│   └── ProductResponse.java
│
└── shared/
    ├── config/
    │   ├── ApplicationConfig.java     — PasswordEncoder, AuthenticationManager beans
    │   ├── CacheConfig.java           — Redis cache manager, TTL configuration
    │   ├── HttpLoggingConfig.java     — CommonsRequestLoggingFilter bean
    │   ├── JpaConfig.java             — JPA auditing, AuditorAware bean
    │   ├── OpenApiDocumentationConfig.java — Swagger/OpenAPI bean
    │   ├── RedisCacheErrorHandler.java — Graceful cache fallback
    │   └── SecurityConfig.java        — SecurityFilterChain, whitelist, CORS
    ├── constants/
    │   └── CacheConstants.java        — Cache name constants
    ├── dto/
    │   ├── ApiResponse.java           — Generic success response wrapper
    │   ├── BaseResponse.java          — Base class for all response DTOs
    │   └── ErrorResponse.java         — Error response with field-level details
    ├── entity/
    │   └── BaseEntity.java            — @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy
    ├── enums/
    │   ├── PaymentType.java
    │   └── Status.java                — Order state machine with valid transitions map
    ├── exception/
    │   ├── RestExceptionHandler.java  — @RestControllerAdvice global handler
    │   ├── DuplicatedUniqueColumnValueException.java
    │   └── InvalidStateTransitionException.java
    ├── idempotency/
    │   ├── IdempotencyRecord.java     — JPA entity (key, response JSON, expiresAt)
    │   ├── IdempotencyRepository.java
    │   └── IdempotencyService.java    — Check/store idempotency keys (24h TTL)
    ├── security/
    │   ├── JwtAuthenticationFilter.java — OncePerRequestFilter
    │   ├── JwtService.java             — Token generation/validation
    │   └── SecurityUserDetailsService.java
    └── util/
        └── SortUtils.java             — Sanitize pageable sort against allowed fields

src/main/resources/
└── application.yml  — All configuration (server, datasource, redis, jwt, actuator, cache TTLs)

src/test/java/com/app/ecommerce/
└── EcommerceApplicationTests.java
```

New packages MUST be discussed and documented before introduction; ad-hoc packages
outside this layout are prohibited without a constitution amendment.

## Governance

This constitution supersedes all other conventions, READMEs, and ad-hoc agreements.
When conflicts arise, the constitution wins.

**Amendment Procedure**:
1. Open a PR with the proposed change to `.specify/memory/constitution.md`.
2. State the version bump type (MAJOR / MINOR / PATCH) and rationale.
3. Update all dependent templates identified in the Sync Impact Report header.
4. Obtain review approval before merging.

**Versioning Policy**:
- **MAJOR** (X.0.0): Removal or incompatible redefinition of a principle, or package restructure.
- **MINOR** (x.Y.0): New principle or section; materially expanded guidance.
- **PATCH** (x.y.Z): Clarifications, wording fixes, non-semantic refinements.

**Compliance Review**:
- All PRs MUST include a "Constitution Check" confirming no principles are violated,
  or document a justified exception in the PR body.
- Added complexity beyond what a principle permits MUST be entered in the plan.md
  Complexity Tracking table with rationale.

**Runtime Development Guidance**: Refer to `.specify/templates/agent-file-template.md`
for per-feature agent context generation; keep it in sync with any stack changes.

---

**Version**: 2.0.0 | **Ratified**: 2023-04-17 | **Last Amended**: 2026-03-18
