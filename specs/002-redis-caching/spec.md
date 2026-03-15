# Feature Specification: Add App-Wide Redis Caching

**Feature Branch**: `002-redis-caching`
**Created**: 2026-03-15
**Status**: Draft
**Input**: User description: "i want the caching to be applied for entire application not only for product"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cache Product and Category Reads (Priority: P1)

A client browsing the catalog repeatedly fetches products by ID, by category, and the full category list. All of these reads should be served from cache after the first database hit to reduce load and improve response speed.

**Why this priority**: Products and categories are the most frequently read data in an ecommerce app. This is the highest-impact caching target.

**Independent Test**: Can be fully tested by fetching the same product/category twice and confirming the second response does not trigger a database query.

**Acceptance Scenarios**:

1. **Given** a product exists, **When** a client requests it by ID for the first time, **Then** the result is stored in cache.
2. **Given** the product is cached, **When** a client requests the same product ID again, **Then** the response is served from cache without a database hit.
3. **Given** categories exist, **When** a client requests all categories, **Then** the result is cached.

---

### User Story 2 - Cache Cart Reads (Priority: P2)

A logged-in user's cart is fetched on every page load and checkout flow. Cart reads should be served from cache so that navigating the site feels instant without repeated database lookups.

**Why this priority**: Cart is the second most frequently read resource per authenticated session; caching it directly reduces per-user DB load during active shopping.

**Independent Test**: Can be tested by fetching the same user's cart twice and confirming the second call does not hit the database.

**Acceptance Scenarios**:

1. **Given** a user has an active cart, **When** they request their cart, **Then** the result is cached keyed to that user.
2. **Given** the cart is cached, **When** the user requests it again without modification, **Then** it is served from cache.

---

### User Story 3 - Cache Order History Reads (Priority: P3)

A user viewing their order history triggers the same query repeatedly. Order history reads should be cached so repeat views are fast.

**Why this priority**: Order history is read-heavy and rarely mutates after an order is placed; it is a straightforward caching win.

**Independent Test**: Can be tested by fetching the same user's orders twice and confirming the second response is served from cache.

**Acceptance Scenarios**:

1. **Given** a user has past orders, **When** they request their order list, **Then** the result is cached keyed to that user.
2. **Given** a new order is placed, **Then** the cached order history for that user is evicted so the next read reflects the new order.

---

### User Story 4 - Invalidate Cache on Mutations Across All Domains (Priority: P2)

Whenever any resource is created, updated, or deleted — in any domain — the relevant cache entries must be evicted immediately so clients never see stale data.

**Why this priority**: Correctness across all cached domains is a hard requirement; stale data in any domain degrades user trust.

**Independent Test**: Can be tested domain by domain: mutate a resource, then immediately fetch it and confirm the fresh data is returned.

**Acceptance Scenarios**:

1. **Given** a product is cached, **When** it is updated or deleted, **Then** its cache entries are evicted.
2. **Given** a cart is cached, **When** an item is added, removed, or quantity changed, **Then** the cart cache for that user is evicted.
4. **Given** a category is cached, **When** it is created, updated, or deleted, **Then** its cache entries are evicted.

---

### Edge Cases

- What happens when the cache store is unavailable? The system must fall through to the database transparently without returning an error to the client.
- What happens when a cached entry's TTL expires naturally? The next request should repopulate it from the database.
- What happens when a user's session ends? Their user-scoped cache entries (cart, orders) should be evicted or left to expire via TTL.

## Requirements *(mandatory)*

### Functional Requirements

**Product domain**

- **FR-001**: The system MUST cache individual product responses keyed by product ID.
- **FR-002**: The system MUST evict product cache entries when a product is created, updated, or deleted.

**Category domain**

- **FR-004**: The system MUST cache the full category listing and individual category responses.
- **FR-005**: The system MUST evict category cache entries when a category is created, updated, or deleted.

**Cart domain**

- **FR-006**: The system MUST cache cart responses keyed by user.
- **FR-007**: The system MUST evict a user's cart cache entry when any item in their cart is added, updated, or removed.

**Order domain**

- **FR-008**: The system MUST cache order history responses keyed by user.
- **FR-009**: The system MUST evict a user's order history cache when a new order is placed by that user.

**Cross-cutting**

- **FR-010**: The system MUST fall back to the database transparently if the cache store is unavailable, without returning an error to the client.
- **FR-011**: Each cache name (products, categories, carts, orders) MUST have its own independently configured TTL so expiry can be tuned per domain without affecting others.
- **FR-012**: Cache configuration (per-cache-name TTL, connection details) MUST be externalized and changeable without a code rebuild.
- **FR-012**: All values stored in the cache MUST be serialized as human-readable JSON so cache contents can be inspected and debugged directly in the cache store.
- **FR-013**: All cache operations (cache hit, cache miss, eviction) MUST be logged at DEBUG level so they are visible during development and troubleshooting but silent in production.
- **FR-014**: The local development environment MUST include a Redis service using the Redis Stack image (latest) so developers can use the built-in cache inspection UI without additional tooling.

### Key Entities

- **Product Cache Entry**: Keyed by product ID; evicted on product mutation; expires after configured TTL.
- **Category Cache Entry**: Keyed by category ID or "all"; evicted on any category mutation.
- **Cart Cache Entry**: Keyed by user identity; evicted on any cart item change.
- **Order History Cache Entry**: Keyed by user identity; evicted when the user places a new order.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Repeated reads for the same resource (product, category, cart, order history) return responses at least 5× faster than the initial uncached database-backed read.
- **SC-002**: After any resource is mutated, the next read for that resource reflects the updated data within one request (no stale reads after the first post-mutation fetch).
- **SC-003**: When the cache store is unavailable, 100% of read and write operations succeed by falling back to the database with no 5xx errors returned to clients.
- **SC-004**: TTL and cache connection settings for each domain can be changed via configuration without a code change or rebuild.
- **SC-005**: All domains (product, category, cart, order) have at least one read operation that benefits from caching, verifiable by absence of database queries on repeated identical requests.

## Assumptions

- All cacheable endpoints are already functional and tested; this feature only adds a caching layer on top.
- The cache is used for read acceleration only — the database remains the source of truth for all writes.
- Default TTL per cache name: products = 10 min, categories = 30 min, carts = 5 min, orders = 15 min; all independently configurable.
- Cart and order caches are scoped per user to prevent cross-user data leakage.
- Caching covers all domains: product, category, cart, and order.
