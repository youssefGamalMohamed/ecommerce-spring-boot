# Data Model: App-Wide Redis Caching

**Feature**: 002-redis-caching
**Date**: 2026-03-15

No new JPA entities are introduced by this feature. The cache stores serialized forms of existing domain objects. This document defines the cache key schema, value types, TTLs, and eviction rules.

---

## Cache Regions

### `products`

| Attribute    | Value |
|--------------|-------|
| Value type   | `Product` (serialized as JSON) |
| Key pattern  | `products::<productId>` (UUID string) |
| TTL          | 10 minutes (configurable via `cache.ttl.products`) |
| Populated by | `ProductServiceImpl.findById(UUID)` |
| Evicted by   | `ProductServiceImpl.save()`, `updateById()`, `deleteById()` — all entries |

---

### `categories`

| Attribute    | Value |
|--------------|-------|
| Value type   | `Category` or `List<Category>` (serialized as JSON) |
| Key pattern  | `categories::<categoryId>` (UUID) or `categories::all` |
| TTL          | 30 minutes (configurable via `cache.ttl.categories`) |
| Populated by | `CategoryServiceImpl.findById(UUID)`, `findAll()` |
| Evicted by   | `CategoryServiceImpl.save()`, `updateById()`, `deleteById()` — all entries |

---

### `carts`

| Attribute    | Value |
|--------------|-------|
| Value type   | `Cart` (serialized as JSON) |
| Key pattern  | `carts::<cartId>` (UUID string) |
| TTL          | 5 minutes (configurable via `cache.ttl.carts`) |
| Populated by | `CartServiceImpl.findById(UUID)` |
| Evicted by   | Cart item mutation methods — **deferred** (not yet implemented; see research Decision 9) |

---

### `orders`

| Attribute    | Value |
|--------------|-------|
| Value type   | `Order` (serialized as JSON) |
| Key pattern  | `orders::<orderId>` (UUID string) |
| TTL          | 15 minutes (configurable via `cache.ttl.orders`) |
| Populated by | `OrderServiceImpl.findById(UUID)` |
| Evicted by   | `OrderServiceImpl.createNewOrder()`, `updateOrder()` — all entries |

---

## Serialization Pre-conditions

| Entity | Field | Required change | Reason |
|--------|-------|-----------------|--------|
| `Category` | `products` | Add `@JsonIgnore` | Bidirectional `Product ↔ Category` causes infinite JSON recursion |
| `CartItem` | `cart` | Verify `@JsonIgnore` present | Bidirectional `Cart ↔ CartItem` would cause recursion |

---

## Configuration Properties (application.yml)

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    type: redis

cache:
  ttl:
    products: 10m
    categories: 30m
    carts: 5m
    orders: 15m

logging:
  level:
    org:
      springframework:
        cache: DEBUG
```
