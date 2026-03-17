# ecommerce-spring-boot Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-03-10

## Active Technologies
- Java 17 + Spring Boot 3.0.0, `spring-boot-starter-data-redis` (new), `spring-boot-starter-cache` (new), Lettuce (transitive), Jackson (already on classpath via `spring-boot-starter-web`) (002-redis-caching)
- MySQL (primary, unchanged) + Redis (cache layer, new) (002-redis-caching)
- Java 17 + Spring Boot 3.0.0, Spring Data JPA (Hibernate), MapStruct 1.6.0, Lombok, SpringDoc OpenAPI 2.0.2 (003-jpa-search-pagination)
- MySQL (primary), Redis (cache layer — existing; no cache changes needed for search) (003-jpa-search-pagination)

- Java 17 + Spring Boot 3.0.0, Spring Security, JJWT 0.11.5, Lombok (001-spring-http-logging)

## Project Structure

```text
src/main/java/com/app/ecommerce/
├── config/          — @Configuration classes (Security, JPA, AOP, HttpLogging, etc.)
├── controller/{framework,impl}/  — REST controllers (interface + impl pattern)
├── dtos/ + models/{request,response}/  — API boundary DTOs
├── entity/          — JPA entities (never serialized to API)
├── service/{framework,impl}/  — Business logic
├── repository/      — Spring Data JPA repos
├── security/{filters,handler}/  — JWT filter, access denied handlers
├── logging/         — (empty after 001-spring-http-logging migration)
├── mq/activemq/     — ActiveMQ senders/listeners
└── exception/       — Global exception handler + custom types
src/main/resources/application.properties
```

## Commands

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Test
mvn test
```

## Code Style

- Java 17 records, switch expressions, text blocks where appropriate
- Lombok `@RequiredArgsConstructor` + `@Slf4j` preferred
- MapStruct for all entity ↔ DTO mapping — no manual field copying
- Every service MUST have an `IXxxService` interface in `service/framework/`
- Every controller MUST have an `IXxxController` interface in `controller/framework/`
- AOP for cross-cutting concerns (no inline logging in business methods)

## Recent Changes
- 003-jpa-search-pagination: Added Java 17 + Spring Boot 3.0.0, Spring Data JPA (Hibernate), MapStruct 1.6.0, Lombok, SpringDoc OpenAPI 2.0.2
- 003-jpa-search-pagination: Added Java 17 + Spring Boot 3.0.0, Spring Data JPA (Hibernate), MapStruct 1.6.0, Lombok, SpringDoc OpenAPI 2.0.2
- 002-redis-caching: Added Java 17 + Spring Boot 3.0.0, `spring-boot-starter-data-redis` (new), `spring-boot-starter-cache` (new), Lettuce (transitive), Jackson (already on classpath via `spring-boot-starter-web`)

  with CommonsRequestLoggingFilter bean only (response status logging out of scope)

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
