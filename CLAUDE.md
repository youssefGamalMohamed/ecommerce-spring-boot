# ecommerce-spring-boot Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-04-06

## Active Technologies
- Java 17 (unchanged — Spring Boot 4.0 minimum is Java 17) + Spring Boot 4.0.5, Spring Framework 7.x (managed), Spring Security 7.x (managed) (007-spring-boot-upgrade)
- MySQL 8 (server), MySQL Connector/J 9.6.0 (driver), Redis (Lettuce, Spring Boot managed) (007-spring-boot-upgrade)

| Concern | Choice | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 4.0.5 |
| Build | Maven | 3.x |
| ORM | Spring Data JPA + Hibernate | Spring Boot managed |
| Database | MySQL | 8.0.31 |
| Cache | Redis (Lettuce) | Spring Boot managed |
| Security | Spring Security + JJWT | 0.12.6 |
| DTO Mapping | MapStruct | 1.6.3 |
| API Docs | SpringDoc OpenAPI (Swagger UI) | 3.0.2 |
| Boilerplate | Lombok | 1.18.44 |
| Validation | Jakarta Bean Validation | Spring Boot managed |
| Monitoring | Spring Boot Actuator | Spring Boot managed |

## Project Structure

```text
src/main/java/com/app/ecommerce/
├── auth/            — User, Token, Role, auth controllers/services (flat domain package)
├── cart/            — Cart, CartItem, controllers, services, mappers, responses
├── category/        — Category, controllers, services, mappers, requests/responses
├── order/           — Order, DeliveryInfo, controllers, services, mappers, requests/responses
├── product/         — Product, controllers, services, mappers, requests/responses
└── shared/
    ├── config/      — @Configuration classes (Security, JPA, Cache, HttpLogging, OpenAPI)
    ├── constants/   — CacheConstants
    ├── models/      — ApiResponse, BaseResponse, ErrorResponse
    ├── entity/      — BaseEntity (auditing base)
    ├── enums/       — PaymentType, Status (with state machine transitions)
    ├── exception/   — RestExceptionHandler, DuplicatedUniqueColumnValueException,
    │                  InvalidStateTransitionException
    ├── idempotency/ — IdempotencyRecord, IdempotencyRepository, IdempotencyService
    ├── security/    — JwtAuthenticationFilter, JwtService, SecurityUserDetailsService
    └── util/        — SortUtils

src/main/resources/
└── application.yml  — All environment configuration

src/test/java/com/app/ecommerce/
└── EcommerceApplicationTests.java
```

## Commands

```bash
# Build (skip tests)
mvn clean install -DskipTests

# Run
mvn spring-boot:run

# Test
mvn test

# Compile only
mvn clean compile
```

## Code Style

- Java 17 records, switch expressions, text blocks where appropriate
- Lombok `@RequiredArgsConstructor` + `@Slf4j` preferred; `@Builder` on entities
- MapStruct for all entity ↔ DTO mapping — no manual field copying
- Every service MUST have an interface (`XxxService`) and implementation (`XxxServiceImpl`) in the same domain package
- Every controller MUST have an interface (`XxxController`) and implementation (`XxxControllerImpl`) in the same domain package
- OpenAPI `@Operation` / `@Tag` annotations go on the controller **interface**
- `@PreAuthorize` on admin-only methods in the controller **implementation**
- `@Transactional` on all service write methods; `@Transactional(readOnly = true)` on queries
- `@Version` field on entities that can be concurrently modified (optimistic locking)
- All monetary values use `BigDecimal` — no `double` or `float` for prices/totals
- AOP for cross-cutting concerns (no inline logging in business methods)
- `ResponseEntity` builder pattern (`ResponseEntity.ok(...)`, `ResponseEntity.status(...).body(...)`) — no `new ResponseEntity<>()`
- Sort parameters sanitized via `SortUtils.sanitize()` — no inline sort logic in services

## Key Patterns

### Request / Response DTOs
- Create requests: `CreateXxxRequest` (only writable fields + `@Valid` annotations)
- Update requests: `UpdateXxxRequest` (all fields optional for partial update)
- Responses: `XxxResponse extends BaseResponse` (includes id, version, audit timestamps)
- Wrappers: `ApiResponse<T>` (success wrapper), `ErrorResponse` (error wrapper)

### Caching
- Products: `PRODUCTS_CACHE` key = product id (TTL 10 min)
- Categories: `CATEGORIES_CACHE` key = category id (TTL 30 min)
- Carts: `CARTS_CACHE` key = cart id (TTL 5 min)
- Orders: `ORDERS_CACHE` key = order id (TTL 15 min)
- Use targeted `@CacheEvict(key = "#id")` — never `allEntries = true`
- Cache errors handled gracefully by `RedisCacheErrorHandler` (fallback to DB)

### Security
- Stateless JWT — `SessionCreationPolicy.STATELESS` (NON-NEGOTIABLE)
- Whitelisted: `POST /auth/**`, `GET /products/**`, `GET /categories/**`, `/actuator/health`, Swagger paths
- Roles: `ADMIN` (full CRUD), `CUSTOMER` (read catalog, own cart/orders)

### Order State Machine
Valid transitions:
- `NOT_MOVED_OUT_FROM_WAREHOUSE` → `ON_THE_WAY_TO_CUSTOMER`, `CANCELED`
- `ON_THE_WAY_TO_CUSTOMER` → `DELIVERED`, `CANCELED`
- `DELIVERED` → (terminal)
- `CANCELED` → (terminal)

## Recent Changes
- 007-spring-boot-upgrade: Added Java 17 (unchanged — Spring Boot 4.0 minimum is Java 17) + Spring Boot 4.0.5, Spring Framework 7.x (managed), Spring Security 7.x (managed)

- 005-architecture-refactor: Full architecture overhaul — domain-based packages, BigDecimal prices,
  @Version optimistic locking, @Transactional boundaries, JWT auth + RBAC, separate request/response
  DTOs with validation, targeted cache eviction, order state machine, idempotency for orders,
  Spring Boot Actuator health/metrics, SortUtils DRY utility
- 004-extend-search-pagination: Extended JPA Specification search across product, category, order domains

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
