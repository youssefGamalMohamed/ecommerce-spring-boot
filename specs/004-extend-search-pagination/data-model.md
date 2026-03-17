# Data Model: Extend Search and Pagination to Remaining Entities

**Branch**: `004-extend-search-pagination` | **Date**: 2026-03-17

---

## Existing Entities (unchanged — no schema migrations)

### Category

```
Table: Category
```

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | UUID | PK, NOT NULL | Auto-generated UUID |
| name | VARCHAR | UNIQUE, NOT NULL | Case-insensitive partial-match filter |
| created_at | TIMESTAMP | NOT NULL | Audit — from BaseEntity; used as sort field |
| updated_at | TIMESTAMP | NOT NULL | Audit — from BaseEntity |
| created_by | VARCHAR(100) | nullable | Audit — from BaseEntity |
| updated_by | VARCHAR(100) | nullable | Audit — from BaseEntity |

**Relationships**: `ManyToMany` inverse side with `Product` (join table `product_categories` owned by `Product`). Not queried in this feature.

**Filter fields used**: `name` (LIKE), `createdAt` (sort only)

---

### Order

```
Table: `order`   ← backtick-quoted (SQL reserved word)
```

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | UUID | PK, NOT NULL | Auto-generated UUID |
| payment_type | VARCHAR | NOT NULL | Enum: `CASH`, `VISA` |
| total_price | DOUBLE | NOT NULL | Filter via sort; sort field |
| delivery_status | VARCHAR | NOT NULL | Mapped from `DeliveryInfo.status`; filter field |
| delivery_address | VARCHAR | nullable | Mapped from `DeliveryInfo.address`; not filtered |
| delivery_date | VARCHAR | nullable | Mapped from `DeliveryInfo.date`; not filtered |
| created_at | TIMESTAMP | NOT NULL | Audit — from BaseEntity; range filter + sort |
| updated_at | TIMESTAMP | NOT NULL | Audit — from BaseEntity |
| created_by | VARCHAR(100) | nullable | Audit — from BaseEntity |
| updated_by | VARCHAR(100) | nullable | Audit — from BaseEntity |

**Relationships**: `OneToOne` with `Cart` (mapped by `order` field on `Cart`). Not queried in this feature.

**Filter fields used**: `deliveryInfo.status` (exact enum match), `paymentType` (exact enum match), `createdAt` (range: >= createdAfter, <= createdBefore)

---

## Specification Classes (new)

### CategorySpecifications

```java
// File: src/main/java/com/app/ecommerce/category/CategorySpecifications.java

public class CategorySpecifications {

    public static Specification<Category> nameLike(String name) {
        // Returns: LOWER(name) LIKE LOWER('%name%')
        // Returns null (no predicate) when name is null or blank
    }
}
```

### OrderSpecifications

```java
// File: src/main/java/com/app/ecommerce/order/OrderSpecifications.java

public class OrderSpecifications {

    public static Specification<Order> hasStatus(Status status) {
        // Returns: deliveryInfo.status = :status
        // Path: root.get("deliveryInfo").get("status")
        // Returns null when status is null
    }

    public static Specification<Order> hasPaymentType(PaymentType paymentType) {
        // Returns: paymentType = :paymentType
        // Returns null when paymentType is null
    }

    public static Specification<Order> createdAfter(Instant after) {
        // Returns: createdAt >= :after
        // Returns null when after is null
    }

    public static Specification<Order> createdBefore(Instant before) {
        // Returns: createdAt <= :before
        // Returns null when before is null
    }
}
```

---

## Repository Changes

### CategoryRepository

```java
// ADD JpaSpecificationExecutor<Category> to extends clause
public interface CategoryRepository
    extends JpaRepository<Category, UUID>,
            JpaSpecificationExecutor<Category> { ... }
```

### OrderRepository

```java
// ADD JpaSpecificationExecutor<Order> to extends clause
public interface OrderRepository
    extends JpaRepository<Order, UUID>,
            JpaSpecificationExecutor<Order> { ... }
```

---

## Service Method Signatures

### CategoryService (interface change)

```java
// REPLACE: List<CategoryDto> findAll()
// WITH:
Page<CategoryDto> findAll(String name, Pageable pageable);
```

### OrderService (new method added)

```java
// ADD (existing methods unchanged):
Page<OrderDto> findAll(Status status,
                       PaymentType paymentType,
                       Instant createdAfter,
                       Instant createdBefore,
                       Pageable pageable);
```

---

## Validation Rules

| Rule | Entity | Constraint | HTTP Response |
|------|--------|------------|---------------|
| page >= 0 | Both | Spring Pageable; invalid → 400 | 400 with descriptive message |
| size >= 1 | Both | Spring Pageable; invalid → 400 | 400 with descriptive message |
| size max 100 | Both | `spring.data.web.pageable.max-page-size=100` | Silently capped |
| createdAfter <= createdBefore | Order | Guard in service; throws `IllegalArgumentException` | 400 via `RestExceptionHandler` |
| status must be valid enum | Order | Spring auto-converts `@RequestParam`; bad value → exception | 400 via `RestExceptionHandler` |
| paymentType must be valid enum | Order | Spring auto-converts `@RequestParam`; bad value → exception | 400 via `RestExceptionHandler` |
| name blank treated as omitted | Category | `name == null \|\| name.isBlank()` guard in spec | No filter applied |

---

## Default Sort Orders

| Entity | Default Sort | Allowed Fields |
|--------|-------------|----------------|
| Category | `name ASC` | `name`, `createdAt` |
| Order | `createdAt DESC` | `totalPrice`, `createdAt` |
