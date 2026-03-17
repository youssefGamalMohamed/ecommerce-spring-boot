# API Contract: Orders

**Base Path**: `/ecommerce/api/v1/orders`

---

## POST /orders (Create Order)

**Auth**: Required (ADMIN or CUSTOMER)

**Headers**:
- `Idempotency-Key` (optional but recommended): UUID string. If provided, duplicate requests with the same key return the original response without creating a new order. Keys expire after 24 hours.

**Request Body** (`CreateOrderRequest`):
```json
{
  "paymentType": "VISA",
  "cartId": "uuid-of-existing-cart"
}
```

**Validation**:
- `paymentType`: required, must be one of: CASH, VISA
- `cartId`: required, must reference an existing cart

**Response** (201 Created): `OrderResponse`

**Idempotency behavior**:
- First request: processes normally, caches response with idempotency key
- Subsequent requests (same key within 24h): returns cached response, no new order created
- After 24h: key expires, treated as new request

---

## GET /orders (List/Search Orders)

**Auth**: Required (ADMIN sees all, CUSTOMER sees own orders only)

**Query Parameters**:
- `status` (optional): enum filter (NOT_MOVED_OUT_FROM_WAREHOUSE, ON_THE_WAY_TO_CUSTOMER, DELIVERED, CANCELED)
- `paymentType` (optional): enum filter (CASH, VISA)
- `createdAfter` (optional): ISO-8601 instant
- `createdBefore` (optional): ISO-8601 instant
- `page`, `size`, `sort`: standard pagination

**Validation**: `createdAfter` <= `createdBefore` when both provided

**Response** (200 OK): Paginated `OrderResponse`

---

## GET /orders/{id}

**Auth**: Required (ADMIN or order owner)

**Response** (200 OK): Single `OrderResponse`
**Error** (404): Not found | **Error** (403): Not owner

---

## PATCH /orders/{id} (Update Order)

**Auth**: Required (ADMIN only)

**Request Body** (`UpdateOrderRequest`):
```json
{
  "deliveryStatus": "ON_THE_WAY_TO_CUSTOMER",
  "deliveryAddress": "123 Main St",
  "deliveryDate": "2026-04-01",
  "version": 0
}
```

**State machine enforcement**: `deliveryStatus` transitions are validated. Invalid transitions return 400 with allowed transitions.

**Validation**: `version` required. `deliveryDate` must be ISO-8601 date format.

**Response** (200 OK): Updated `OrderResponse`
**Error** (400): Invalid state transition | **Error** (404): Not found | **Error** (409): Version conflict

---

## Error Response Format (all endpoints)

```json
{
  "success": false,
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "details": "name: must not be blank; price: must be greater than or equal to 0.00",
  "path": "/ecommerce/api/v1/products",
  "timestamp": 1710758400000
}
```

For state transition errors:
```json
{
  "success": false,
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Invalid status transition",
  "details": "Cannot transition from CANCELED to DELIVERED. Allowed transitions from CANCELED: (none — terminal state)",
  "path": "/ecommerce/api/v1/orders/uuid",
  "timestamp": 1710758400000
}
```

For optimistic lock conflicts:
```json
{
  "success": false,
  "status": 409,
  "error": "CONFLICT",
  "message": "Resource was modified by another user",
  "details": "Please refresh the resource and try again with the updated version",
  "path": "/ecommerce/api/v1/products/uuid",
  "timestamp": 1710758400000
}
```
