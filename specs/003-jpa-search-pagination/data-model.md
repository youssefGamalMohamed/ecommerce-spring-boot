# Data Model: JPA Search with Pagination, Filtering, and Sorting

**Branch**: `003-jpa-search-pagination` | **Date**: 2026-03-17

> **Clarification applied (2026-03-17)**: No new DTO or response wrapper classes are introduced. Existing `ProductDto` is the item type. Framework-native `Page<ProductDto>` and `Pageable` replace any custom pagination DTOs.

---

## Existing Entities (no schema changes required)

### Product *(existing — read-only for this feature)*

```
Product {
  UUID       id           — primary key, auto-generated
  String     name         — filterable (LIKE, case-insensitive)
  String     description  — product description
  double     price        — filterable by range (>= minPrice, <= maxPrice)
  Integer    quantity     — stock count
  Set<Category> categories — filterable by category UUID (join)
  Instant    createdAt    — default sort field, from BaseEntity
  Instant    updatedAt    — from BaseEntity
  String     createdBy    — from BaseEntity
  String     updatedBy    — from BaseEntity
}
```

**No schema migration needed.**

### Category *(existing — join target for filtering)*

```
Category {
  UUID    id    — used as categoryId filter value
  UUID    name
}
```

**Relationship**: `Product` ↔ `Category` is Many-to-Many via `product_category` join table.

---

## Existing DTOs (reused — no new DTOs needed)

### ProductDto *(existing — item type in Page response)*

```
ProductDto extends BaseDto {
  UUID          id
  String        name
  String        description
  double        price
  Integer       quantity
  Set<CategoryDto> categories
}
```

The search endpoint returns `Page<ProductDto>`. Spring Data's `Page<T>` automatically provides:
- `content` — list of `ProductDto` items on the current page
- `number` — current page index (0-based)
- `size` — requested page size
- `totalElements` — total matching products across all pages
- `totalPages` — total number of pages
- `last` — whether this is the last page

Wrapped inside the existing `ApiResponseDto<Page<ProductDto>>` envelope.

---

## New Classes

### ProductSpecifications *(new — static Criteria API predicates, no DB schema impact)*

| Method | Filter |
|---|---|
| `nameLike(String name)` | `LOWER(name) LIKE LOWER('%' + name + '%')` |
| `priceGte(Double minPrice)` | `price >= minPrice` |
| `priceLte(Double maxPrice)` | `price <= maxPrice` |
| `hasCategory(UUID categoryId)` | `JOIN categories c WHERE c.id = categoryId` |

Each method returns a `Specification<Product>`. When the argument is null the predicate is not applied; `Specification.where(null)` is a safe no-op.

---

## Repository Change

### ProductRepository *(modified — add JpaSpecificationExecutor)*

```java
// Before
public interface ProductRepository extends JpaRepository<Product, UUID>

// After
public interface ProductRepository extends JpaRepository<Product, UUID>,
                                           JpaSpecificationExecutor<Product>
```

No schema migration required.

---

## Sort Parameter Format (Spring Pageable convention)

Spring's `Pageable` argument resolver interprets the `sort` query parameter as `sort=field,direction`.

| Client `sort` value | Effective sort | Behaviour when invalid field |
|---|---|---|
| `sort=price,asc` | price ASC | → falls back to `createdAt,desc` |
| `sort=name,desc` | name DESC | → falls back to `createdAt,desc` |
| `sort=createdAt,desc` | createdAt DESC | default |
| *(omitted)* | createdAt DESC | default |

Field whitelist enforced in service layer: `name`, `price`, `createdAt`. Any other field is silently replaced with `createdAt`.
