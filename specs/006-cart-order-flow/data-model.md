# Data Model: Cart & Order Lifecycle Flow

**Branch**: `006-cart-order-flow` | **Phase**: 1 | **Date**: 2026-03-19

---

## New: `CartStatus` Enum

**File**: `cart/CartStatus.java`

| Value | Meaning |
|---|---|
| `OPEN` | Cart is mutable; items can be added, updated, or removed |
| `CHECKED_OUT` | Cart is locked; an Order has been created from it |

---

## Modified Entity: `Cart`

**File**: `cart/Cart.java`

| Field | Type | Change | Notes |
|---|---|---|---|
| `id` | `UUID` | — | Unchanged |
| `version` | `Long` | — | `@Version` — unchanged |
| `owner` | `User` | **NEW** | `@ManyToOne(fetch = LAZY)` `@JoinColumn(name = "owner_id", nullable = false)` — FK to `users` table |
| `status` | `CartStatus` | **NEW** | `@Enumerated(EnumType.STRING)` `@Builder.Default = OPEN` |
| `cartItems` | `Set<CartItem>` | — | Unchanged |
| `order` | `Order` | — | `@OneToOne @JoinColumn(name = "order_id")` — unchanged |

**New unique constraint**: `(owner_id, status)` where `status = 'OPEN'` — at most one OPEN cart per user. Enforced at service level (not DB partial index, which MySQL 8 supports but adds complexity). Service guards creation using `findByOwnerAndStatus`.

**DB impact**: New column `owner_id` (NOT NULL, FK → `users.id`) and `status` (VARCHAR NOT NULL). Schema migration required.

---

## Unchanged Entity: `CartItem`

No field changes. Already has `product`, `productQuantity`, `cart`.

---

## Unchanged Entity: `Order`

No field changes. Owner is derivable via `order.getCart().getOwner()`.

---

## Modified Repository: `CartRepository`

**File**: `cart/CartRepository.java`

```
// New derived query methods:
Optional<Cart> findByOwnerAndStatus(User owner, CartStatus status)
boolean existsByOwnerAndStatus(User owner, CartStatus status)
```

---

## Modified Repository: `OrderRepository`

No changes — `JpaSpecificationExecutor` already supports dynamic owner filtering.

---

## Modified Specifications: `OrderSpecifications`

**File**: `order/OrderSpecifications.java`

New static method:

```
hasOwner(User user)
  → JOIN order → cart (via mappedBy "order") → owner
  → WHERE cart.owner.id = user.id
  → Returns null predicate if user is null (no filter)
```

---

## New / Modified DTOs

### New: `AddCartItemRequest`

**File**: `cart/AddCartItemRequest.java`

| Field | Type | Constraint |
|---|---|---|
| `productId` | `UUID` | `@NotNull` |
| `quantity` | `int` | `@Min(1)` |

### New: `UpdateCartItemQuantityRequest`

**File**: `cart/UpdateCartItemQuantityRequest.java`

| Field | Type | Constraint | Notes |
|---|---|---|---|
| `quantity` | `int` | `@Min(0)` | 0 = auto-delete the item |

### Modified: `CartResponse`

**File**: `cart/CartResponse.java`

Add field: `CartStatus status`

### Modified: `CreateOrderRequest`

**File**: `order/CreateOrderRequest.java`

Remove field: `cartId` — cart is derived from the authenticated user's OPEN cart.

Remaining fields:
| Field | Type | Constraint |
|---|---|---|
| `paymentType` | `PaymentType` | `@NotNull` |

---

## New Exception: `CartNotOpenException`

**File**: `shared/exception/CartNotOpenException.java`

Thrown when a mutation or checkout is attempted on a `CHECKED_OUT` cart.
Mapped in `RestExceptionHandler` → HTTP 409 Conflict.

---

## Service Interface Changes

### `CartService`

New methods added to the interface:

| Method | Returns | Description |
|---|---|---|
| `getCurrentCart(User)` | `CartResponse` | Get or auto-create the OPEN cart for the user |
| `addItem(User, AddCartItemRequest)` | `CartResponse` | Add item (or increment quantity if product exists) |
| `updateItemQuantity(User, UUID cartItemId, UpdateCartItemQuantityRequest)` | `CartResponse` | Set absolute quantity; 0 = remove |
| `removeItem(User, UUID cartItemId)` | `void` | Explicitly remove a cart item |

### `OrderService`

Signatures updated to accept `User currentUser`:

| Method | Change |
|---|---|
| `createNewOrder(CreateOrderRequest, User)` | Cart derived from user's OPEN cart |
| `findById(UUID, User)` | Ownership check for CUSTOMER role |
| `updateOrder(UUID, UpdateOrderRequest, User)` | Ownership check for CUSTOMER role |
| `findAll(Status, PaymentType, Instant, Instant, Pageable, User)` | Scoped by owner for CUSTOMER role |

---

## State Transitions

### Cart Status

```
[OPEN] ──── checkout ──→ [CHECKED_OUT]  (terminal)
```

Mutations (add/update/remove item) are only allowed in `OPEN` state.

### Order Delivery Status (unchanged)

```
NOT_MOVED_OUT_FROM_WAREHOUSE → ON_THE_WAY_TO_CUSTOMER → DELIVERED (terminal)
                             ↘                        ↘
                              CANCELED (terminal)      CANCELED (terminal)
```
