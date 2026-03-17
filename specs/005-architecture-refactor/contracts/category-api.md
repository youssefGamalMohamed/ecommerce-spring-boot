# API Contract: Categories

**Base Path**: `/ecommerce/api/v1/categories`

---

## POST /categories (Create Category)

**Auth**: Required (ADMIN only)

**Request Body** (`CreateCategoryRequest`):
```json
{
  "name": "Electronics"
}
```

**Validation**:
- `name`: required, non-blank, max 100 chars, must be unique

**Response** (201 Created): `CategoryResponse`
**Error** (409): Duplicate category name

---

## GET /categories (List/Search Categories)

**Auth**: Public

**Query Parameters**:
- `name` (optional): partial match, case-insensitive
- `page`, `size`, `sort`: standard pagination

**Response** (200 OK): Paginated `CategoryResponse`

---

## GET /categories/{id}

**Auth**: Public

**Response** (200 OK): Single `CategoryResponse`
**Error** (404): Not found

---

## PATCH /categories/{id} (Partial Update)

**Auth**: Required (ADMIN only)

**Request Body** (`UpdateCategoryRequest`):
```json
{
  "name": "Consumer Electronics",
  "version": 0
}
```

**Response** (200 OK): Updated `CategoryResponse`
**Error** (404): Not found | **Error** (409): Version conflict or duplicate name

---

## DELETE /categories/{id}

**Auth**: Required (ADMIN only)

**Response** (204 No Content)
**Error** (404): Not found
