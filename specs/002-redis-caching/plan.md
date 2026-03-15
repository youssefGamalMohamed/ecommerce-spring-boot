# Implementation Plan: App-Wide Redis Caching

**Branch**: `002-redis-caching` | **Date**: 2026-03-15 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-redis-caching/spec.md`

## Summary

Add a transparent Redis cache layer across all four domains (product, category, cart, order) using Spring Cache annotations on service implementation classes. Values are stored as human-readable JSON. Each cache name has an independently configurable TTL. A custom error handler ensures Redis unavailability falls back to the database without surfacing errors to API clients.

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3.0.0, `spring-boot-starter-data-redis` (new), `spring-boot-starter-cache` (new), Lettuce (transitive), Jackson (already on classpath via `spring-boot-starter-web`)
**Storage**: MySQL (primary, unchanged) + Redis (cache layer, new)
**Testing**: `spring-boot-starter-test` (existing)
**Target Platform**: Linux server (Docker Compose for local dev)
**Project Type**: REST web service (single-module Maven mono-repo)
**Performance Goals**: Repeated reads ≥5× faster than uncached DB reads (SC-001)
**Constraints**: Zero 5xx errors when Redis is unavailable (SC-003); stale reads not permitted after first post-mutation fetch (SC-002)
**Scale/Scope**: All four existing domains — product, category, cart, order

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Layered Architecture | ✅ PASS | Cache annotations on `*ServiceImpl` only; no business logic in controllers or repos |
| II. DTO-First Communication | ✅ PASS | Caching happens at service layer on domain entities; API boundary via DTOs unchanged |
| III. JWT Stateless Authentication | ✅ PASS | No session state introduced; Redis used for caching only, not session |
| IV. Interface-Driven Design | ✅ PASS | Annotations on impl classes; interfaces unchanged |
| V. Async Messaging | ✅ PASS | No side effects introduced; not applicable to this feature |
| VI. Observability | ✅ PASS | `logging.level.org.springframework.cache=DEBUG` added; no `System.out.println`; `@Slf4j` used for error handler |

**Constitution verdict**: No violations. No complexity tracking entry required.

## Project Structure

### Documentation (this feature)

```text
specs/002-redis-caching/
├── plan.md               # This file
├── spec.md               # Feature specification
├── research.md           # Phase 0 — research decisions
├── data-model.md         # Phase 1 — cache key schema and TTLs
├── quickstart.md         # Phase 1 — local dev setup
└── contracts/
    └── caching-contract.md  # Phase 1 — cache method contract table
```

### Source Code changes (repository root)

```text
pom.xml
  └── + spring-boot-starter-data-redis
  └── + spring-boot-starter-cache

src/main/java/com/app/ecommerce/
├── config/
│   └── CacheConfig.java          (NEW) — @EnableCaching, RedisCacheManager, CacheErrorHandler
├── category/
│   ├── Category.java              (MODIFY) — add @JsonIgnore on products field
│   ├── CategoryServiceImpl.java   (MODIFY) — add @Cacheable / @CacheEvict annotations
├── product/
│   └── ProductServiceImpl.java    (MODIFY) — add @Cacheable / @CacheEvict annotations
├── cart/
│   ├── CartItem.java              (VERIFY) — ensure @JsonIgnore on cart field
│   └── CartServiceImpl.java       (MODIFY) — add @Cacheable on findById
└── order/
    └── OrderServiceImpl.java      (MODIFY) — add @Cacheable / @CacheEvict annotations

src/main/resources/
└── application.yml                (MODIFY) — add Redis config, cache TTLs, DEBUG log level

compose.yaml                       (ALREADY DONE) — Redis Stack service added
```

**Structure Decision**: Single-module, no new packages. All config in existing `config/`. Cache annotations co-located with each domain's service implementation.

## Phase 0: Research

All unknowns resolved. See [research.md](research.md) for full decisions.

**Key findings**:
- Service layer is the correct annotation target (Principle I)
- `GenericJackson2JsonRedisSerializer` for JSON serialization (FR-013)
- `Category.products` requires `@JsonIgnore` to prevent circular serialization
- `CacheErrorHandler` provides transparent DB fallback when Redis is down (FR-010)
- Per-cache TTL via `RedisCacheManager.withInitialCacheConfigurations()` (FR-011)
- Two new Maven dependencies: `spring-boot-starter-data-redis`, `spring-boot-starter-cache`
- Cart item eviction is a deferred obligation pending mutation method implementation

## Phase 1: Design Artifacts

- [data-model.md](data-model.md) — cache key schema, value types, TTLs, eviction rules
- [contracts/caching-contract.md](contracts/caching-contract.md) — per-method cache annotation contract
- [quickstart.md](quickstart.md) — local setup and verification steps

## Implementation Phases (for /speckit.tasks)

### Phase A: Infrastructure

1. Add `spring-boot-starter-data-redis` and `spring-boot-starter-cache` to `pom.xml`
2. Add Redis connection config and per-cache TTL properties to `application.yml`
3. Add `logging.level.org.springframework.cache=DEBUG` to `application.yml`
4. Create `CacheConfig.java` in `config/`:
   - `@EnableCaching`
   - `RedisCacheManager` bean with `GenericJackson2JsonRedisSerializer`, per-cache TTL map, default TTL
   - `CacheErrorHandler` bean that logs WARN and swallows Redis errors

### Phase B: Serialization fixes

5. Add `@JsonIgnore` to `Category.products` field
6. Verify `@JsonIgnore` on `CartItem.cart` field (add if missing)

### Phase C: Product domain caching

7. `ProductServiceImpl.findById()` → `@Cacheable(value = "products", key = "#productId")`
8. `ProductServiceImpl.save()` → `@CacheEvict(value = "products", allEntries = true)`
9. `ProductServiceImpl.updateById()` → `@CacheEvict(value = "products", allEntries = true)`
10. `ProductServiceImpl.deleteById()` → `@CacheEvict(value = "products", allEntries = true)`

### Phase D: Category domain caching

12. `CategoryServiceImpl.findById()` → `@Cacheable(value = "categories", key = "#categoryId")`
13. `CategoryServiceImpl.findAll()` → `@Cacheable(value = "categories", key = "'all'")`
14. `CategoryServiceImpl.save()` → `@CacheEvict(value = "categories", allEntries = true)`
15. `CategoryServiceImpl.updateById()` → `@CacheEvict(value = "categories", allEntries = true)`
16. `CategoryServiceImpl.deleteById()` → `@CacheEvict(value = "categories", allEntries = true)`

### Phase E: Cart domain caching

17. `CartServiceImpl.findById()` → `@Cacheable(value = "carts", key = "#cartId")`

### Phase F: Order domain caching

18. `OrderServiceImpl.findById()` → `@Cacheable(value = "orders", key = "#orderId")`
19. `OrderServiceImpl.createNewOrder()` → `@CacheEvict(value = "orders", allEntries = true)`
20. `OrderServiceImpl.updateOrder()` → `@CacheEvict(value = "orders", allEntries = true)`

### Phase G: Verification

21. Start `docker compose up -d`, run app, verify cache hit/miss logs appear in console
22. Verify JSON values visible in RedisInsight at `localhost:8001`
23. Verify fallback: stop Redis, confirm endpoints return 200 with DB data
24. Verify eviction: update a product, confirm next fetch returns updated data
