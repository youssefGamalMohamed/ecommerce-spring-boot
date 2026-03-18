# [PROJECT NAME] Development Guidelines

Auto-generated from all feature plans. Last updated: [DATE]

## Active Technologies

| Concern | Choice | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.0.0 |
| Build | Maven | 3.x |
| ORM | Spring Data JPA + Hibernate | Spring Boot managed |
| Database | MySQL | 8.0.31 |
| Cache | Redis (Lettuce) | Spring Boot managed |
| Security | Spring Security + JJWT | 0.11.5 |
| DTO Mapping | MapStruct | 1.6.0 |
| API Docs | SpringDoc OpenAPI (Swagger UI) | 2.0.2 |
| Boilerplate | Lombok | Spring Boot managed |
| Validation | Jakarta Bean Validation | Spring Boot managed |
| Monitoring | Spring Boot Actuator | Spring Boot managed |

[ADD/REMOVE ROWS AS NEW DEPENDENCIES ARE INTRODUCED]

## Project Structure

```text
src/main/java/com/app/ecommerce/
├── auth/            — User, Token, Role, auth controllers/services
├── cart/            — Cart, CartItem, controllers, services, mappers, responses
├── category/        — Category, controllers, services, mappers, requests/responses
├── order/           — Order, DeliveryInfo, controllers, services, mappers, requests/responses
├── product/         — Product, controllers, services, mappers, requests/responses
└── shared/
    ├── config/      — @Configuration classes (Security, JPA, Cache, HttpLogging, OpenAPI)
    ├── constants/   — Cache key constants
    ├── dto/         — ApiResponse, BaseResponse, ErrorResponse
    ├── entity/      — BaseEntity
    ├── enums/       — PaymentType, Status
    ├── exception/   — RestExceptionHandler + custom exception types
    ├── idempotency/ — IdempotencyRecord, IdempotencyRepository, IdempotencyService
    ├── security/    — JwtAuthenticationFilter, JwtService, SecurityUserDetailsService
    └── util/        — SortUtils

src/main/resources/
└── application.yml

src/test/java/com/app/ecommerce/
└── [domain tests]
```

[EXTEND WITH NEW PACKAGES AS THEY ARE ADDED]

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
- Lombok `@RequiredArgsConstructor` + `@Slf4j` preferred
- MapStruct for all entity ↔ DTO mapping — no manual field copying
- Interface + Impl pattern in same domain package: `XxxService` / `XxxServiceImpl`, `XxxController` / `XxxControllerImpl`
- OpenAPI `@Operation` / `@Tag` on controller **interface**; `@PreAuthorize` on controller **implementation**
- `@Transactional` on write service methods; `@Transactional(readOnly = true)` on reads
- `@Version Long version` on entities subject to concurrent modification
- All monetary values use `BigDecimal` — no `double`/`float` for prices
- `ResponseEntity` builder pattern — no `new ResponseEntity<>()`
- Sort sanitized via `SortUtils.sanitize()` — no inline sort logic in services
- Targeted `@CacheEvict(key = "#id")` — never `allEntries = true`

## Recent Changes

[LAST 3-5 FEATURES AND WHAT THEY ADDED]

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
