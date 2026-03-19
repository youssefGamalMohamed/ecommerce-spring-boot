# Quickstart: Cart & Order Lifecycle Flow

**Branch**: `006-cart-order-flow` | **Date**: 2026-03-19

A developer guide for testing the full cart → order flow end-to-end.

---

## Prerequisites

- Application running locally (`mvn spring-boot:run`)
- MySQL and Redis running (via `docker compose up`)
- At least one product exists in the database (create via `POST /products` as ADMIN)

---

## Step 1: Register and Login as a Customer

```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testcustomer", "email": "test@example.com", "password": "password123"}'

# Login — save the accessToken
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testcustomer", "password": "password123"}'
```

Save the `accessToken` from the login response. Use it as `TOKEN` in all subsequent requests.

---

## Step 2: Get (or Create) Current Cart

```bash
curl -X GET http://localhost:8080/carts \
  -H "Authorization: Bearer $TOKEN"
```

Expected: A new empty cart with `status: "OPEN"` and empty `cartItems`.

---

## Step 3: Add Items to Cart

```bash
# Add product (replace PRODUCT_ID with a real product UUID)
curl -X POST http://localhost:8080/carts/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": "PRODUCT_ID", "quantity": 2}'
```

Expected: Cart response with one item, `productQuantity: 2`.

```bash
# Add the same product again — quantity should increment to 3
curl -X POST http://localhost:8080/carts/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": "PRODUCT_ID", "quantity": 1}'
```

Expected: Same item, `productQuantity: 3`.

---

## Step 4: Update Item Quantity

```bash
# Replace CART_ITEM_ID with the id from the cartItems array
curl -X PATCH http://localhost:8080/carts/items/CART_ITEM_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 5}'
```

Expected: Item updated to `productQuantity: 5`.

```bash
# Set quantity to 0 — item should be automatically removed
curl -X PATCH http://localhost:8080/carts/items/CART_ITEM_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 0}'
```

Expected: Cart returned with the item removed from `cartItems`.

---

## Step 5: Place an Order

```bash
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"paymentType": "CREDIT_CARD"}'
```

Expected:
- HTTP 201
- Order created with `totalPrice = sum(product.price × quantity)`
- Cart in response has `status: "CHECKED_OUT"`

---

## Step 6: Verify Cart Auto-Reset

```bash
curl -X GET http://localhost:8080/carts \
  -H "Authorization: Bearer $TOKEN"
```

Expected: A fresh empty cart with `status: "OPEN"` — different UUID from the previous cart.

---

## Step 7: Verify Order Ownership

```bash
# View your own order
curl -X GET http://localhost:8080/orders/ORDER_ID \
  -H "Authorization: Bearer $TOKEN"

# Try to use a checked-out cart again
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"paymentType": "CREDIT_CARD"}'
```

Expected for second request: HTTP 409 — cart is empty (new cart has no items) or appropriate error.

---

## Step 8: Admin — Update Delivery Status

```bash
# Login as admin first to get ADMIN_TOKEN
curl -X PATCH http://localhost:8080/orders/ORDER_ID \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"deliveryStatus": "ON_THE_WAY_TO_CUSTOMER"}'
```

Expected: Order updated with new delivery status.

---

## Common Error Scenarios to Test

| Scenario | Expected |
|---|---|
| POST /carts/items with invalid productId | 404 Not Found |
| PATCH /carts/items on a CHECKED_OUT cart | 409 Conflict |
| POST /orders with empty cart | 409 Conflict |
| GET /orders/{id} for another user's order | 404 Not Found |
| PATCH /orders/{id} with invalid state transition | 409 Conflict |
