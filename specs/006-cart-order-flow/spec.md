# Feature Specification: Cart & Order Lifecycle Flow

**Feature Branch**: `006-cart-order-flow`
**Created**: 2026-03-19
**Status**: Draft

## Context: How Real-World E-Commerce Cart & Order Works

Before the requirements, here is a brief explanation of how leading e-commerce platforms handle the cart and order flow, which this feature implements:

### Real-World Cart Lifecycle

1. **Cart Creation**: When a logged-in customer visits the store, a cart is created and persisted in the database (not just Redis). Redis is used as a *cache layer on top* of the database, not as the primary store. This ensures the cart survives browser restarts, device switches, and cache evictions.

2. **One Active Cart Per User**: A customer has at most one active (open) cart at any time. When a customer places an order, the cart transitions to a "checked out" state and is locked — a new cart is created for future shopping.

3. **Adding/Removing Items**: Customers add products (with quantities) to their cart. Each line item references a product and a quantity. The price shown in the cart is pulled from the product catalog in real time — the price is *not* frozen at add-to-cart time but is confirmed (snapshotted) at checkout.

4. **Cart Validity**: Before checkout, the system validates the cart: it checks that all products are still available and active. Stock/inventory checks are outside the scope of this feature (no inventory system exists), so availability is limited to checking that the product record still exists.

5. **Checkout (Create Order)**: The customer submits their cart as an order. At this moment, the `totalPrice` is calculated from current product prices, and the cart is locked (no more item changes). The `Order` takes ownership of the `Cart`.

6. **Cart Status**: A cart is either `OPEN` (mutable, not yet ordered) or `CHECKED_OUT` (locked, attached to an order). This is the primary validity signal.

---

## User Scenarios & Testing

### User Story 1 - Manage My Shopping Cart (Priority: P1)

A logged-in customer browses the catalog and builds their cart: they create a cart (if one does not already exist), add products with desired quantities, update quantities, and remove items. At any time they can view their current cart.

**Why this priority**: Without cart management, no order can ever be placed. This is the foundational flow.

**Independent Test**: Can be fully tested by creating a cart, adding two products, updating the quantity of one, removing the other, and verifying the cart state via GET — without needing to place an order.

**Acceptance Scenarios**:

1. **Given** a logged-in customer with no existing open cart, **When** they request their current cart, **Then** a new empty cart is created and returned.
2. **Given** a customer with an open cart, **When** they add a product with quantity 2, **Then** a cart item appears in the cart with `productQuantity = 2`.
3. **Given** a cart item exists, **When** the customer updates its quantity to 5, **Then** the cart item reflects `productQuantity = 5`.
4. **Given** a cart item exists, **When** the customer removes it, **Then** the cart no longer contains that item.
5. **Given** a customer already has an open cart, **When** they request their current cart again, **Then** the same existing cart is returned (no duplicate created).
6. **Given** a cart item references a product that no longer exists, **When** the customer views the cart, **Then** a warning is included indicating the item is no longer available.

---

### User Story 2 - Place an Order From Cart (Priority: P1)

A logged-in customer with items in their open cart places an order. The system validates the cart, calculates the total, creates the order, and locks the cart.

**Why this priority**: The entire shopping experience leads to this moment; without it the feature has no business value.

**Independent Test**: Can be fully tested by creating a cart, adding items, then submitting an order with the cart ID and verifying the order is created with the correct total and the cart is locked.

**Acceptance Scenarios**:

1. **Given** an open cart with items, **When** the customer submits an order with a valid payment type, **Then** an order is created with `totalPrice` equal to the sum of all `product.price × productQuantity`, and the cart status becomes `CHECKED_OUT`.
2. **Given** a cart that is already `CHECKED_OUT`, **When** the customer attempts to place an order using that cart, **Then** the request is rejected with an appropriate error.
3. **Given** an empty cart (no items), **When** the customer attempts to place an order, **Then** the request is rejected — orders require at least one item.
4. **Given** an order is successfully placed, **When** the customer requests their current cart, **Then** a fresh empty `OPEN` cart is returned.

---

### User Story 3 - View & Update Order Status (Priority: P2)

An admin updates the delivery status of a placed order following the defined state machine. A customer can view their own order details and list all their past orders. The `GET /orders` endpoint is role-scoped: customers receive only their own orders; admins receive all orders.

**Why this priority**: Core post-purchase operations, but dependent on a placed order existing.

**Independent Test**: Can be tested by placing an order then updating delivery status as admin through each valid transition.

**Acceptance Scenarios**:

1. **Given** an order with status `NOT_MOVED_OUT_FROM_WAREHOUSE`, **When** an admin updates it to `ON_THE_WAY_TO_CUSTOMER`, **Then** the order reflects the new status.
2. **Given** an order with status `DELIVERED`, **When** an admin attempts to change status to `CANCELED`, **Then** the request is rejected as an invalid transition.
3. **Given** an order ID, **When** a customer requests it, **Then** the full order including cart items and delivery info is returned.
4. **Given** a customer with past orders, **When** they call `GET /orders`, **Then** only their own orders are returned (no other customers' orders are visible).
5. **Given** an admin calls `GET /orders`, **When** the request is processed, **Then** all orders across all customers are returned.

---

### Edge Cases

- What happens when a customer adds the same product twice? The existing cart item quantity is incremented rather than creating a duplicate entry.
- What happens when a customer updates a cart item quantity to 0? The item is automatically removed from the cart — no explicit DELETE call is required.
- What happens when a customer attempts to access or modify another customer's cart or order? The request returns a not-found response (ownership violations are not surfaced as "forbidden" to avoid confirming existence).
- What happens when the cart has items but a referenced product has been deleted? The order creation is rejected; the customer must remove the invalid item before checkout.
- What happens if two concurrent requests try to modify the same cart? The optimistic locking mechanism rejects the second write with a conflict error.
- What happens if no items are in the cart at checkout? The system rejects the order with a clear error message.

---

## Requirements

### Functional Requirements

- **FR-001**: System MUST expose a single endpoint for retrieving the current customer's open cart; the customer identity MUST be derived entirely from the authenticated token — no user identifier is included in the URL path. If no `OPEN` cart exists for that customer, one MUST be created automatically and returned.
- **FR-002**: System MUST associate each cart and order with the customer who owns them; customers MUST NOT be able to access or modify another customer's cart or order — ownership is enforced on all read and write operations.
- **FR-003**: System MUST allow a customer to add a product (by product ID) with a quantity to their open cart; adding the same product again MUST increment the existing item's quantity rather than creating a duplicate entry.
- **FR-004**: System MUST allow a customer to update the quantity of an existing cart item in their open cart using an absolute value (the desired final quantity); if the new quantity is `0`, the item MUST be automatically removed from the cart.
- **FR-005**: System MUST allow a customer to remove a specific item from their open cart.
- **FR-006**: A cart MUST have a status of either `OPEN` or `CHECKED_OUT`; only `OPEN` carts may be mutated (items added, updated, or removed).
- **FR-007**: System MUST reject order creation when the cart is `CHECKED_OUT` or contains zero items.
- **FR-008**: System MUST validate that all cart items reference existing products before an order is placed; orders referencing deleted products MUST be rejected.
- **FR-009**: System MUST calculate `totalPrice` at order-creation time using current product prices; no price is frozen at add-to-cart time.
- **FR-010**: Upon successful order creation, the cart status MUST transition to `CHECKED_OUT` and MUST reject any further item mutations.
- **FR-011**: After a successful checkout, the customer's "current cart" MUST return a fresh empty `OPEN` cart (auto-created on next request).
- **FR-012**: System MUST support idempotent order creation via an `Idempotency-Key` header to prevent duplicate orders on network retries.
- **FR-013**: Cart and CartItem data MUST be persisted in the primary database; an in-memory cache may be layered on top for read performance but MUST NOT be the only store.

### Key Entities

- **Cart**: Represents a customer's shopping session. Has an `owner` relationship to the `User` entity (many carts can belong to one user over time; at most one is `OPEN`), a `status` (`OPEN` or `CHECKED_OUT`), and a collection of `CartItem`s.
- **CartItem**: A line item inside a Cart. References a `Product` and holds a `productQuantity`. Price is not stored on the item — it is read from the current product record at order time.
- **Order**: Created from a `CHECKED_OUT` cart. Holds a snapshotted `totalPrice`, `paymentType`, and embedded `DeliveryInfo`. Linked one-to-one with its originating Cart.
- **DeliveryInfo** (embedded in Order): Tracks delivery `status` (governed by a state machine), `address`, and expected `date`.

---

## Success Criteria

### Measurable Outcomes

- **SC-001**: A customer can complete the full flow — from an empty cart to a placed order — without any manual data setup or back-channel steps.
- **SC-002**: A cart and its items survive a cache eviction and are still fully retrievable from persistent storage.
- **SC-003**: Attempting to checkout a cart that is already `CHECKED_OUT` always returns a clear rejection; no duplicate orders are created under any circumstance.
- **SC-004**: Order total always equals the sum of each cart item's current product price multiplied by its quantity at the moment the order is placed.
- **SC-005**: A customer's cart is never accessible to or modifiable by a different customer.
- **SC-006**: Concurrent writes to the same cart from two requests are handled safely — one request succeeds and the other receives a conflict error (no silent data loss).

---

## Clarifications

### Session 2026-03-19

- Q: Should the "get current cart" endpoint include a `/me` path segment or derive identity from the token? → A: No `/me` in the URL — customer identity is always derived from the authenticated JWT token. The endpoint is simply `GET /carts`.
- Q: How should Cart ownership be stored — audit field, username string, or entity relationship? → A: A direct JPA relationship from `Cart` to `User` (the existing auth domain entity); ownership is a proper foreign key, not a string field or audit column.
- Q: When a customer updates a cart item's quantity to 0, should it be auto-removed or return a validation error? → A: Treat `quantity = 0` as an implicit delete — the item is removed from the cart automatically.
- Q: Should customers be able to list their own orders via `GET /orders`, or is listing admin-only? → A: Role-scoped — customers see only their own orders; admins see all orders. Same endpoint, different scope based on JWT role.
- Q: Can a customer fetch any order by ID, or only their own? → A: Customers can only fetch their own orders by ID; fetching another customer's order returns not-found (ownership enforced on both list and single-fetch).
- Q: Should cart item quantity updates use absolute values or deltas? → A: Absolute — the client sends the desired final quantity; the system sets it directly (idempotent, safe under retries).

---

## Assumptions

- **No inventory system**: Product availability is determined solely by whether the product record exists. Out-of-stock or limited-quantity scenarios are out of scope.
- **Price not frozen at add-to-cart**: Prices may change between when an item is added and when the order is placed. The order total reflects prices at checkout time.
- **One open cart per user**: A customer may only have one `OPEN` cart at a time. The system creates one automatically on demand and after each successful checkout.
- **Authentication already implemented**: The customer identity is derived from the authenticated JWT token. The existing auth/security infrastructure is used as-is.
- **No guest carts**: Carts require an authenticated user. Guest/anonymous cart support is out of scope.
- **No discount or coupon codes**: Pricing is purely `sum(product.price × quantity)` with no promotions or adjustments.
- **Cart ownership via User relation**: Cart holds a direct JPA foreign-key relationship to the `User` entity (from the existing auth domain). Ownership queries ("find the OPEN cart for this user") use this relationship, not audit fields.
