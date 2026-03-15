# Tasks: App-Wide Redis Caching

**Input**: Design documents from `/specs/002-redis-caching/`
**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅

**Organization**: Tasks grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (US1–US4)

---

## Phase 1: Setup (Dependencies & Configuration)

**Purpose**: Add required dependencies and externalize all cache configuration before any code is written.

- [ ] T001 Add `spring-boot-starter-data-redis` and `spring-boot-starter-cache` dependencies to `pom.xml`
- [ ] T002 Add Redis connection config (`spring.data.redis.host/port`), per-cache TTL properties (`cache.ttl.products=10m`, `cache.ttl.categories=30m`, `cache.ttl.carts=5m`, `cache.ttl.orders=15m`), and `logging.level.org.springframework.cache=DEBUG` to `src/main/resources/application.yml`
- [ ] T003 Verify `compose.yaml` Redis Stack service exposes port `6379` (Redis) and `8001` (RedisInsight); confirm `redis_data` volume is declared

**Checkpoint**: Dependencies declared, all cache properties externalized, Docker Compose ready

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core cache infrastructure and serialization safety fixes that MUST be complete before any `@Cacheable` annotation is added.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Create `src/main/java/com/app/ecommerce/config/CacheConfig.java` with `@Configuration` and `@EnableCaching`; inject TTL duration properties via `@Value`; build `RedisCacheManager` @Bean using `GenericJackson2JsonRedisSerializer` for values, `StringRedisSerializer` for keys, and `withInitialCacheConfigurations(Map)` to apply per-cache TTL from the injected properties; set a 10-minute default TTL for any unlisted cache
- [ ] T005 Add a `CacheErrorHandler` implementation to `CacheConfig.java` by implementing `CachingConfigurer`; override `errorHandler()` to return a handler that logs at WARN and swallows all `RuntimeException`s from `handleCacheGetError`, `handleCachePutError`, `handleCacheEvictError`, and `handleCacheClearError` — ensuring Redis unavailability never propagates a 5xx to the API client
- [ ] T006 [P] Add `@JsonIgnore` annotation (from `com.fasterxml.jackson.annotation`) to the `products` field in `src/main/java/com/app/ecommerce/category/Category.java` to prevent infinite JSON recursion caused by the bidirectional `Product ↔ Category` relationship during cache serialization
- [ ] T007 [P] Read `src/main/java/com/app/ecommerce/cart/CartItem.java`; if the `cart` field does not already have `@JsonIgnore`, add it to prevent circular serialization between `Cart ↔ CartItem`

**Checkpoint**: `CacheConfig` compiles, `@EnableCaching` active, serialization safe — user story work can now begin

---

## Phase 3: User Story 1 — Cache Product and Category Reads (Priority: P1) 🎯 MVP

**Goal**: Repeated reads for the same product (by ID or category) and category (by ID or all) are served from cache without a DB hit.

**Independent Test**: Start the app, call `GET /products/{id}` twice with the same ID. The DEBUG log must show `Cache entry for key '...' found in cache 'products'` on the second call, and no SQL query must appear in the datasource-proxy log for that second request.

- [ ] T008 [P] [US1] Add `@Cacheable(value = "products", key = "#productId")` to `findById(UUID productId)` in `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java`
- [ ] T009 [P] [US1] Add `@Cacheable(value = "categories", key = "#categoryId")` to `findById(UUID categoryId)` in `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java`
- [ ] T010 [P] [US1] Add `@Cacheable(value = "categories", key = "'all'")` to `findAll()` in `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java`

**Checkpoint**: Products and categories are cached on first read and served from cache on repeat reads — MVP complete

---

## Phase 4: User Story 4 — Invalidate Cache on Mutations Across All Domains (Priority: P2)

**Goal**: After any create, update, or delete across product, category, and order domains, the next read returns fresh data from the database (no stale cache).

**Independent Test**: Call `PUT /products/{id}` to update a product's name. Immediately call `GET /products/{id}`. The response MUST return the new name, and the DEBUG log MUST show a cache miss (no "found in cache" line) followed by a SQL SELECT.

- [ ] T011 [P] [US4] Add `@CacheEvict(value = "products", allEntries = true)` to `save(Product)` in `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java`
- [ ] T012 [P] [US4] Add `@CacheEvict(value = "products", allEntries = true)` to `updateById(UUID, Product)` in `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java`
- [ ] T013 [P] [US4] Add `@CacheEvict(value = "products", allEntries = true)` to `deleteById(UUID)` in `src/main/java/com/app/ecommerce/product/ProductServiceImpl.java`
- [ ] T014 [P] [US4] Add `@CacheEvict(value = "categories", allEntries = true)` to `save(Category)` in `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java`
- [ ] T015 [P] [US4] Add `@CacheEvict(value = "categories", allEntries = true)` to `updateById(UUID, Category)` in `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java`
- [ ] T016 [P] [US4] Add `@CacheEvict(value = "categories", allEntries = true)` to `deleteById(UUID)` in `src/main/java/com/app/ecommerce/category/CategoryServiceImpl.java`
- [ ] T017 [P] [US4] Add `@CacheEvict(value = "orders", allEntries = true)` to `createNewOrder(Order)` in `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java`
- [ ] T018 [P] [US4] Add `@CacheEvict(value = "orders", allEntries = true)` to `updateOrder(UUID, Order)` in `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java`

**Checkpoint**: All product, category, and order mutations evict their cache regions — stale reads eliminated

---

## Phase 5: User Story 2 — Cache Cart Reads (Priority: P2)

**Goal**: Repeated reads for the same cart are served from cache without a DB hit.

**Independent Test**: Call `GET /carts/{id}` twice with the same ID. The DEBUG log must show a cache hit on the second call with no SQL query.

- [ ] T019 [US2] Add `@Cacheable(value = "carts", key = "#cartId")` to `findById(UUID cartId)` in `src/main/java/com/app/ecommerce/cart/CartServiceImpl.java`

**Note**: Cart item mutation eviction is a deferred obligation (see `contracts/caching-contract.md`) — to be added when `CartItemServiceImpl` mutation methods are implemented.

**Checkpoint**: Cart reads are cached — per-user DB load reduced during active shopping sessions

---

## Phase 6: User Story 3 — Cache Order History Reads (Priority: P3)

**Goal**: Repeated reads for the same order are served from cache without a DB hit.

**Independent Test**: Call `GET /orders/{id}` twice with the same ID. The DEBUG log must show a cache hit on the second call with no SQL query.

- [ ] T020 [US3] Add `@Cacheable(value = "orders", key = "#orderId")` to `findById(UUID orderId)` in `src/main/java/com/app/ecommerce/order/OrderServiceImpl.java`

**Checkpoint**: Order reads are cached — all four domains fully covered

---

## Phase 7: Polish & Verification

**Purpose**: End-to-end validation of all three spec requirements: cache hits, eviction correctness, and Redis fallback.

- [ ] T021 Run `docker compose up -d`, start the app, call `GET /ecommerce/api/v1/products/{id}` (use a valid ID) twice; confirm DEBUG log shows cache miss then cache hit and that SQL appears only on the first request; open RedisInsight at `http://localhost:8001` and confirm JSON values are visible under `products::*` keys
- [ ] T022 [P] Verify cache fallback: run `docker compose stop redis` while the app is running; call any cached endpoint; confirm the response is HTTP 200 with correct data and that a WARN log line appears mentioning the cache error; run `docker compose start redis` to restore
- [ ] T023 [P] Verify eviction: cache a product by calling `GET /ecommerce/api/v1/products/{id}`; update it via `PUT`; call `GET` again and confirm the updated data is returned and DEBUG log shows cache miss (no "found in cache")

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — BLOCKS all user story phases
- **Phase 3–6 (User Stories)**: All depend on Phase 2 — can proceed in priority order or in parallel
- **Phase 7 (Polish)**: Depends on all user story phases complete

### User Story Dependencies

- **US1 (P1)**: Independent after Phase 2 — no dependency on US2, US3, US4
- **US4 (P2)**: Independent after Phase 2 — eviction does not require reads to be cached first
- **US2 (P2)**: Independent after Phase 2 — no dependency on US1, US4
- **US3 (P3)**: Independent after Phase 2 — note T020 and T017/T018 modify the same file `OrderServiceImpl.java`; complete Phase 4 before Phase 6 to avoid conflicts

### Parallel Opportunities

- T006 and T007 (Phase 2) can run in parallel — different files
- T008–T011 (Phase 3) all target different method annotations — can run in parallel
- T011–T018 (Phase 4) all target different methods (some same file, different methods) — T011–T013 can be batched together (same file), T014–T016 together (same file), T017–T018 together (same file)
- T022 and T023 (Phase 7) are independent verification steps — can run in parallel

---

## Parallel Example: Phase 3 (US1)

```
# All four tasks target different methods — execute together:
T008: @Cacheable on ProductServiceImpl.findById()
T009: @Cacheable on ProductServiceImpl.findAllByCategoryName()
T010: @Cacheable on CategoryServiceImpl.findById()
T011: @Cacheable on CategoryServiceImpl.findAll()
```

## Parallel Example: Phase 4 (US4)

```
# Batch by file:
Batch A (ProductServiceImpl.java): T011, T012, T013
Batch B (CategoryServiceImpl.java): T014, T015, T016
Batch C (OrderServiceImpl.java): T017, T018
# Batches A, B, C can run in parallel — different files
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL — blocks everything)
3. Complete Phase 3: US1 (product + category cache reads)
4. **STOP and VALIDATE**: Two requests to same product ID; confirm cache hit in logs
5. Optionally deploy/demo

### Incremental Delivery

1. Phase 1 + 2 → Infrastructure ready
2. Phase 3 (US1) → Product/Category reads cached ✅ Demo
3. Phase 4 (US4) → Mutations evict stale data ✅ Demo
4. Phase 5 (US2) → Cart reads cached ✅ Demo
5. Phase 6 (US3) → Order reads cached ✅ Demo
6. Phase 7 → Verified end-to-end ✅ Ship

---

## Notes

- Tests not requested in the spec — no test tasks generated
- `[P]` tasks operate on different files or non-conflicting method annotations
- T012–T014 modify the same file (`ProductServiceImpl.java`) — complete sequentially or in one edit
- T014–T016 modify the same file (`CategoryServiceImpl.java`) — same note
- T017–T018 and T020 modify the same file (`OrderServiceImpl.java`) — complete Phase 4 order tasks (T017, T018) before Phase 6 (T020)
- Cart item mutation eviction (`carts` cache) is a **deferred obligation** — future `CartItemServiceImpl` mutation methods MUST add `@CacheEvict(value = "carts", allEntries = true)`
