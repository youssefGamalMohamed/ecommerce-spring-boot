# Data Model: Architecture Refactor & Enhancement

**Branch**: `005-architecture-refactor` | **Date**: 2026-03-18

## Entity Changes Summary

| Entity | Action | Key Changes |
|--------|--------|-------------|
| Product | MODIFY | `double price` → `BigDecimal price`, add `@Version`, `EAGER` → `LAZY`, remove `@JsonIgnore` |
| Category | MODIFY | Add `@Version`, `EAGER` → `LAZY`, remove `@JsonIgnore` |
| Order | MODIFY | `double totalPrice` → `BigDecimal totalPrice`, add `@Version` |
| DeliveryInfo | MODIFY | `String date` → `LocalDate date` |
| Cart | MODIFY | Add `@Version`, remove `@JsonIgnore` |
| CartItem | MODIFY | Remove `@JsonIgnore` |
| BaseEntity | KEEP | Already correct (Instant timestamps, String auditor) |
| User | NEW | Authentication entity with credentials and role |
| Token | NEW | JWT token tracking for revocation |
| IdempotencyRecord | NEW | Idempotency key storage with response cache |

---

## Modified Entities

### Product

```
Product (table: Product)
├── id: UUID (PK, auto-generated)
├── name: String (max 255)
├── description: String
├── price: BigDecimal (precision 19, scale 2)      ← WAS: double
├── quantity: Integer
├── version: Long                                    ← NEW: optimistic locking
├── categories: Set<Category> (ManyToMany, LAZY)     ← WAS: EAGER
├── cartItem: List<CartItem> (OneToMany, LAZY)       ← REMOVE @JsonIgnore
├── createdAt: Instant (audit, auto)
├── updatedAt: Instant (audit, auto)
├── createdBy: String (audit, auto)
└── updatedBy: String (audit, auto)
```

**Column changes**:
- `price` column: `DOUBLE` → `DECIMAL(19,2)`
- `version` column: `BIGINT` (new, nullable initially, Hibernate manages)

**Relationship changes**:
- `categories`: `FetchType.EAGER` → `FetchType.LAZY`
- Repository adds `@EntityGraph(attributePaths = "categories")` on query methods needing categories

---

### Category

```
Category (table: Category)
├── id: UUID (PK, auto-generated)
├── name: String (unique, max 100)
├── version: Long                                    ← NEW: optimistic locking
├── products: Set<Product> (ManyToMany, LAZY)        ← WAS: EAGER, REMOVE @JsonIgnore
├── createdAt: Instant (audit, auto)
├── updatedAt: Instant (audit, auto)
├── createdBy: String (audit, auto)
└── updatedBy: String (audit, auto)
```

**Column changes**:
- `version` column: `BIGINT` (new)

**Relationship changes**:
- `products`: `FetchType.EAGER` → `FetchType.LAZY`

---

### Order

```
Order (table: `order`)
├── id: UUID (PK, auto-generated)
├── paymentType: PaymentType (enum STRING)
├── totalPrice: BigDecimal (precision 19, scale 2)   ← WAS: double
├── version: Long                                     ← NEW: optimistic locking
├── deliveryInfo: DeliveryInfo (embedded)
├── cart: Cart (OneToOne, cascade ALL)
├── createdAt: Instant (audit, auto)
├── updatedAt: Instant (audit, auto)
├── createdBy: String (audit, auto)
└── updatedBy: String (audit, auto)
```

**Column changes**:
- `total_price` column: `DOUBLE` → `DECIMAL(19,2)`
- `version` column: `BIGINT` (new)

---

### DeliveryInfo (Embeddable)

```
DeliveryInfo (embedded in Order)
├── status: Status (enum STRING, default NOT_MOVED_OUT_FROM_WAREHOUSE)
├── address: String
└── date: LocalDate                                   ← WAS: String
```

**Column changes**:
- `delivery_date` column: `VARCHAR` → `DATE`

---

### Cart

```
Cart (table: Cart)
├── id: UUID (PK, auto-generated)
├── version: Long                                     ← NEW: optimistic locking
├── cartItems: Set<CartItem> (OneToMany, cascade ALL)
├── order: Order (OneToOne)                           ← REMOVE @JsonIgnore
├── createdAt: Instant (audit, auto)
├── updatedAt: Instant (audit, auto)
├── createdBy: String (audit, auto)
└── updatedBy: String (audit, auto)
```

---

### CartItem

```
CartItem (table: Cart_Item)
├── id: UUID (PK, auto-generated)
├── product: Product (ManyToOne)
├── productQuantity: int
├── cart: Cart (ManyToOne)                            ← REMOVE @JsonIgnore
├── createdAt: Instant (audit, auto)
├── updatedAt: Instant (audit, auto)
├── createdBy: String (audit, auto)
└── updatedBy: String (audit, auto)
```

---

## New Entities

### User

```
User (table: users)
├── id: UUID (PK, auto-generated)
├── username: String (unique, max 100)
├── email: String (unique, max 255)
├── password: String (BCrypt encoded, max 255)
├── role: Role (enum STRING: ADMIN, CUSTOMER)
├── enabled: boolean (default true)
├── createdAt: Instant (audit, auto)
├── updatedAt: Instant (audit, auto)
├── createdBy: String (audit, auto)
└── updatedBy: String (audit, auto)
```

**Implements**: `UserDetails` (Spring Security)

**Relationships**: None initially (Orders/Carts can reference User in future iteration)

---

### Token

```
Token (table: tokens)
├── id: UUID (PK, auto-generated)
├── accessToken: String (unique, max 500)
├── refreshToken: String (unique, max 500)
├── revoked: boolean (default false)
├── expired: boolean (default false)
├── user: User (ManyToOne)
├── createdAt: Instant (audit, auto)
└── updatedAt: Instant (audit, auto)
```

**Purpose**: Track issued tokens for revocation support. On logout or password change, all tokens for that user are marked `revoked = true`.

---

### IdempotencyRecord

```
IdempotencyRecord (table: idempotency_records)
├── id: UUID (PK, auto-generated)
├── idempotencyKey: String (unique, max 255)
├── httpStatus: int
├── responseBody: String (TEXT/CLOB — cached JSON response)
├── createdAt: Instant
└── expiresAt: Instant (createdAt + 24 hours)
```

**Purpose**: Stores the response of the first request with a given idempotency key. Subsequent requests with the same key return the stored response without re-processing.

**Cleanup**: Scheduled job deletes records where `expiresAt < now`.

---

## State Machine: Order Status

```
                    ┌──────────────────────────┐
                    │ NOT_MOVED_OUT_FROM_       │
                    │ WAREHOUSE (initial)       │
                    └─────────┬────────┬────────┘
                              │        │
                    transition│        │ cancel
                              ▼        ▼
                    ┌──────────────┐  ┌──────────┐
                    │ ON_THE_WAY_  │  │ CANCELED  │
                    │ TO_CUSTOMER  │  │ (terminal)│
                    └──────┬──┬───┘  └──────────┘
                           │  │              ▲
                 transition│  │ cancel       │
                           ▼  └──────────────┘
                    ┌──────────────┐
                    │  DELIVERED   │
                    │  (terminal)  │
                    └──────────────┘
```

**Allowed transitions**:

| From | Allowed To |
|------|-----------|
| NOT_MOVED_OUT_FROM_WAREHOUSE | ON_THE_WAY_TO_CUSTOMER, CANCELED |
| ON_THE_WAY_TO_CUSTOMER | DELIVERED, CANCELED |
| DELIVERED | (none — terminal) |
| CANCELED | (none — terminal) |

---

## DTO Structure

### Request DTOs (NEW)

**CreateProductRequest**:
- `name`: String (`@NotBlank`, `@Size(max = 255)`)
- `description`: String (optional)
- `price`: BigDecimal (`@NotNull`, `@DecimalMin("0.00")`)
- `quantity`: Integer (`@NotNull`, `@Min(0)`)
- `categoryIds`: Set<UUID> (`@NotEmpty`)

**UpdateProductRequest**:
- `name`: String (optional, `@Size(max = 255)`)
- `description`: String (optional)
- `price`: BigDecimal (optional, `@DecimalMin("0.00")`)
- `quantity`: Integer (optional, `@Min(0)`)
- `categoryIds`: Set<UUID> (optional)
- `version`: Long (`@NotNull` — required for optimistic locking)

**CreateCategoryRequest**:
- `name`: String (`@NotBlank`, `@Size(max = 100)`)

**UpdateCategoryRequest**:
- `name`: String (optional, `@Size(max = 100)`)
- `version`: Long (`@NotNull`)

**CreateOrderRequest**:
- `paymentType`: PaymentType (`@NotNull`)
- `cartId`: UUID (`@NotNull`)

**UpdateOrderRequest**:
- `deliveryStatus`: Status (optional — validated via state machine)
- `deliveryAddress`: String (optional)
- `deliveryDate`: LocalDate (optional)
- `version`: Long (`@NotNull`)

### Response DTOs (RENAMED from existing)

**ProductResponse** (was `ProductDto`):
- `id`: UUID
- `name`: String
- `description`: String
- `price`: BigDecimal
- `quantity`: Integer
- `version`: Long
- `categories`: Set<CategoryResponse>
- `createdAt`: Instant
- `updatedAt`: Instant
- `createdBy`: String
- `updatedBy`: String

**CategoryResponse** (was `CategoryDto`):
- `id`: UUID
- `name`: String
- `version`: Long
- `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

**OrderResponse** (was `OrderDto`):
- `id`: UUID
- `paymentType`: PaymentType
- `totalPrice`: BigDecimal
- `version`: Long
- `deliveryInfo`: DeliveryInfoResponse
- `cart`: CartDto
- `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

**DeliveryInfoResponse** (was `DeliveryInfoDto`):
- `status`: Status
- `address`: String
- `date`: LocalDate

**BaseResponse** (was `BaseDto`):
- `createdAt`: Instant
- `updatedAt`: Instant
- `createdBy`: String
- `updatedBy`: String

### Auth DTOs (NEW)

**RegisterRequest**:
- `username`: String (`@NotBlank`, `@Size(min = 3, max = 100)`)
- `email`: String (`@NotBlank`, `@Email`)
- `password`: String (`@NotBlank`, `@Size(min = 8, max = 100)`)

**LoginRequest**:
- `username`: String (`@NotBlank`)
- `password`: String (`@NotBlank`)

**LoginResponse**:
- `accessToken`: String
- `refreshToken`: String

**RefreshTokenRequest**:
- `refreshToken`: String (`@NotBlank`)

---

## Database Migration Notes

### Column Type Changes (handled by Hibernate ddl-auto: update)

1. `Product.price`: `DOUBLE` → `DECIMAL(19,2)` — MySQL rounds on cast
2. `Order.total_price`: `DOUBLE` → `DECIMAL(19,2)` — MySQL rounds on cast
3. `Order.delivery_date`: `VARCHAR(255)` → `DATE` — requires ISO format data

### New Columns

1. `Product.version`: `BIGINT` (nullable, Hibernate auto-manages)
2. `Category.version`: `BIGINT` (nullable)
3. `Order.version`: `BIGINT` (nullable)
4. `Cart.version`: `BIGINT` (nullable)

### New Tables

1. `users` — User authentication
2. `tokens` — JWT token tracking
3. `idempotency_records` — Idempotency key storage

### Pre-Migration Verification Queries

```sql
-- Verify no precision loss on Product prices
SELECT id, price, CAST(price AS DECIMAL(19,2)) as decimal_price
FROM Product
WHERE price != CAST(price AS DECIMAL(19,2));

-- Verify no precision loss on Order totals
SELECT id, total_price, CAST(total_price AS DECIMAL(19,2)) as decimal_price
FROM `order`
WHERE total_price != CAST(total_price AS DECIMAL(19,2));

-- Verify delivery_date format is parseable as DATE
SELECT id, delivery_date
FROM `order`
WHERE delivery_date IS NOT NULL
AND STR_TO_DATE(delivery_date, '%Y-%m-%d') IS NULL;
```
