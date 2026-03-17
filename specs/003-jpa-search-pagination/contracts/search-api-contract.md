# API Contract: Product Collection Endpoint (Search, Filter, Sort, Paginate)

**Feature**: 003-jpa-search-pagination | **Date**: 2026-03-17

> **REST Best Practice applied**: Filtering, sorting, and pagination are expressed as query parameters on the collection resource `GET /products`. No `/search` suffix.

---

## Endpoint

```
GET /products
```

**Authentication**: None required (public endpoint)
**Content-Type**: `application/json` (response)

**Replaces**: The previous `GET /products?category=<name>` (required param, non-paginated) is superseded by this endpoint. Category filtering is now `GET /products?categoryId=<uuid>` (optional, paginated).

---

## Query Parameters

| Parameter | Type | Default | Required | Constraints |
|---|---|---|---|---|
| `name` | string | — | No | Partial, case-insensitive match on product name |
| `minPrice` | number | — | No | >= 0.0; inclusive lower bound |
| `maxPrice` | number | — | No | >= minPrice when both provided; inclusive upper bound |
| `categoryId` | UUID string | — | No | Valid UUID; exact match on one of the product's category IDs |
| `page` | integer | 0 | No | >= 0 |
| `size` | integer | 20 | No | 1–100 (enforced by `spring.data.web.pageable.max-page-size`) |
| `sort` | string | `createdAt,desc` | No | Format: `field,direction` — e.g., `sort=price,asc`. Allowed fields: `name`, `price`, `createdAt`. Invalid field → fallback to `createdAt,desc` without error. Multiple `sort` params allowed. |

---

## Successful Response — 200 OK

```json
{
  "success": true,
  "status": 200,
  "message": "Operation completed successfully",
  "timestamp": 1710686400000,
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "name": "Wireless Headphones",
        "description": "Premium noise-cancelling headphones",
        "price": 149.99,
        "quantity": 42,
        "categories": [
          { "id": "...", "name": "Electronics" }
        ]
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": { "sorted": true, "empty": false }
    },
    "totalElements": 87,
    "totalPages": 5,
    "last": false,
    "first": true,
    "numberOfElements": 20,
    "empty": false
  }
}
```

The `data` field is the serialized Spring `Page<ProductDto>` object, wrapped in the existing `ApiResponseDto` envelope.

---

## Error Responses

### 400 Bad Request — Validation failure

Triggered when `minPrice > maxPrice`.

```json
{
  "success": false,
  "status": 400,
  "message": "minPrice must be less than or equal to maxPrice",
  "timestamp": 1710686400000
}
```

---

## Example Requests

```
# List all products (default page=0, size=20, sort=createdAt,desc)
GET /products

# Explicit pagination
GET /products?page=0&size=10

# Filter by name
GET /products?name=headphone

# Filter by price range, sort by price ascending
GET /products?minPrice=50&maxPrice=200&sort=price,asc

# Filter by category (UUID)
GET /products?categoryId=550e8400-e29b-41d4-a716-446655440001

# Combined filter + sort + pagination
GET /products?name=phone&minPrice=100&sort=price,desc&page=1&size=5

# Invalid sort field → silently falls back to createdAt,desc
GET /products?sort=unknown,asc
```

---

## Existing Endpoints (unchanged)

| Method | Path | Purpose |
|---|---|---|
| `POST /products` | Create product (Admin) |
| `GET /products/{id}` | Get product by ID |
| `PUT /products/{id}` | Update product (Admin) |
| `DELETE /products/{id}` | Delete product (Admin) |

The `GET /products` endpoint above **replaces** the former `GET /products?category=<name>` endpoint.

---

## Notes

- All filter parameters are optional and combinable (AND logic).
- An unrecognized `sort` field silently falls back to `createdAt,desc`; no 4xx error.
- An empty result is a valid 200 response with `data.content: []` and `data.totalElements: 0`.
- The endpoint resolves TODO(PRODUCTS_WHITELIST) from the constitution.
- No custom pagination DTO; the response uses Spring Data JPA's standard `Page<T>` serialization.
