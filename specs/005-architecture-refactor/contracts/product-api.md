# API Contract: Products

**Base Path**: `/ecommerce/api/v1/products`

---

## POST /products (Create Product)

**Auth**: Required (ADMIN only)

**Request Body** (`CreateProductRequest`):
```json
{
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse with USB receiver",
  "price": 29.99,
  "quantity": 150,
  "categoryIds": ["uuid-1", "uuid-2"]
}
```

**Validation**:
- `name`: required, non-blank, max 255 chars
- `price`: required, >= 0.00, decimal with 2-digit precision
- `quantity`: required, >= 0
- `categoryIds`: required, non-empty set of valid category UUIDs

**Response** (201 Created):
```json
{
  "success": true,
  "status": 201,
  "message": "Created",
  "data": {
    "id": "uuid",
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse with USB receiver",
    "price": 29.99,
    "quantity": 150,
    "version": 0,
    "categories": [
      { "id": "uuid-1", "name": "Electronics", "version": 0, "createdAt": "...", "updatedAt": "...", "createdBy": "admin", "updatedBy": "admin" }
    ],
    "createdAt": "2026-03-18T10:30:00Z",
    "updatedAt": "2026-03-18T10:30:00Z",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
}
```

---

## GET /products (List/Search Products)

**Auth**: Public (no token required)

**Query Parameters**:
- `name` (optional): partial match, case-insensitive
- `minPrice` (optional): BigDecimal, minimum price filter
- `maxPrice` (optional): BigDecimal, maximum price filter
- `categoryId` (optional): UUID, filter by category
- `page` (optional): int, default 0
- `size` (optional): int, default 20, max 100
- `sort` (optional): field,direction (e.g., `price,asc`). Allowed fields: `name`, `price`, `quantity`, `createdAt`

**Validation**: `minPrice` <= `maxPrice` when both provided

**Response** (200 OK): Paginated `ProductResponse` wrapped in `ApiResponseDto`

---

## GET /products/{id} (Get Product by ID)

**Auth**: Public

**Response** (200 OK): Single `ProductResponse`
**Error** (404): Product not found

---

## PATCH /products/{id} (Partial Update)

**Auth**: Required (ADMIN only)

**Request Body** (`UpdateProductRequest`):
```json
{
  "price": 24.99,
  "version": 0
}
```

Only include fields to change. `version` is always required for optimistic locking.

**Validation**: Same rules as create, but all fields optional except `version`

**Response** (200 OK): Updated `ProductResponse`
**Error** (404): Not found | **Error** (409): Version conflict

---

## DELETE /products/{id}

**Auth**: Required (ADMIN only)

**Response** (204 No Content)
**Error** (404): Not found
