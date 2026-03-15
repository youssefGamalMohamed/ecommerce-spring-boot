# Research: App-Wide Redis Caching

**Feature**: 002-redis-caching
**Date**: 2026-03-15

---

## Decision 1: Where to apply cache annotations

**Decision**: Service layer (`*ServiceImpl` classes)
**Rationale**: Constitution Principle I mandates that business logic (including caching policy) lives in the service layer. Controllers MUST NOT contain business logic; repositories are only for DB access. Annotating service methods with `@Cacheable`/`@CacheEvict` keeps the cache as a transparent read-through layer above the repository.
**Alternatives considered**:
- Controller layer — violates Principle I; also caches HTTP concerns instead of domain objects
- Repository layer — Spring Data repos cannot hold `@Cacheable` reliably with proxies; bypasses service logic on cache misses

---

## Decision 2: Serialization format

**Decision**: `GenericJackson2JsonRedisSerializer` (Jackson, embeds `@class` type metadata)
**Rationale**: Satisfies FR-013 (human-readable JSON in cache store). Works across all domain types without per-type configuration. Jackson is already on the classpath via `spring-boot-starter-web`.
**Alternatives considered**:
- `JdkSerializationRedisSerializer` (default) — binary, not human-readable; fails FR-013
- `Jackson2JsonRedisSerializer<T>` — type-specific; requires separate bean per cached type; verbose

**Required pre-conditions**:
- `Category.products` field MUST have `@JsonIgnore` added. Without it, the `Product ↔ Category` bidirectional relationship causes infinite recursion during serialization. (`Category.products` is the `mappedBy` non-owning side — safe to ignore.)
- `CartItem.cart` — verify `@JsonIgnore` is present (common pattern already used for `Cart.order`).

---

## Decision 3: Per-cache-name TTL configuration

**Decision**: Build `RedisCacheManager` manually using `withInitialCacheConfigurations(Map<String, RedisCacheConfiguration>)` with a default TTL for all other caches.
**Rationale**: Satisfies FR-011 and FR-012. Each cache name gets an independent `RedisCacheConfiguration` with its own TTL, all driven from `application.yml` properties.
**TTL defaults** (all configurable):

| Cache name            | Default TTL | Reasoning                                     |
|-----------------------|-------------|-----------------------------------------------|
| `products`            | 10 min      | Moderate change frequency                     |
| `categories`          | 30 min      | Very stable data                              |
| `carts`               | 5 min       | Active shopping sessions; must stay fresh     |
| `orders`              | 15 min      | Immutable after placement; moderate staleness acceptable |

---

## Decision 4: Cache fallback on Redis unavailability

**Decision**: Implement a custom `CacheErrorHandler` that logs the error at WARN level and swallows it, allowing the request to fall through to the database.
**Rationale**: Satisfies FR-010 and SC-003. Spring Cache by default propagates cache exceptions to the caller, which would return 5xx errors when Redis is down. A custom `CacheErrorHandler` intercepts `get`, `put`, `evict`, and `clear` errors silently.
**Alternatives considered**:
- Let default exceptions propagate — fails FR-010 and SC-003
- Wrap every service method in try-catch — too invasive, violates single-responsibility

---

## Decision 5: Cache logging at DEBUG level

**Decision**: Add `logging.level.org.springframework.cache=DEBUG` to `application.yml`.
**Rationale**: Satisfies FR-014. Spring Cache's built-in logging emits cache hit/miss/evict events at DEBUG, which are suppressed by default (INFO). This is consistent with how the project already enables DEBUG for `CommonsRequestLoggingFilter` and SQL logging.
**No code change required** — configuration only.

---

## Decision 6: New configuration class

**Decision**: Add `CacheConfig.java` to `com.app.ecommerce.config`.
**Rationale**: Follows existing convention — all `@Configuration` classes live in `config/`. No new package is needed. The class will declare `@EnableCaching`, build the `RedisCacheManager`, and register the custom `CacheErrorHandler`.

---

## Decision 7: New Maven dependencies

Two dependencies must be added to `pom.xml`:

| Dependency | Reason |
|------------|--------|
| `spring-boot-starter-data-redis` | Brings in Lettuce client and `RedisTemplate` / `RedisCacheManager` |
| `spring-boot-starter-cache` | Enables Spring Cache abstraction (`@EnableCaching`, `@Cacheable`, etc.) |

Both are managed by Spring Boot 3.0.0 BOM — no explicit version needed.

---

## Decision 8: Cache key strategy

| Cache name             | Key expression          | Notes |
|------------------------|-------------------------|-------|
| `products`             | `#productId`            | UUID string |
| `categories`           | `#categoryId` / `'all'` | `findAll()` uses literal key `'all'` |
| `carts`                | `#cartId`               | UUID string |
| `orders`               | `#orderId`              | UUID string |

For eviction on mutations, `allEntries = true` is used because a single product/category mutation can affect multiple cache entries (e.g., all category listings).

---

## Decision 9: Cart item mutation eviction

**Finding**: `CartItemServiceImpl` currently only implements `findById`. There are no add/remove/update cart item operations in the service layer yet.
**Decision**: Add `@CacheEvict(value = "carts", allEntries = true)` to cart item mutation methods **when they are implemented**. Document this dependency explicitly in the caching contract so future developers know to add the annotation.
The `findById` in `CartServiceImpl` will be cached. Eviction from cart mutations is a deferred obligation.
