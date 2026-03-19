# Research: Cart & Order Lifecycle Flow

**Branch**: `006-cart-order-flow` | **Phase**: 0 | **Date**: 2026-03-19

All findings are derived from direct codebase analysis — no external unknowns remain.

---

## Decision 1: Cart Ownership Storage

**Decision**: Add a `@ManyToOne(fetch = LAZY) User owner` field to the `Cart` entity with a DB column `owner_id`.

**Rationale**:
- User confirmed they want a proper JPA relationship, not a string field or audit column.
- `CartItem` already uses the same cross-domain entity reference pattern (`CartItem → Product`), so `Cart → User` is consistent.
- The constitution permits cross-domain entity imports (evidenced by `CartItem.product`); it only forbids cross-domain *service* calls.
- Enables `CartRepository.findByOwnerAndStatus(User, CartStatus)` — a clean, index-backed query.

**Alternatives considered**:
- `createdBy` from `BaseEntity` auditing — rejected: couples ownership to an audit concern; harder to index and query; loses explicit domain meaning.
- `ownerUsername` String column — rejected: string duplication of the User PK; breaks if username changes.

---

## Decision 2: Current User Resolution

**Decision**: Inject `@AuthenticationPrincipal User currentUser` as a controller method parameter, then pass the `User` object to service methods.

**Rationale**:
- `User` implements `UserDetails` and is what `JwtAuthenticationFilter` places in the `SecurityContext`.
- `@AuthenticationPrincipal` resolves the principal with zero boilerplate — no manual `SecurityContextHolder.getContext()` calls.
- Services receive a `User` parameter (not a string), keeping them testable without a `SecurityContext` mock.

**Alternatives considered**:
- `SecurityContextHolder.getContext().getAuthentication()` inside service — rejected: tightly couples service to Spring Security context; harder to unit test.
- Extract only `username` string and do a `UserRepository.findByUsername()` lookup in the service — rejected: unnecessary extra DB query; the principal object is already available.

---

## Decision 3: CartStatus Enum Placement

**Decision**: `CartStatus` enum (`OPEN`, `CHECKED_OUT`) lives in `com.app.ecommerce.cart` — not in `shared/enums/`.

**Rationale**:
- `shared/enums/` holds cross-domain enums (`PaymentType`, `Status`). `CartStatus` is exclusively cart-scoped.
- Keeping it in `cart/` follows the constitution's domain-based packaging principle.

---

## Decision 4: Remove `cartId` from `CreateOrderRequest`

**Decision**: Remove the `cartId` field from `CreateOrderRequest`. The service looks up the authenticated user's `OPEN` cart automatically.

**Rationale**:
- A user always has exactly one `OPEN` cart — there is no ambiguity.
- Client does not need to track the cart UUID to place an order; the JWT token is sufficient.
- Reduces surface area for misuse (e.g., submitting another user's cartId).

**Breaking change**: `cartId` was a required field. This is a breaking API change — the existing `POST /orders` contract changes. Acceptable because the feature has not yet been fully implemented (no real clients depend on it in its current partial state).

---

## Decision 5: Cart Item Add — Upsert Strategy

**Decision**: Adding a product that already exists in the cart increments the existing `CartItem.productQuantity` rather than creating a duplicate.

**Rationale**: User confirmed in spec clarification. Avoids duplicate cart items for the same product. Simplifies client logic (no need to check for existing items before adding).

**Implementation**: `CartRepository.findByOwnerAndStatus()` gives the open cart; then search `cart.getCartItems()` for an item with matching `product.id`. If found, increment and save; if not, create new.

---

## Decision 6: Quantity = 0 Means Delete

**Decision**: In `PATCH /carts/items/{cartItemId}` with `quantity = 0`, the item is automatically deleted.

**Rationale**: User confirmed in spec clarification. Reduces API calls for the client. Prevents "zombie" cart items with zero quantity.

---

## Decision 7: Order List Scoping by Role

**Decision**: `GET /orders` returns only the authenticated user's orders for CUSTOMER role; returns all orders for ADMIN role. Same single endpoint, different WHERE clause based on role.

**Implementation**: `OrderServiceImpl.findAll()` accepts a `User currentUser` parameter. If `currentUser.getRole() == Role.ADMIN`, no owner filter is applied. If `Role.CUSTOMER`, `OrderSpecifications.hasOwner(currentUser)` is added to the specification chain.

**New spec method**: `OrderSpecifications.hasOwner(User user)` — joins `Order → Cart (mappedBy="order") → owner` and filters by `owner.id = user.id`.

---

## Decision 8: Order Ownership Enforcement on Single Fetch and Update

**Decision**: `findById` and `updateOrder` in `OrderServiceImpl` verify that `order.getCart().getOwner().getId().equals(currentUser.getId())` when the requester has `CUSTOMER` role. On mismatch, throw `NoSuchElementException` (not-found) — do not reveal existence.

**Rationale**: User confirmed in clarification. Returning 404 (not-found) on ownership violations avoids leaking whether an order ID exists.

---

## Decision 9: Cart Status Transition on Checkout

**Decision**: In `OrderServiceImpl.createNewOrder()`, after persisting the Order:
1. Set `cart.setOrder(savedOrder)` — fulfills the `@JoinColumn(name = "order_id")` FK on Cart (Cart is the owning side).
2. Set `cart.setStatus(CHECKED_OUT)`.
3. Save the cart.

**Rationale**: `Order.cart` is `mappedBy = "order"` — Cart holds the FK. The current implementation calls `order.setCart(cart)` but doesn't set the owning side (`cart.setOrder(order)`), which means the FK is never actually written to the DB. This bug is fixed as part of this feature.

---

## Decision 10: New Exception for Invalid Cart State

**Decision**: Add `CartNotOpenException` (extends `RuntimeException`) to `shared/exception/`. The `RestExceptionHandler` maps it to HTTP 409 Conflict.

**Rationale**: Attempting to mutate or checkout a `CHECKED_OUT` cart is a business logic violation, not a missing resource (404). A dedicated exception gives the handler a clear mapping and the client a meaningful error code.
