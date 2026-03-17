# Quickstart: Extend Search and Pagination to Remaining Entities

**Branch**: `004-extend-search-pagination` | **Date**: 2026-03-17

## Prerequisites

- Application running on `http://localhost:8080`
- At least a few categories and orders seeded in the database

---

## Category Endpoint Verification

### 1. Basic paginated listing

```bash
curl -s "http://localhost:8080/categories?page=0&size=5" | jq '{total: .data.totalElements, pages: .data.totalPages, count: (.data.content | length)}'
```

**Expect**: `total` >= 0, `pages` >= 1, `count` <= 5.

### 2. Default pagination (no params)

```bash
curl -s "http://localhost:8080/categories" | jq '.data.size, .data.number'
```

**Expect**: `20` (default size), `0` (default page).

### 3. Name filter — partial, case-insensitive

```bash
curl -s "http://localhost:8080/categories?name=elec" | jq '.data.content[].name'
```

**Expect**: Only category names containing "elec" (case-insensitive).

### 4. Sort by createdAt descending

```bash
curl -s "http://localhost:8080/categories?sort=createdAt,desc&size=3" | jq '.data.content[].name'
```

**Expect**: 3 most recently created categories in descending order.

### 5. Invalid sort field falls back gracefully

```bash
curl -s "http://localhost:8080/categories?sort=badField,asc" | jq '.success, .status'
```

**Expect**: `true`, `200` — no error; default sort (`name,asc`) applied.

### 6. Empty filter returns all (paginated)

```bash
curl -s "http://localhost:8080/categories?name=" | jq '.data.totalElements'
```

**Expect**: Same `totalElements` as calling `/categories` with no params — blank name treated as omitted.

---

## Order Endpoint Verification

> Replace `<TOKEN>` with a valid JWT Bearer token.

### 7. Basic paginated order listing

```bash
curl -s -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/orders?page=0&size=5" | \
  jq '{total: .data.totalElements, pages: .data.totalPages, count: (.data.content | length)}'
```

**Expect**: `total` >= 0, `pages` >= 1, `count` <= 5.

### 8. Filter by delivery status

```bash
curl -s -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/orders?status=DELIVERED" | \
  jq '.data.content[].deliveryInfo.status'
```

**Expect**: All returned orders have `status = "DELIVERED"`.

### 9. Filter by payment type

```bash
curl -s -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/orders?paymentType=CASH" | \
  jq '.data.content[].paymentType'
```

**Expect**: All returned orders have `paymentType = "CASH"`.

### 10. Date range filter

```bash
curl -s -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/orders?createdAfter=2026-01-01T00:00:00Z&createdBefore=2026-03-31T23:59:59Z" | \
  jq '.data.totalElements'
```

**Expect**: Count of orders created in Q1 2026.

### 11. Inverted date range returns HTTP 400

```bash
curl -s -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/orders?createdAfter=2026-12-01T00:00:00Z&createdBefore=2026-01-01T00:00:00Z" | \
  jq '.success, .status'
```

**Expect**: `false`, `400`.

### 12. Invalid enum value returns HTTP 400

```bash
curl -s -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/orders?status=INVALID_STATUS" | \
  jq '.success, .status'
```

**Expect**: `false`, `400`.

### 13. Combined filters (AND logic)

```bash
curl -s -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/orders?status=ON_THE_WAY_TO_CUSTOMER&paymentType=VISA&sort=totalPrice,desc" | \
  jq '.data.content | map({paymentType, status: .deliveryInfo.status, price: .totalPrice})'
```

**Expect**: All results have `paymentType=VISA` AND `status=ON_THE_WAY_TO_CUSTOMER`, ordered by `totalPrice` descending.

### 14. Sort by totalPrice ascending

```bash
curl -s -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/orders?sort=totalPrice,asc&size=3" | \
  jq '[.data.content[].totalPrice]'
```

**Expect**: 3 cheapest orders in ascending price order.

### 15. Invalid sort field on orders falls back gracefully

```bash
curl -s -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/orders?sort=address,asc" | \
  jq '.success, .status'
```

**Expect**: `true`, `200` — no error; default sort (`createdAt,desc`) applied.

---

## Swagger UI

Verify both endpoints appear correctly in the API docs:

```
http://localhost:8080/swagger-ui.html
```

- `GET /categories` should show `name`, `page`, `size`, `sort` as separate query parameters.
- `GET /orders` should show `status`, `paymentType`, `createdAfter`, `createdBefore`, `page`, `size`, `sort` as separate query parameters.
