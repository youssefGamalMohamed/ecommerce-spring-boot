# Implementation Plan: Cart & Order Lifecycle Flow

**Branch**: `006-cart-order-flow` | **Date**: 2026-03-19 | **Spec**: [spec.md](spec.md)

---

## Summary

Implement the complete cart and order lifecycle for the e-commerce API. The current codebase has Cart/Order entities and an order creation endpoint, but no cart management API exists — customers have no way to create a cart or add items to it, and the order creation endpoint is broken at the DB level (owning side of the Cart↔Order FK is never set).

This plan adds:
1. A `CartStatus` (`OPEN` / `CHECKED_OUT`) field and `User owner` relationship to `Cart`
2. Full cart management REST endpoints (`GET /carts`, `POST /carts/items`, `PATCH /carts/items/{id}`, `DELETE /carts/items/{id}`)
3. Fixed and ownership-secured order creation (derives cart from JWT identity; transitions cart to `CHECKED_OUT`)
4. Role-scoped order listing and single-fetch (customers see only their own orders)

---

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3.0.0, Spring Data JPA + Hibernate, Spring Security + JJWT 0.11.5, MapStruct 1.6.0, Lombok, SpringDoc OpenAPI 2.0.2
**Storage**: MySQL 8.0.31 (primary), Redis (read cache via Lettuce)
**Testing**: Maven + Spring Boot Test (integration context test)
**Target Platform**: Linux server (Docker Compose)
**Project Type**: REST web service (single-module Maven mono-repo)
**Performance Goals**: Standard web API expectations; no new performance targets introduced by this feature
**Constraints**: Stateless JWT (NON-NEGOTIABLE), BigDecimal for all monetary values, `@Version` for optimistic locking on Cart and Order
**Scale/Scope**: Single-user-per-cart constraint enforced at service layer; existing infrastructure scales as-is

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Domain-Based Architecture | ✅ PASS | All new files in `cart/` or `order/` domains. `CartStatus` in `cart/` (not shared). `CartNotOpenException` in `shared/exception/`. Cross-domain `Cart → User` entity import follows existing `CartItem → Product` pattern. |
| II. DTO-First Communication | ✅ PASS | New `AddCartItemRequest`, `UpdateCartItemQuantityRequest`. Updated `CartResponse` (adds `status`). Updated `CreateOrderRequest` (removes `cartId`). Entities never serialized directly. |
| III. JWT Stateless Auth | ✅ PASS | All new endpoints are authenticated. No whitelist additions. `@AuthenticationPrincipal User` resolves current user in controllers. STATELESS session policy unchanged. |
| IV. Interface-Driven Design | ✅ PASS | `CartController` (interface) + `CartControllerImpl` (impl). OpenAPI annotations on interface. `@PreAuthorize` on impl. CartService + CartServiceImpl extended per the pattern. |
| V. Monetary Precision | ✅ PASS | `totalPrice` calculation already uses `BigDecimal.multiply` + `BigDecimal.add`. No float/double introduced. |
| VI. Transactional Integrity | ✅ PASS | All new service write methods annotated `@Transactional`. All new reads annotated `@Transactional(readOnly = true)`. |
| VII. Observability | ✅ PASS | No `System.out.println`. `@Slf4j` on all new service impls. |

**No violations. Complexity Tracking table omitted (not required).**

---

## Project Structure

### Documentation (this feature)

```text
specs/006-cart-order-flow/
├── plan.md              ← This file
├── spec.md              ← Feature specification
├── research.md          ← Phase 0 decisions
├── data-model.md        ← Entity and DTO changes
├── quickstart.md        ← End-to-end developer guide
├── contracts/
│   └── api.md           ← REST endpoint contracts
├── checklists/
│   └── requirements.md  ← Specification quality checklist
└── tasks.md             ← Phase 2 output (via /speckit.tasks)
```

### Source Code Changes

```text
src/main/java/com/app/ecommerce/
│
├── cart/
│   ├── Cart.java                          MODIFIED  — add owner (ManyToOne User), status (CartStatus)
│   ├── CartStatus.java                    NEW       — enum OPEN, CHECKED_OUT
│   ├── CartRepository.java                MODIFIED  — add findByOwnerAndStatus, existsByOwnerAndStatus
│   ├── CartService.java                   MODIFIED  — add getCurrentCart, addItem, updateItemQuantity, removeItem
│   ├── CartServiceImpl.java               MODIFIED  — implement new methods; upsert logic; 0-qty delete
│   ├── CartController.java                NEW       — interface with OpenAPI annotations
│   ├── CartControllerImpl.java            NEW       — @RestController with @AuthenticationPrincipal
│   ├── CartMapper.java                    MODIFIED  — map status field
│   ├── CartResponse.java                  MODIFIED  — add CartStatus status field
│   ├── AddCartItemRequest.java            NEW       — productId (UUID), quantity (@Min(1))
│   └── UpdateCartItemQuantityRequest.java NEW       — quantity (@Min(0))
│
├── order/
│   ├── CreateOrderRequest.java            MODIFIED  — remove cartId field
│   ├── OrderService.java                  MODIFIED  — add User param to all methods
│   ├── OrderServiceImpl.java              MODIFIED  — validate cart OPEN + owner; CHECKED_OUT transition;
│   │                                                   fix FK (cart.setOrder); role-scoped findAll/findById
│   ├── OrderController.java               MODIFIED  — add @AuthenticationPrincipal to method signatures
│   ├── OrderControllerImpl.java           MODIFIED  — pass currentUser to all service calls
│   ├── OrderSpecifications.java           MODIFIED  — add hasOwner(User) spec
│   └── OrderRepository.java              UNCHANGED
│
└── shared/
    └── exception/
        └── CartNotOpenException.java      NEW       — extends RuntimeException; mapped to HTTP 409
```

---

## Implementation Walkthrough

### 1. Cart Entity & Enum Changes

Add to `Cart.java`:
- `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id", nullable = false) private User owner;`
- `@Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private CartStatus status = CartStatus.OPEN;`

New `CartStatus.java`:
```
public enum CartStatus { OPEN, CHECKED_OUT }
```

### 2. CartRepository Queries

```java
Optional<Cart> findByOwnerAndStatus(User owner, CartStatus status);
boolean existsByOwnerAndStatus(User owner, CartStatus status);
```

Spring Data JPA derives these automatically from the field names.

### 3. CartService New Methods

**`getCurrentCart(User owner)`**
- Try `findByOwnerAndStatus(owner, OPEN)`
- If empty: create new `Cart` with `owner = owner`, `status = OPEN`, save, return
- Cache result with `@Cacheable(CARTS_CACHE, key = "#result.id")`

**`addItem(User owner, AddCartItemRequest request)`**
- Load open cart via `getCurrentCart(owner)` (unwrap to entity, not DTO)
- Verify product exists (`productRepository.findById(request.getProductId())`)
- Search `cart.getCartItems()` for existing item with same `product.id`
  - Found: increment `productQuantity += request.getQuantity()`, save item
  - Not found: create new `CartItem`, link to cart, save
- Evict and re-cache the cart
- Return updated `CartResponse`

**`updateItemQuantity(User owner, UUID cartItemId, UpdateCartItemQuantityRequest request)`**
- Load cart item; verify `item.getCart().getOwner().getId().equals(owner.getId())` — else 404
- Verify `item.getCart().getStatus() == OPEN` — else throw `CartNotOpenException`
- If `request.getQuantity() == 0`: delete item, evict cache, return updated cart
- Else: set new quantity, save, evict cache, return updated cart

**`removeItem(User owner, UUID cartItemId)`**
- Same ownership + status checks as above
- Delete item, evict cart cache

### 4. CartController

```
GET  /carts                      → cartService.getCurrentCart(currentUser)  → 200
POST /carts/items                → cartService.addItem(currentUser, request) → 201
PATCH /carts/items/{cartItemId}  → cartService.updateItemQuantity(...)       → 200
DELETE /carts/items/{cartItemId} → cartService.removeItem(...)               → 204
```

`@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")` on all impl methods.

### 5. Fix & Extend OrderServiceImpl

**`createNewOrder(CreateOrderRequest request, User currentUser)`**
1. Get the user's OPEN cart entity: `findByOwnerAndStatus(currentUser, OPEN)` — else 404
2. Validate cart is not empty: `cart.getCartItems().isEmpty()` → 409
3. Validate all products exist (loop through items)
4. Calculate `totalPrice` using `BigDecimal` (existing logic)
5. Build and save Order (existing logic)
6. **Fix**: `cart.setOrder(savedOrder)` — set owning side FK
7. **New**: `cart.setStatus(CHECKED_OUT)` — lock the cart
8. Save cart
9. Return `OrderResponse`

**`findById(UUID orderId, User currentUser)`**
- Fetch order as before
- If `currentUser.getRole() == CUSTOMER` and `order.getCart().getOwner().getId() != currentUser.getId()` → throw `NoSuchElementException` (404)

**`updateOrder(UUID orderId, UpdateOrderRequest request, User currentUser)`**
- Same ownership check as findById

**`findAll(..., User currentUser)`**
- If `CUSTOMER`: add `.and(OrderSpecifications.hasOwner(currentUser))` to the spec chain
- If `ADMIN`: no owner filter

### 6. OrderSpecifications.hasOwner

```java
public static Specification<Order> hasOwner(User user) {
    return (root, query, cb) -> {
        if (user == null) return null;
        // Order -> cart (mappedBy="order") -> owner -> id
        var cart = root.join("cart");
        var owner = cart.join("owner");
        return cb.equal(owner.get("id"), user.getId());
    };
}
```

### 7. CartNotOpenException → RestExceptionHandler

```java
// shared/exception/CartNotOpenException.java
public class CartNotOpenException extends RuntimeException {
    public CartNotOpenException() {
        super("Cart is already checked out and cannot be modified");
    }
}
```

Add handler in `RestExceptionHandler`:
```java
@ExceptionHandler(CartNotOpenException.class)
@ResponseStatus(HttpStatus.CONFLICT)
public ResponseEntity<ErrorResponse> handleCartNotOpen(CartNotOpenException ex) { ... }
```

---

## Cache Strategy

| Cache | Key | Evict on |
|---|---|---|
| `CARTS_CACHE` | cart `id` | addItem, updateItemQuantity, removeItem, checkout |
| `ORDERS_CACHE` | order `id` | createNewOrder (CachePut), updateOrder (CachePut) |

`getCurrentCart` uses `@Cacheable` on the returned cart ID.
All cart mutation methods use `@CacheEvict(key = "#cartId")` then return fresh data.

---

## Known Risks & Notes

1. **Schema migration**: `Cart` table gains two new NOT NULL columns (`owner_id`, `status`). Existing rows will fail if any exist without defaults. If running against a populated DB, a migration script must backfill these columns before deploying.

2. **`Order.cart` bidirectional fix**: The existing `OrderServiceImpl.createNewOrder()` calls `order.setCart(cart)` but never sets `cart.setOrder(order)`. Since Cart owns the FK (`order_id`), the relationship is never persisted. This is a pre-existing bug fixed in this feature.

3. **ProductRepository dependency in CartServiceImpl**: `CartServiceImpl` will need to inject `ProductRepository` to validate that the product exists before adding it to the cart. This is a cross-domain repository import (cart → product), consistent with the existing pattern in `OrderServiceImpl` (order → CartRepository).
