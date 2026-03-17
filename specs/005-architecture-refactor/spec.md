# Feature Specification: Architecture Refactor & Enhancement

**Feature Branch**: `005-architecture-refactor`
**Created**: 2026-03-18
**Status**: Draft
**Input**: User description: "Comprehensive enhancement and refactor plan covering API design best practices, SOLID principles, design patterns, transactional integrity, security, caching, entity layer fixes, input validation, and cross-cutting concerns for the ecommerce application."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Data Integrity on Write Operations (Priority: P1)

As a store operator, I need all write operations (creating products, placing orders, updating inventory) to either fully succeed or fully roll back, so that my store data is never left in an inconsistent state — for example, an order exists but inventory was not deducted, or a product references categories that were only partially saved.

**Why this priority**: Without transactional boundaries, a failure mid-operation can leave orphaned records, incorrect totals, or ghost orders. Data integrity is non-negotiable for any commerce system and is the single highest-risk gap in the current application.

**Independent Test**: Can be fully tested by simulating a failure mid-operation (e.g., forcing a database constraint violation after the first write in a multi-step save) and verifying the entire operation rolls back cleanly with no partial state persisted.

**Acceptance Scenarios**:

1. **Given** a product save request that references 3 categories, **When** the product insert succeeds but the third category association fails, **Then** the entire operation rolls back — no product row and no category associations are persisted.
2. **Given** an order update that changes status and delivery info, **When** the delivery info update fails due to a constraint violation, **Then** the order status remains unchanged.
3. **Given** a concurrent update to the same product by two users, **When** both submit at the same time, **Then** exactly one succeeds and the other receives a conflict error — no data is silently overwritten.
4. **Given** any read-only operation (findById, findAll, search), **When** executed, **Then** it runs in a read-only context for consistency and performance optimization.

---

### User Story 2 - Accurate Monetary Calculations (Priority: P1)

As a customer placing an order, I need prices and totals to be calculated with exact precision so that I am never overcharged or undercharged due to rounding errors. As a store operator, I need financial reports to balance correctly to the cent.

**Why this priority**: Using floating-point representation for money is a well-known source of silent data corruption. An order totaling $19.99 + $5.01 might store as $25.000000000000004. This causes incorrect invoices, failed payment reconciliation, and regulatory issues. Tied at P1 because it is a data-model change that ripples through every layer.

**Independent Test**: Can be fully tested by creating products with prices like $0.10, $0.20, $19.99 and verifying that cart totals, order totals, and persisted values are exact to the cent with no floating-point drift.

**Acceptance Scenarios**:

1. **Given** a product priced at $0.10, **When** a customer adds 3 to cart, **Then** the cart total is exactly $0.30 (not $0.30000000000000004).
2. **Given** an order with 7 line items, **When** the total is computed, **Then** it matches the sum of individual line prices to the cent.
3. **Given** a product price of $9999999.99, **When** stored and retrieved, **Then** the value is identical with no precision loss.
4. **Given** existing data in the database with floating-point prices, **When** the data migration runs, **Then** all values are converted to exact decimal representation without rounding errors.

---

### User Story 3 - Separate Input and Output Contracts (Priority: P1)

As an API consumer (frontend developer or integration partner), I need create/update requests to accept only the fields I should provide, and responses to return only the fields I should see, so that I am not confused by irrelevant fields and cannot accidentally send data that should be system-managed (like IDs, audit timestamps, or internal status).

**Why this priority**: A single data transfer object for both input and output violates API design best practices. Clients can currently send `id`, `createdAt`, `updatedAt` in request bodies — these are silently ignored, creating confusion and documentation noise. Splitting contracts also enables per-operation validation (e.g., `name` required on create, optional on update).

**Independent Test**: Can be fully tested by sending a create request with an `id` field and verifying it is rejected or ignored, and by sending an update request with only changed fields and verifying unchanged fields remain intact.

**Acceptance Scenarios**:

1. **Given** a product creation request, **When** the client sends only writable fields (name, description, price, quantity, category identifiers), **Then** the system creates the product and returns a full response including system-generated fields (id, creation timestamp, etc.).
2. **Given** a product creation request, **When** the client includes read-only fields (id, creation timestamp), **Then** the system ignores them — the response contains system-generated values, not client-provided ones.
3. **Given** a product update request, **When** the client sends only the fields they want to change, **Then** only those fields are updated and all other fields retain their previous values.
4. **Given** any successful response, **When** the client inspects it, **Then** it includes all relevant fields including audit metadata (created/updated timestamps, who changed it).

---

### User Story 4 - Secure API Access (Priority: P2)

As a store operator, I need the API to require authentication so that only authorized users can access and modify store data. As a customer, I need my account and order data to be protected from unauthorized access by other users.

**Why this priority**: The entire API is currently publicly accessible with no authentication. While less immediately dangerous than data integrity issues (P1), this is essential before any production deployment. Without authentication, anyone can create/delete products, view all orders, and modify prices.

**Independent Test**: Can be fully tested by attempting to access any protected endpoint without a valid token and verifying a 401 response, then authenticating and verifying access is granted according to the user's role.

**Acceptance Scenarios**:

1. **Given** an unauthenticated request to any protected endpoint, **When** no token is provided, **Then** the system returns 401 Unauthorized.
2. **Given** a valid authentication token for a user with appropriate permissions, **When** the user accesses resources within their role, **Then** access is granted.
3. **Given** a valid token for a customer role, **When** the user attempts to delete a product (admin-only operation), **Then** the system returns 403 Forbidden.
4. **Given** a valid token, **When** the token expires, **Then** subsequent requests return 401 and the client must re-authenticate.
5. **Given** a login request with valid credentials, **When** the system authenticates the user, **Then** a token is returned with appropriate expiration.
6. **Given** a data modification by an authenticated user, **When** the change is persisted, **Then** the audit fields record the authenticated user's identity (not a hardcoded placeholder).

---

### User Story 5 - Robust Input Validation with Clear Error Messages (Priority: P2)

As an API consumer, I need every input to be validated before processing, and any validation failure to return a clear, actionable error message telling me exactly which field failed and why, so that I can correct my request without guessing.

**Why this priority**: Current validation is partial — some fields lack constraints, and business rule validation is inconsistent. Poor validation leads to bad data entering the system and confusing client-side debugging. Combined with the request contract split (P1), this becomes the quality gate for all incoming data.

**Independent Test**: Can be fully tested by sending requests with missing required fields, invalid formats, out-of-range values, and verifying each returns a specific, field-level error message.

**Acceptance Scenarios**:

1. **Given** a product creation request with a blank name, **When** submitted, **Then** the system returns 400 with a message identifying `name` as required.
2. **Given** a product creation request with a negative price, **When** submitted, **Then** the system returns 400 with a message that price must be zero or positive.
3. **Given** a product creation request with quantity exceeding the system maximum, **When** submitted, **Then** the system returns 400 with the allowed range.
4. **Given** a search request with `minPrice` greater than `maxPrice`, **When** submitted, **Then** the system returns 400 explaining the constraint.
5. **Given** a request with multiple validation failures, **When** submitted, **Then** all field-level errors are returned in a single response (not one at a time).

---

### User Story 6 - Optimized Data Loading and Caching (Priority: P2)

As a customer browsing the catalog, I need product listing pages to load quickly even under high traffic, without the system making excessive database queries behind the scenes. As a store operator, I need cache updates to reflect my changes promptly without requiring a full cache flush.

**Why this priority**: The current eager loading on product-to-category relationships causes N+1 query problems on every list fetch. The current cache eviction strategy destroys the entire cache on any single write. Together, these create a performance ceiling that will be hit as data grows.

**Independent Test**: Can be fully tested by loading a product listing page and measuring the number of database queries executed (verifying no N+1), and by updating a single product and verifying that only that product's cache entry is invalidated.

**Acceptance Scenarios**:

1. **Given** 100 products each with 3 categories, **When** loading a paginated list of 20 products, **Then** the system executes a bounded number of queries (not 21+).
2. **Given** a cached product, **When** that product is updated, **Then** only that product's cache entry is refreshed — other cached products remain unaffected.
3. **Given** a product listing request, **When** the cache layer is unavailable, **Then** the system falls back to the database transparently with no error to the user.
4. **Given** a product that was recently cached, **When** retrieved again within the TTL window, **Then** no database query is executed.

---

### User Story 7 - Clean Entity Model and Correct Data Types (Priority: P2)

As a developer maintaining this codebase, I need entities to use correct data types (dates as date types, not strings), have no serialization annotations (since entities never reach the API layer directly), and use lazy loading by default, so that the data model is correct, performant, and free of layer-leaking concerns.

**Why this priority**: String-typed date fields invite format bugs and prevent date comparisons. Serialization annotations on entities suggest they might leak to the API. Eager loading as default causes performance issues. These are foundational fixes that make all other refactoring safer.

**Independent Test**: Can be fully tested by verifying that entities compile and function correctly without any serialization annotations, that date fields accept and return proper date types, and that lazy-loaded collections are fetched only when explicitly needed.

**Acceptance Scenarios**:

1. **Given** a delivery info with a date, **When** stored and retrieved, **Then** the date is a proper date type (not a string), supports comparison, and is formatted consistently in API responses.
2. **Given** an entity class, **When** inspected, **Then** it contains no serialization-specific annotations — all serialization control lives in data transfer objects and mappers.
3. **Given** a product entity loaded without its categories accessed, **When** the transaction ends, **Then** no category query was executed (lazy loading).
4. **Given** a product loaded with an explicit instruction to include categories, **When** categories are accessed, **Then** they are available without an additional query.

---

### User Story 8 - Order Lifecycle State Management (Priority: P3)

As a store operator, I need the system to enforce valid order status transitions (e.g., a canceled order cannot be marked as delivered), so that order status always reflects reality and downstream processes (shipping, refunds, notifications) are triggered correctly.

**Why this priority**: Currently any status can be set on any order without validation. While not immediately dangerous for a small operation, incorrect state transitions cause fulfillment errors, customer confusion, and accounting discrepancies at scale.

**Independent Test**: Can be fully tested by attempting every possible status transition and verifying that only valid ones succeed, while invalid ones are rejected with a clear message explaining the allowed transitions from the current state.

**Acceptance Scenarios**:

1. **Given** an order with status NOT_MOVED_OUT_FROM_WAREHOUSE, **When** updated to ON_THE_WAY_TO_CUSTOMER, **Then** the transition succeeds.
2. **Given** an order with status CANCELED, **When** updated to DELIVERED, **Then** the system rejects the transition with a message listing valid transitions from CANCELED (none — it is a terminal state).
3. **Given** an order with status ON_THE_WAY_TO_CUSTOMER, **When** updated to DELIVERED, **Then** the transition succeeds.
4. **Given** any order, **When** a valid status transition occurs, **Then** the transition timestamp and actor are recorded for audit purposes.

---

### User Story 9 - Application Health and Observability (Priority: P3)

As a DevOps engineer deploying this application, I need health check endpoints and basic metrics so that I can monitor application status, detect issues proactively, and integrate with infrastructure monitoring tools.

**Why this priority**: Without health checks and metrics, production monitoring is blind. This is lower priority than data integrity and security but essential for production readiness. It is also low-effort relative to its value.

**Independent Test**: Can be fully tested by hitting the health endpoint and verifying it reports database connectivity and cache availability, and by checking that basic metrics (request count, response times) are exposed.

**Acceptance Scenarios**:

1. **Given** a running application, **When** the health endpoint is called, **Then** it reports overall status and individual component health (database, cache, disk).
2. **Given** the database is unreachable, **When** the health endpoint is called, **Then** it reports the application as degraded with the database component marked as down.
3. **Given** a running application, **When** the metrics endpoint is called, **Then** it exposes request counts, response times, memory usage, and active database connections.

---

### User Story 10 - Idempotent Write Operations (Priority: P3)

As an API consumer, I need create operations (especially order placement) to be idempotent, so that network retries or client-side double-clicks do not create duplicate records.

**Why this priority**: In ecommerce, duplicate orders are a real operational problem — they cause double charges, double shipments, and customer complaints. An idempotency mechanism prevents this class of bugs entirely. Lower priority than core integrity and security, but important for production reliability.

**Independent Test**: Can be fully tested by sending the same create request twice with the same idempotency key and verifying that only one resource is created, with the second request returning the result of the first.

**Acceptance Scenarios**:

1. **Given** an order creation request with an idempotency key, **When** the same request is sent twice, **Then** only one order is created and both responses return the same order.
2. **Given** an order creation request with a new idempotency key, **When** submitted, **Then** a new order is created normally.
3. **Given** an idempotency key that was used more than 24 hours ago, **When** reused, **Then** it is treated as a new request (keys expire after 24 hours).

---

### Edge Cases

- What happens when a transaction spans multiple services (e.g., product save triggers category validation) and the outer operation fails — does everything roll back consistently?
- How does the system behave when the cache layer is down during a cache write — does it degrade gracefully or block the operation?
- What happens when a product's price is updated while an order referencing that product is in-progress — does the order use the original or new price?
- How does the system handle concurrent updates to the same entity — is optimistic locking in place to detect conflicts?
- What happens when the database migration from floating-point to decimal encounters a value that cannot be represented exactly?
- How does the system handle a token that is structurally valid but was issued before a password change or user deactivation?
- What happens when a partial-update request sends an empty body — should the system reject it or treat it as a no-op?
- How does the system handle a search request where all filter parameters are null — should it return all results (paginated) or reject the request?

## Requirements *(mandatory)*

### Functional Requirements

**Transactional Integrity**

- **FR-001**: System MUST wrap all write operations (create, update, delete) in database transactions that roll back entirely on any failure.
- **FR-002**: System MUST use read-only transactions for all query operations to enable database-level optimizations and consistent reads.
- **FR-003**: System MUST implement optimistic locking on entities that can be concurrently modified (Product, Order, Category) to detect and reject conflicting updates with a clear conflict error.

**Monetary Precision**

- **FR-004**: System MUST represent all monetary values (product price, order total, cart item subtotal) as exact decimal types with at minimum 2-digit fractional precision, eliminating floating-point arithmetic entirely.
- **FR-005**: System MUST migrate existing floating-point price data to exact decimal format without precision loss via a database schema change.

**API Contract Separation**

- **FR-006**: System MUST define separate request models for create and update operations, exposing only client-writable fields.
- **FR-007**: System MUST define response models that include all relevant fields plus system-generated metadata (id, timestamps, auditor identity).
- **FR-008**: System MUST ignore read-only fields (id, audit fields) if included in create/update requests — they MUST NOT override system-generated values.
- **FR-009**: System MUST support partial updates — clients send only changed fields, and unchanged fields retain their previous values.

**Input Validation**

- **FR-010**: System MUST validate all required fields on create requests and return field-level errors for each violation.
- **FR-011**: System MUST validate value constraints: prices must be non-negative, quantities must be non-negative integers, names must be non-blank and within length limits.
- **FR-012**: System MUST validate business rules cross-field: search range constraints (minPrice <= maxPrice, startDate <= endDate) and return specific error messages.
- **FR-013**: System MUST return all validation errors in a single response when multiple fields fail (not fail-fast on the first error).

**Authentication & Authorization**

- **FR-014**: System MUST require a valid authentication token for all endpoints except login, registration, and public product/category browsing.
- **FR-015**: System MUST support at minimum two roles: ADMIN (full CRUD on all resources) and CUSTOMER (read products/categories, manage own cart, place and view own orders).
- **FR-016**: System MUST return 401 for missing, invalid, or expired tokens and 403 for insufficient permissions.
- **FR-017**: System MUST record the authenticated user's identity as the auditor for all data changes (replacing any hardcoded placeholder).

**Entity Layer Correctness**

- **FR-018**: System MUST use proper date/time types for all date fields — no string-typed dates anywhere in the data model.
- **FR-019**: System MUST NOT have serialization-specific annotations on data model entities — all serialization control MUST live in data transfer objects and mappers.
- **FR-020**: System MUST use lazy loading as the default fetch strategy for all entity relationships, with explicit eager loading only where needed for specific operations.

**Caching Optimization**

- **FR-021**: System MUST evict only the specific cache entry affected by a write operation, not the entire entity cache.
- **FR-022**: System MUST use cache-put semantics on save/update to refresh the specific entry without full eviction.
- **FR-023**: System MUST degrade gracefully when the cache layer is unavailable — all operations MUST continue via direct database access with no error exposed to the user.

**Order State Management**

- **FR-024**: System MUST enforce a defined set of valid order status transitions and reject any transition not in the allowed set with an error listing valid transitions from the current state.
- **FR-025**: System MUST record the timestamp and actor for every status transition for audit purposes.

**Observability**

- **FR-026**: System MUST expose a health check endpoint reporting status of all critical dependencies (database, cache layer).
- **FR-027**: System MUST expose basic operational metrics (request count, response time distribution, active connections).

**Idempotency**

- **FR-028**: System MUST support idempotency keys on order creation to prevent duplicate orders from retried requests.
- **FR-029**: Idempotency keys MUST expire after 24 hours, after which the same key may be reused for a new request.

### Key Entities

- **User/Account**: Represents an authenticated user with credentials, role (ADMIN or CUSTOMER), and profile information. Related to Orders (as purchaser) and Cart (as owner). Provides identity for audit trails.
- **Product**: Represents a purchasable item with name, description, exact-decimal price, available quantity, and category associations. Subject to optimistic locking for concurrent updates.
- **Category**: Represents a product grouping with a unique name. Many-to-many relationship with Product.
- **Order**: Represents a customer purchase with a state-machine-managed status, payment type, exact-decimal total, delivery information, and associated cart. Tracks status transition history with timestamps and actors.
- **Cart / CartItem**: Represents a customer's shopping session. CartItem links a Product to a Cart with a quantity.
- **DeliveryInfo**: Value object within Order containing delivery status, address, and delivery date (proper date type, not string).
- **IdempotencyRecord**: Tracks submitted idempotency keys with their associated response data and expiration time, used to detect and deduplicate retried requests.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Zero partial writes — any operation that fails mid-execution leaves the database in its pre-operation state 100% of the time.
- **SC-002**: Monetary calculations are exact to the cent — no value in the system differs from its mathematically correct result by any amount, verifiable by summing line items and comparing to stored totals.
- **SC-003**: API consumers can determine all writable fields for any operation by inspecting the request schema alone, with zero ambiguity about which fields are accepted versus system-generated.
- **SC-004**: 100% of protected endpoints return 401 or 403 for unauthorized access — no endpoint is publicly writable after security is enabled.
- **SC-005**: 100% of validation failures return a response containing every field-level error in a single message, not one error at a time.
- **SC-006**: Product listing pages with 20 items execute a bounded, predictable number of database queries regardless of how many categories each product has — no N+1 pattern.
- **SC-007**: Updating a single product invalidates only that product's cache entry — other cached products remain available without a cache miss.
- **SC-008**: Invalid order status transitions are rejected 100% of the time with a message listing the allowed transitions from the current state.
- **SC-009**: Duplicate order submissions with the same idempotency key result in exactly one persisted order, with both responses returning identical data.
- **SC-010**: Health endpoint responds accurately, reporting degraded status within 10 seconds of a dependency failure.
- **SC-011**: All date fields throughout the system are stored and transmitted as proper date types — zero string-typed dates remain.
- **SC-012**: Concurrent updates to the same entity are detected and the second writer receives a conflict error, preventing silent data overwrite, 100% of the time.

## Assumptions

- The application is not yet in production, so breaking changes to the API contract (data transfer structure, field types) are acceptable without a versioning or migration strategy for existing API consumers.
- The existing database can be migrated from floating-point to decimal columns via a schema change without data loss — all existing price values are representable in decimal.
- The cache layer is used solely as a cache (not as a primary data store), so cache invalidation strategies do not need to guarantee zero staleness — eventual consistency within TTL windows is acceptable.
- Token-based stateless authentication is the appropriate choice for this application (no requirement for session-based auth or external identity provider integration at this time).
- The two roles (ADMIN, CUSTOMER) are sufficient for the initial security implementation — more granular role-based or attribute-based access control can be added later.
- Optimistic locking is preferred over pessimistic locking for concurrency control, as the application does not have extremely high write contention on individual records.
- The order state machine has four states: NOT_MOVED_OUT_FROM_WAREHOUSE (initial) -> ON_THE_WAY_TO_CUSTOMER -> DELIVERED (terminal), and CANCELED (terminal, reachable from any non-terminal state).
- Public browsing of products and categories does not require authentication — only write operations and personal data (cart, orders, account) require a valid token.

## Scope Boundaries

**In Scope:**

- All 10 user stories described above (transactional integrity, monetary precision, API contract separation, authentication/authorization, input validation, caching optimization, entity model correctness, order state management, observability, idempotency)
- Database schema changes (decimal types, version columns, user table, idempotency table)
- New request/response data transfer models and updated mappers
- Security integration with role-based access
- Transaction annotations across the service layer
- Entity model corrections (fetch types, data types, removing serialization annotations)
- Cache strategy refinement (targeted eviction)
- Order state machine validation logic
- Validation annotations on request models
- Health and metrics endpoint setup
- Idempotency mechanism for order creation

**Out of Scope:**

- OAuth2 / SSO / external identity provider integration
- Payment gateway integration
- Email / SMS notification system
- Full-text search engine integration
- Admin UI or dashboard
- CI/CD pipeline changes
- Load testing and performance benchmarking infrastructure
- Message queue implementation (ActiveMQ)
- API versioning strategy (v2 endpoints)
- Rate limiting (to be handled at API gateway level)
- Batch import/export operations
- Multi-tenancy support
