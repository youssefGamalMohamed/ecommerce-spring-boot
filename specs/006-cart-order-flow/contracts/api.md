# API Contracts: Cart & Order Lifecycle Flow

**Branch**: `006-cart-order-flow` | **Phase**: 1 | **Date**: 2026-03-19

All endpoints require a valid JWT Bearer token unless noted otherwise.
All responses are wrapped in `ApiResponse<T>`.
Errors are wrapped in `ErrorResponse`.

---

## Cart Endpoints (NEW)

### GET /carts

Retrieve the authenticated customer's current open cart. If no open cart exists, one is created automatically and returned.

**Auth**: CUSTOMER or ADMIN
**Roles**: Any authenticated user

**Response 200**:
```json
{
  "message": "Success",
  "data": {
    "id": "uuid",
    "version": 0,
    "status": "OPEN",
    "cartItems": [
      {
        "id": "uuid",
        "productQuantity": 2,
        "product": {
          "id": "uuid",
          "name": "Product Name",
          "price": 29.99
        }
      }
    ],
    "createdAt": "2026-03-19T10:00:00Z",
    "updatedAt": "2026-03-19T10:00:00Z"
  }
}
```

---

### POST /carts/items

Add a product to the authenticated customer's current open cart. If the product already exists in the cart, its quantity is incremented by the given amount.

**Auth**: CUSTOMER or ADMIN

**Request Body**:
```json
{
  "productId": "uuid",
  "quantity": 2
}
```

| Field | Required | Constraint |
|---|---|---|
| `productId` | Yes | Must reference an existing product |
| `quantity` | Yes | `>= 1` |

**Response 201**: Updated `CartResponse` (same shape as GET /carts)

**Error 404**: Product not found
**Error 409**: Cart is `CHECKED_OUT`

---

### PATCH /carts/items/{cartItemId}

Update the quantity of a specific cart item. Sending `quantity = 0` automatically removes the item from the cart.

**Auth**: CUSTOMER or ADMIN

**Path parameter**: `cartItemId` (UUID)

**Request Body**:
```json
{
  "quantity": 3
}
```

| Field | Required | Constraint |
|---|---|---|
| `quantity` | Yes | `>= 0` (0 = remove item) |

**Response 200**: Updated `CartResponse`

**Error 404**: Cart item not found, or item does not belong to the authenticated user's cart
**Error 409**: Cart is `CHECKED_OUT`

---

### DELETE /carts/items/{cartItemId}

Explicitly remove a cart item.

**Auth**: CUSTOMER or ADMIN

**Path parameter**: `cartItemId` (UUID)

**Response 204**: No content

**Error 404**: Cart item not found, or item does not belong to the authenticated user's cart
**Error 409**: Cart is `CHECKED_OUT`

---

## Order Endpoints (MODIFIED)

### POST /orders

Create a new order from the authenticated customer's current open cart. The cart is automatically found — no `cartId` is needed.

**Auth**: CUSTOMER or ADMIN
**Idempotency**: Supported via optional `Idempotency-Key` header

**Request Body**:
```json
{
  "paymentType": "CREDIT_CARD"
}
```

| Field | Required | Constraint |
|---|---|---|
| `paymentType` | Yes | Must be a valid `PaymentType` value |

> **Breaking change from previous contract**: `cartId` field has been removed.

**Response 201**: `OrderResponse`

```json
{
  "message": "Order created successfully",
  "data": {
    "id": "uuid",
    "totalPrice": 59.98,
    "paymentType": "CREDIT_CARD",
    "version": 0,
    "deliveryInfo": {
      "status": "NOT_MOVED_OUT_FROM_WAREHOUSE",
      "address": null,
      "date": null
    },
    "cart": {
      "id": "uuid",
      "status": "CHECKED_OUT",
      "cartItems": [...]
    }
  }
}
```

**Error 404**: No open cart found for the authenticated user
**Error 409**: Cart is already `CHECKED_OUT` or cart is empty (zero items)
**Error 409**: Referenced products no longer exist

---

### GET /orders/{id}

Retrieve a single order by ID.

**Auth**: CUSTOMER or ADMIN

**Ownership rule**:
- `CUSTOMER`: only their own orders are visible; returns 404 if the order belongs to another user
- `ADMIN`: any order is accessible

**Response 200**: `OrderResponse`

**Error 404**: Order not found (or belongs to another customer)

---

### GET /orders

List orders with optional filters and pagination.

**Auth**: CUSTOMER or ADMIN

**Ownership rule**:
- `CUSTOMER`: automatically scoped to their own orders only
- `ADMIN`: returns all orders across all customers

**Query parameters** (all optional):

| Parameter | Type | Description |
|---|---|---|
| `status` | `Status` | Filter by delivery status |
| `paymentType` | `PaymentType` | Filter by payment method |
| `createdAfter` | `Instant` | Filter orders created after this timestamp |
| `createdBefore` | `Instant` | Filter orders created before this timestamp |
| `page` | `int` | Page number (default 0) |
| `size` | `int` | Page size (default 20) |
| `sort` | `string` | Sort field: `totalPrice` or `createdAt` (default: `createdAt,desc`) |

**Response 200**: `Page<OrderResponse>`

---

### PATCH /orders/{id}

Update delivery information for an order. Admin-only for status transitions; customers may not update orders.

**Auth**: ADMIN only (via `@PreAuthorize`)

**Request Body** (all fields optional):
```json
{
  "deliveryStatus": "ON_THE_WAY_TO_CUSTOMER",
  "deliveryAddress": "123 Main St",
  "deliveryDate": "2026-04-01",
  "version": 0
}
```

| Field | Required | Notes |
|---|---|---|
| `version` | Yes | For optimistic locking |
| `deliveryStatus` | No | Must be a valid state transition |
| `deliveryAddress` | No | |
| `deliveryDate` | No | |

**Response 200**: Updated `OrderResponse`

**Error 404**: Order not found
**Error 409**: Invalid delivery status transition or optimistic locking conflict

---

## Error Response Shape

```json
{
  "status": 409,
  "message": "Cart is already checked out and cannot be modified",
  "errors": []
}
```

## HTTP Status Summary

| Scenario | Status |
|---|---|
| Resource not found (or ownership violation) | 404 |
| Cart not OPEN (mutation/checkout on CHECKED_OUT cart) | 409 |
| Empty cart at checkout | 409 |
| Invalid state transition | 409 |
| Optimistic lock conflict | 409 |
| Validation error (missing/invalid fields) | 400 |
