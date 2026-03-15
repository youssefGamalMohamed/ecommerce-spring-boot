# Caching Contract: App-Wide Redis Caching

**Feature**: 002-redis-caching
**Date**: 2026-03-15

This document defines the contract between the application and the cache layer — which methods are cached, their cache names, keys, and eviction triggers. Any developer touching a cached service method MUST consult this contract to avoid stale-data bugs.

---

## Contract Table

| Service | Method | Cache name(s) | Operation | Key | allEntries |
|---------|--------|---------------|-----------|-----|-----------|
| `ProductServiceImpl` | `findById(UUID productId)` | `products` | `@Cacheable` | `#productId` | — |
| `ProductServiceImpl` | `save(Product)` | `products` | `@CacheEvict` | — | `true` |
| `ProductServiceImpl` | `updateById(UUID, Product)` | `products` | `@CacheEvict` | — | `true` |
| `ProductServiceImpl` | `deleteById(UUID)` | `products` | `@CacheEvict` | — | `true` |
| `CategoryServiceImpl` | `findById(UUID categoryId)` | `categories` | `@Cacheable` | `#categoryId` | — |
| `CategoryServiceImpl` | `findAll()` | `categories` | `@Cacheable` | `'all'` | — |
| `CategoryServiceImpl` | `save(Category)` | `categories` | `@CacheEvict` | — | `true` |
| `CategoryServiceImpl` | `updateById(UUID, Category)` | `categories` | `@CacheEvict` | — | `true` |
| `CategoryServiceImpl` | `deleteById(UUID)` | `categories` | `@CacheEvict` | — | `true` |
| `CartServiceImpl` | `findById(UUID cartId)` | `carts` | `@Cacheable` | `#cartId` | — |
| `CartItemServiceImpl` | *(future mutation methods)* | `carts` | `@CacheEvict` | — | `true` |
| `OrderServiceImpl` | `findById(UUID orderId)` | `orders` | `@Cacheable` | `#orderId` | — |
| `OrderServiceImpl` | `createNewOrder(Order)` | `orders` | `@CacheEvict` | — | `true` |
| `OrderServiceImpl` | `updateOrder(UUID, Order)` | `orders` | `@CacheEvict` | — | `true` |

---

## Error Handling Contract

| Scenario | Behaviour |
|----------|-----------|
| Redis unavailable on `@Cacheable` GET | Cache miss logged at WARN; method executes normally against DB |
| Redis unavailable on `@Cacheable` PUT | Error logged at WARN; result returned to caller; no cache write |
| Redis unavailable on `@CacheEvict` | Error logged at WARN; eviction skipped; DB write still completes |
| Cache entry expired (TTL) | Next call is a cache miss; method re-populates cache from DB |
| Cache entry evicted manually | Next call is a cache miss; method re-populates cache from DB |

---

## Invariants

1. The database is always the source of truth. Cache is never written to directly — only populated via `@Cacheable` method return values.
2. `allEntries = true` is used on all mutation evictions because a single product mutation can affect multiple cache keys (e.g., multiple category listings).
3. Cart cache eviction from item mutations is a **deferred obligation** — must be added when `CartItemServiceImpl` mutation methods are implemented.
4. Cache annotations live exclusively on `@Service` implementation classes, never on interfaces or `@Repository` classes.
