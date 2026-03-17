# API Contract: Extend Search and Pagination to Remaining Entities

**Branch**: `004-extend-search-pagination` | **Date**: 2026-03-17

---

## Endpoint 1: `GET /categories` — Paginated Category Listing

### Request

```
GET /categories
Authorization: (not required — public endpoint)
```

#### Query Parameters

| Parameter | Type | Required | Default | Constraints | Description |
|-----------|------|----------|---------|-------------|-------------|
| name | String | No | (none) | — | Case-insensitive partial match on category name. Blank treated as omitted. |
| page | Integer | No | 0 | >= 0 | 0-based page number |
| size | Integer | No | 20 | 1–100 | Items per page; capped at 100 |
| sort | String | No | `name,asc` | field must be `name` or `createdAt` | Format: `field,direction`. Invalid field falls back to `name,asc`. |

### Response — HTTP 200

```json
{
  "success": true,
  "status": 200,
  "message": "Success",
  "timestamp": 1710686400000,
  "data": {
    "content": [
      {
        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "name": "Electronics"
      }
    ],
    "totalElements": 42,
    "totalPages": 3,
    "number": 0,
    "size": 20,
    "last": false,
    "first": true
  }
}
```

### Response — HTTP 400 (bad pagination values)

```json
{
  "success": false,
  "status": 400,
  "message": "<descriptive validation message>",
  "timestamp": 1710686400000
}
```

---

## Endpoint 2: `GET /orders` — Paginated Order Listing

### Request

```
GET /orders
Authorization: Bearer <jwt-token>   ← required
```

#### Query Parameters

| Parameter | Type | Required | Default | Constraints | Description |
|-----------|------|----------|---------|-------------|-------------|
| status | String (enum) | No | (none) | `DELIVERED`, `ON_THE_WAY_TO_CUSTOMER`, `NOT_MOVED_OUT_FROM_WAREHOUSE`, `CANCELED` | Exact match on delivery status |
| paymentType | String (enum) | No | (none) | `CASH`, `VISA` | Exact match on payment type |
| createdAfter | ISO-8601 DateTime | No | (none) | <= createdBefore | Inclusive lower bound on `createdAt` |
| createdBefore | ISO-8601 DateTime | No | (none) | >= createdAfter | Inclusive upper bound on `createdAt` |
| page | Integer | No | 0 | >= 0 | 0-based page number |
| size | Integer | No | 20 | 1–100 | Items per page; capped at 100 |
| sort | String | No | `createdAt,desc` | field must be `totalPrice` or `createdAt` | Format: `field,direction`. Invalid field falls back to `createdAt,desc`. |

### Response — HTTP 200

```json
{
  "success": true,
  "status": 200,
  "message": "Success",
  "timestamp": 1710686400000,
  "data": {
    "content": [
      {
        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "paymentType": "VISA",
        "totalPrice": 249.99,
        "deliveryInfo": {
          "status": "ON_THE_WAY_TO_CUSTOMER",
          "address": "123 Main St",
          "date": "2026-03-20"
        }
      }
    ],
    "totalElements": 158,
    "totalPages": 8,
    "number": 0,
    "size": 20,
    "last": false,
    "first": true
  }
}
```

### Response — HTTP 400 (invalid date range)

```json
{
  "success": false,
  "status": 400,
  "message": "createdAfter must be less than or equal to createdBefore",
  "timestamp": 1710686400000
}
```

### Response — HTTP 400 (invalid enum value)

```json
{
  "success": false,
  "status": 400,
  "message": "<descriptive message from MethodArgumentTypeMismatchException>",
  "timestamp": 1710686400000
}
```

### Response — HTTP 401 (missing or invalid token)

```json
{
  "success": false,
  "status": 401,
  "message": "Unauthorized",
  "timestamp": 1710686400000
}
```

---

## Common Behaviours

- When all filters are omitted the full entity collection is returned (paginated).
- All filters are combined with AND logic when multiple are provided.
- An empty result set returns HTTP 200 with `content: []` and `totalElements: 0` — never HTTP 404.
- Invalid sort fields fall back silently to the entity default — no error is returned.
- `size` values above 100 are silently capped at 100; no error is returned.
