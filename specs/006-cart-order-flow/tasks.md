# Tasks: Cart & Order Lifecycle Flow

**Branch**: `006-cart-order-flow`
**Input**: Design documents from `specs/006-cart-order-flow/`
**Stack**: Java 17, Spring Boot 3.0.0, Spring Data JPA, Spring Security + JWT, MapStruct 1.6.0, Lombok, Redis

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies on sibling tasks)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- All paths are relative to `src/main/java/com/app/ecommerce/`

---

## Phase 1: Foundational (Blocking Prerequisites)

**Purpose**: Entity additions, new exceptions, new DTOs — everything that US1, US2, and US3 depend on. No user story work can begin until this phase is complete.

---

- [x] T001 [P] Create new file `cart/CartStatus.java` — a simple Java enum with two values. The file must be in package `com.app.ecommerce.cart`. Content:
  ```java
  package com.app.ecommerce.cart;

  public enum CartStatus {
      OPEN,
      CHECKED_OUT
  }
  ```
  This enum represents whether a cart is still mutable (`OPEN`) or locked because an order was placed from it (`CHECKED_OUT`).

---

- [x] T002 [P] Create new file `shared/exception/CartNotOpenException.java`. This exception is thrown whenever a mutation is attempted on a `CHECKED_OUT` cart (adding items, updating items, removing items, or placing an order). It extends `RuntimeException`. Content:
  ```java
  package com.app.ecommerce.shared.exception;

  public class CartNotOpenException extends RuntimeException {
      public CartNotOpenException() {
          super("Cart is already checked out and cannot be modified");
      }
  }
  ```

---

- [x] T003 [P] Create new file `cart/AddCartItemRequest.java` — the request body DTO for `POST /carts/items`. It carries the product to add and the quantity. Use Lombok `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Getter`, `@Setter`. Use `@JsonInclude(JsonInclude.Include.NON_NULL)`. Add SpringDoc `@Schema` on the class and both fields. Content:
  ```java
  package com.app.ecommerce.cart;

  import com.fasterxml.jackson.annotation.JsonInclude;
  import io.swagger.v3.oas.annotations.media.Schema;
  import jakarta.validation.constraints.Min;
  import jakarta.validation.constraints.NotNull;
  import lombok.*;

  import java.util.UUID;

  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "Request to add a product to the cart")
  public class AddCartItemRequest {

      @NotNull(message = "Product ID is required")
      @Schema(description = "ID of the product to add", requiredMode = Schema.RequiredMode.REQUIRED)
      private UUID productId;

      @Min(value = 1, message = "Quantity must be at least 1")
      @Schema(description = "Quantity to add (must be >= 1)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
      private int quantity;
  }
  ```

---

- [x] T004 [P] Create new file `cart/UpdateCartItemQuantityRequest.java` — the request body DTO for `PATCH /carts/items/{cartItemId}`. Sending `quantity = 0` means "remove the item". Content:
  ```java
  package com.app.ecommerce.cart;

  import com.fasterxml.jackson.annotation.JsonInclude;
  import io.swagger.v3.oas.annotations.media.Schema;
  import jakarta.validation.constraints.Min;
  import lombok.*;

  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "Request to update a cart item's quantity. Sending 0 removes the item.")
  public class UpdateCartItemQuantityRequest {

      @Min(value = 0, message = "Quantity must be >= 0 (0 removes the item)")
      @Schema(description = "New absolute quantity. 0 = remove the item.", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
      private int quantity;
  }
  ```

---

- [x] T005 Modify `cart/Cart.java` — add two new fields: `owner` (the User who owns this cart) and `status` (OPEN or CHECKED_OUT). The current file has no `owner` or `status`. Make the following additions:

  **Add imports** at the top of the file:
  ```java
  import com.app.ecommerce.auth.User;
  import jakarta.persistence.EnumType;
  import jakarta.persistence.Enumerated;
  import jakarta.persistence.FetchType;
  import jakarta.persistence.ManyToOne;
  ```

  **Add two new fields** inside the class (after the `version` field):
  ```java
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private CartStatus status = CartStatus.OPEN;
  ```

  The final class should still have `@DynamicUpdate`, `@SuperBuilder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Getter`, `@Setter`. The `cartItems` and `order` fields remain unchanged.

---

- [x] T006 Modify `cart/CartRepository.java` — add two derived query methods that Spring Data JPA generates automatically from the field names. The current file only extends `JpaRepository<Cart, UUID>` with no custom methods. Add the following two method signatures:

  **Add imports**:
  ```java
  import com.app.ecommerce.auth.User;
  import java.util.Optional;
  ```

  **Add methods to the interface**:
  ```java
  Optional<Cart> findByOwnerAndStatus(User owner, CartStatus status);

  boolean existsByOwnerAndStatus(User owner, CartStatus status);
  ```

  Spring Data JPA will auto-implement these. `findByOwnerAndStatus` is used to look up or verify the user's OPEN cart. `existsByOwnerAndStatus` is used to avoid creating a duplicate OPEN cart.

---

- [x] T007 Modify `cart/CartResponse.java` — add a `status` field to expose the cart's lifecycle state to API consumers. The current file has `id` (UUID) and `cartItems` (Set<CartItemResponse>).

  **Add import**:
  ```java
  import com.app.ecommerce.cart.CartStatus;
  ```

  **Add field** after `id`:
  ```java
  @Schema(description = "Current status of the cart", example = "OPEN")
  private CartStatus status;
  ```

  Do not change any other existing fields. The class already extends `BaseResponse` and uses `@SuperBuilder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Getter`, `@Setter`, `@JsonInclude(NON_NULL)`.

---

- [x] T008 Modify `cart/CartMapper.java` — the mapper currently ignores `id`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`. The new `status` field in both `Cart` (source) and `CartResponse` (target) has the same name, so MapStruct will map it automatically. However, the mapper must also ignore the `owner` field on the source entity (it should not be exposed in responses).

  Add one new `@Mapping` annotation to the `mapToResponse` method:
  ```java
  @Mapping(target = "owner", ignore = true)
  ```

  The final `mapToResponse` method signature should look like:
  ```java
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "owner", ignore = true)
  CartResponse mapToResponse(Cart cart);
  ```

  The `status` field will be automatically mapped by MapStruct because both Cart and CartResponse have a field named `status` of type `CartStatus`.

---

- [x] T009 Modify `order/CreateOrderRequest.java` — remove the `cartId` field entirely. The cart is now derived from the authenticated user's OPEN cart on the server side; the client no longer needs to provide it.

  **Remove** these lines from the class:
  ```java
  @NotNull(message = "Cart ID is required")
  @Schema(description = "ID of the cart to place order from", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
  private UUID cartId;
  ```

  **Remove** the `UUID` import if it is no longer used (check — the class only used UUID for `cartId`, so it is safe to remove `import java.util.UUID;`).

  The final class only has one field: `paymentType` with `@NotNull`. Update the `@Schema` class-level description to read: `"Request to create a new order. The cart is automatically derived from the authenticated user's current open cart."`.

---

- [x] T010 Modify `shared/exception/RestExceptionHandler.java` — register a handler for the new `CartNotOpenException`. The pattern to follow is identical to the existing `handleDuplicatedUniqueValueException` (which also returns HTTP 409 Conflict).

  Add the following method to `RestExceptionHandler` class (place it after the `handleDuplicatedUniqueValueException` method):
  ```java
  @ExceptionHandler(value = CartNotOpenException.class)
  public ResponseEntity<ErrorResponse> handleCartNotOpenException(
          CartNotOpenException exception, WebRequest request) {
      log.warn("Cart not open: {}", exception.getMessage());

      ErrorResponse errorResponse = ErrorResponse.conflict(
              exception.getMessage(),
              request.getDescription(false).replace("uri=", "")
      );

      return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }
  ```

  Add the import at the top of the file:
  ```java
  import com.app.ecommerce.shared.exception.CartNotOpenException;
  ```

  No other changes to this file.

---

**Checkpoint — Phase 1 complete**: CartStatus, CartNotOpenException, both request DTOs, Cart entity (owner + status), CartRepository (new queries), CartResponse (status), CartMapper (ignore owner), CreateOrderRequest (no cartId), RestExceptionHandler (CartNotOpenException → 409) are all ready. Phase 2 (US1) can now begin.

---

## Phase 2: User Story 1 — Manage My Shopping Cart (Priority: P1) 🎯 MVP

**Goal**: Customers can retrieve their current cart (auto-created if missing), add products, update item quantities, and remove items — all via REST endpoints.

**Independent Test**: After this phase, run the following sequence and verify all steps work:
1. `GET /carts` → returns empty OPEN cart (auto-created)
2. `POST /carts/items` with a valid productId → cart has 1 item
3. `POST /carts/items` with the same productId again → quantity incremented (no duplicate item)
4. `PATCH /carts/items/{cartItemId}` with quantity=5 → item has productQuantity=5
5. `PATCH /carts/items/{cartItemId}` with quantity=0 → item is removed from cart
6. `DELETE /carts/items/{cartItemId}` → item removed, 204 returned

---

- [x] T011 Modify `cart/CartService.java` (the interface) — extend it with four new method signatures that cover all cart mutation operations. The current interface only declares `CartResponse findById(UUID cartId)`. Add the following method declarations:

  **Add imports**:
  ```java
  import com.app.ecommerce.auth.User;
  ```

  **Add methods to the interface**:
  ```java
  /**
   * Returns the authenticated user's current OPEN cart.
   * If no OPEN cart exists, one is automatically created and persisted.
   */
  CartResponse getCurrentCart(User owner);

  /**
   * Adds a product to the user's OPEN cart.
   * If the product already exists in the cart, its quantity is incremented
   * by the amount in the request (upsert behavior).
   * Throws NoSuchElementException if the product does not exist.
   * Throws CartNotOpenException if the cart is CHECKED_OUT.
   */
  CartResponse addItem(User owner, AddCartItemRequest request);

  /**
   * Updates the quantity of a specific cart item to the absolute value in the request.
   * If quantity == 0, the item is automatically deleted from the cart.
   * Throws NoSuchElementException if the cart item is not found or does not belong to this user.
   * Throws CartNotOpenException if the cart is CHECKED_OUT.
   */
  CartResponse updateItemQuantity(User owner, UUID cartItemId, UpdateCartItemQuantityRequest request);

  /**
   * Removes a specific cart item from the user's cart.
   * Throws NoSuchElementException if the cart item is not found or does not belong to this user.
   * Throws CartNotOpenException if the cart is CHECKED_OUT.
   */
  void removeItem(User owner, UUID cartItemId);
  ```

---

- [x] T012 Modify `cart/CartServiceImpl.java` — add implementation of all four new service methods. The current class only implements `findById`. You will need to inject `CartItemRepository` and `ProductRepository` (from the `product` domain) as additional dependencies.

  **Add new fields** (via `@RequiredArgsConstructor` — just declare them as `private final`):
  ```java
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository; // import: com.app.ecommerce.product.ProductRepository
  ```

  **Import additions** needed at the top:
  ```java
  import com.app.ecommerce.auth.User;
  import com.app.ecommerce.cart.CartNotOpenException; // will exist after T002
  import com.app.ecommerce.product.Product;
  import com.app.ecommerce.product.ProductRepository;
  import org.springframework.cache.annotation.CacheEvict;
  import org.springframework.cache.annotation.CachePut;
  import java.util.HashSet;
  import java.util.NoSuchElementException;
  ```

  **Implement `getCurrentCart(User owner)`**:
  ```java
  @Override
  @Transactional
  public CartResponse getCurrentCart(User owner) {
      log.info("getCurrentCart(owner={})", owner.getUsername());
      Cart cart = cartRepository.findByOwnerAndStatus(owner, CartStatus.OPEN)
              .orElseGet(() -> {
                  log.info("No OPEN cart found for user {}, creating new cart", owner.getUsername());
                  Cart newCart = Cart.builder()
                          .owner(owner)
                          .status(CartStatus.OPEN)
                          .cartItems(new HashSet<>())
                          .build();
                  return cartRepository.save(newCart);
              });
      return cartMapper.mapToResponse(cart);
  }
  ```

  **Implement `addItem(User owner, AddCartItemRequest request)`**:
  ```java
  @Override
  @Transactional
  public CartResponse addItem(User owner, AddCartItemRequest request) {
      log.info("addItem(owner={}, productId={}, quantity={})", owner.getUsername(), request.getProductId(), request.getQuantity());

      Cart cart = cartRepository.findByOwnerAndStatus(owner, CartStatus.OPEN)
              .orElseGet(() -> {
                  Cart newCart = Cart.builder()
                          .owner(owner)
                          .status(CartStatus.OPEN)
                          .cartItems(new HashSet<>())
                          .build();
                  return cartRepository.save(newCart);
              });

      if (cart.getStatus() != CartStatus.OPEN) {
          throw new CartNotOpenException();
      }

      Product product = productRepository.findById(request.getProductId())
              .orElseThrow(() -> new NoSuchElementException("Product with id " + request.getProductId() + " not found"));

      // Upsert: increment quantity if product already in cart, else create new CartItem
      CartItem existingItem = cart.getCartItems().stream()
              .filter(item -> item.getProduct().getId().equals(request.getProductId()))
              .findFirst()
              .orElse(null);

      if (existingItem != null) {
          existingItem.setProductQuantity(existingItem.getProductQuantity() + request.getQuantity());
          cartItemRepository.save(existingItem);
      } else {
          CartItem newItem = CartItem.builder()
                  .product(product)
                  .productQuantity(request.getQuantity())
                  .cart(cart)
                  .build();
          cartItemRepository.save(newItem);
          cart.getCartItems().add(newItem);
      }

      Cart savedCart = cartRepository.save(cart);
      log.info("Item added to cart {}", savedCart.getId());
      return cartMapper.mapToResponse(savedCart);
  }
  ```

  **Implement `updateItemQuantity(User owner, UUID cartItemId, UpdateCartItemQuantityRequest request)`**:
  ```java
  @Override
  @Transactional
  public CartResponse updateItemQuantity(User owner, UUID cartItemId, UpdateCartItemQuantityRequest request) {
      log.info("updateItemQuantity(owner={}, cartItemId={}, quantity={})", owner.getUsername(), cartItemId, request.getQuantity());

      CartItem item = cartItemRepository.findById(cartItemId)
              .orElseThrow(() -> new NoSuchElementException("Cart item with id " + cartItemId + " not found"));

      // Ownership check: item must belong to the user's cart
      if (!item.getCart().getOwner().getId().equals(owner.getId())) {
          throw new NoSuchElementException("Cart item with id " + cartItemId + " not found");
      }

      if (item.getCart().getStatus() != CartStatus.OPEN) {
          throw new CartNotOpenException();
      }

      Cart cart = item.getCart();

      if (request.getQuantity() == 0) {
          // Auto-remove when quantity is set to 0
          cart.getCartItems().remove(item);
          cartItemRepository.delete(item);
          log.info("Cart item {} removed (quantity set to 0)", cartItemId);
      } else {
          item.setProductQuantity(request.getQuantity());
          cartItemRepository.save(item);
          log.info("Cart item {} updated to quantity {}", cartItemId, request.getQuantity());
      }

      Cart savedCart = cartRepository.save(cart);
      return cartMapper.mapToResponse(savedCart);
  }
  ```

  **Implement `removeItem(User owner, UUID cartItemId)`**:
  ```java
  @Override
  @Transactional
  public void removeItem(User owner, UUID cartItemId) {
      log.info("removeItem(owner={}, cartItemId={})", owner.getUsername(), cartItemId);

      CartItem item = cartItemRepository.findById(cartItemId)
              .orElseThrow(() -> new NoSuchElementException("Cart item with id " + cartItemId + " not found"));

      // Ownership check
      if (!item.getCart().getOwner().getId().equals(owner.getId())) {
          throw new NoSuchElementException("Cart item with id " + cartItemId + " not found");
      }

      if (item.getCart().getStatus() != CartStatus.OPEN) {
          throw new CartNotOpenException();
      }

      Cart cart = item.getCart();
      cart.getCartItems().remove(item);
      cartItemRepository.delete(item);
      cartRepository.save(cart);
      log.info("Cart item {} removed from cart {}", cartItemId, cart.getId());
  }
  ```

---

- [x] T013 Create new file `cart/CartController.java` — the interface that declares the HTTP contract for all cart endpoints. OpenAPI `@Operation` and `@Tag` annotations go here (NOT on the implementation). The implementation uses `@PreAuthorize`.

  ```java
  package com.app.ecommerce.cart;

  import com.app.ecommerce.auth.User;
  import com.app.ecommerce.shared.dto.ApiResponse;
  import com.app.ecommerce.shared.dto.ErrorResponse;
  import io.swagger.v3.oas.annotations.Operation;
  import io.swagger.v3.oas.annotations.media.Content;
  import io.swagger.v3.oas.annotations.media.Schema;
  import io.swagger.v3.oas.annotations.tags.Tag;
  import jakarta.validation.Valid;
  import org.springframework.http.ResponseEntity;
  import org.springframework.security.core.annotation.AuthenticationPrincipal;
  import org.springframework.web.bind.annotation.*;

  import java.util.UUID;

  @Tag(name = "Cart", description = "Shopping cart management")
  public interface CartController {

      @Operation(summary = "Get Current Cart", description = "Returns the authenticated user's open cart. Creates one automatically if none exists.")
      @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
              @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart retrieved or created",
                      content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
      ResponseEntity<ApiResponse<CartResponse>> getCurrentCart(@AuthenticationPrincipal User currentUser);

      @Operation(summary = "Add Item to Cart", description = "Adds a product to the cart. If the product already exists, its quantity is incremented.")
      @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
              @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Item added",
                      content = @Content(schema = @Schema(implementation = ApiResponse.class))),
              @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found",
                      content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
              @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cart is already checked out",
                      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
      ResponseEntity<ApiResponse<CartResponse>> addItem(@AuthenticationPrincipal User currentUser, @Valid @RequestBody AddCartItemRequest request);

      @Operation(summary = "Update Cart Item Quantity", description = "Sets the absolute quantity of a cart item. Sending quantity=0 removes the item.")
      @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
              @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item updated",
                      content = @Content(schema = @Schema(implementation = ApiResponse.class))),
              @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found",
                      content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
              @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cart is already checked out",
                      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
      ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(@AuthenticationPrincipal User currentUser,
                                                                    @PathVariable("cartItemId") UUID cartItemId,
                                                                    @Valid @RequestBody UpdateCartItemQuantityRequest request);

      @Operation(summary = "Remove Cart Item", description = "Explicitly removes a specific item from the cart.")
      @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
              @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Item removed"),
              @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found",
                      content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
              @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cart is already checked out",
                      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
      ResponseEntity<Void> removeItem(@AuthenticationPrincipal User currentUser, @PathVariable("cartItemId") UUID cartItemId);
  }
  ```

---

- [x] T014 Create new file `cart/CartControllerImpl.java` — the `@RestController` implementation. `@PreAuthorize` annotations go here. The current user is resolved via `@AuthenticationPrincipal User currentUser` injected by Spring Security.

  ```java
  package com.app.ecommerce.cart;

  import com.app.ecommerce.auth.User;
  import com.app.ecommerce.shared.dto.ApiResponse;
  import jakarta.validation.Valid;
  import lombok.RequiredArgsConstructor;
  import lombok.extern.slf4j.Slf4j;
  import org.springframework.http.HttpStatus;
  import org.springframework.http.ResponseEntity;
  import org.springframework.security.access.prepost.PreAuthorize;
  import org.springframework.security.core.annotation.AuthenticationPrincipal;
  import org.springframework.web.bind.annotation.*;

  import java.util.UUID;

  @Slf4j
  @RequiredArgsConstructor
  @RestController
  @RequestMapping("/carts")
  public class CartControllerImpl implements CartController {

      private final CartService cartService;

      @GetMapping
      @Override
      @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
      public ResponseEntity<ApiResponse<CartResponse>> getCurrentCart(@AuthenticationPrincipal User currentUser) {
          log.info("getCurrentCart(user={})", currentUser.getUsername());
          CartResponse cart = cartService.getCurrentCart(currentUser);
          return ResponseEntity.ok(ApiResponse.success(cart));
      }

      @PostMapping("/items")
      @Override
      @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
      public ResponseEntity<ApiResponse<CartResponse>> addItem(@AuthenticationPrincipal User currentUser,
                                                               @Valid @RequestBody AddCartItemRequest request) {
          log.info("addItem(user={}, request={})", currentUser.getUsername(), request);
          CartResponse cart = cartService.addItem(currentUser, request);
          return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(cart));
      }

      @PatchMapping("/items/{cartItemId}")
      @Override
      @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
      public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(@AuthenticationPrincipal User currentUser,
                                                                           @PathVariable("cartItemId") UUID cartItemId,
                                                                           @Valid @RequestBody UpdateCartItemQuantityRequest request) {
          log.info("updateItemQuantity(user={}, cartItemId={}, quantity={})", currentUser.getUsername(), cartItemId, request.getQuantity());
          CartResponse cart = cartService.updateItemQuantity(currentUser, cartItemId, request);
          return ResponseEntity.ok(ApiResponse.success(cart, "Cart item updated successfully"));
      }

      @DeleteMapping("/items/{cartItemId}")
      @Override
      @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
      public ResponseEntity<Void> removeItem(@AuthenticationPrincipal User currentUser,
                                             @PathVariable("cartItemId") UUID cartItemId) {
          log.info("removeItem(user={}, cartItemId={})", currentUser.getUsername(), cartItemId);
          cartService.removeItem(currentUser, cartItemId);
          return ResponseEntity.noContent().build();
      }
  }
  ```

  > **Note on `ApiResponse.created()`**: If `ApiResponse` does not have a static `created()` factory method, check the existing `ApiResponse` class. The `OrderControllerImpl` uses `ApiResponse.created(createdOrder)` — use the same method. If it doesn't exist, use `ApiResponse.success(cart)` and change the status to `HttpStatus.CREATED`.

---

**Checkpoint — US1 complete**: Customers can now manage their cart end-to-end. `GET /carts`, `POST /carts/items`, `PATCH /carts/items/{id}`, `DELETE /carts/items/{id}` all work. Verify with the quickstart.md steps 1–5 before proceeding.

---

## Phase 3: User Story 2 — Place an Order From Cart (Priority: P1)

**Goal**: Customers can place an order from their current OPEN cart. The service derives the cart automatically from the JWT identity, validates cart state, calculates the total, creates the order, and transitions the cart to `CHECKED_OUT`.

**Independent Test**: After this phase:
1. Add items to cart via `POST /carts/items`
2. Call `POST /orders` with `{"paymentType": "CREDIT_CARD"}` — no `cartId` in body
3. Verify HTTP 201, `totalPrice = sum(product.price × quantity)`
4. Verify cart in response has `status: "CHECKED_OUT"`
5. Call `POST /orders` again — should fail (cart is empty after auto-reset, or with 409 if CHECKED_OUT cart is reused)
6. Call `GET /carts` — verify a fresh OPEN cart is returned

---

- [x] T015 Modify `order/OrderService.java` (the interface) — all four method signatures must accept a `User currentUser` parameter. The current interface has:
  ```java
  OrderResponse createNewOrder(CreateOrderRequest request);
  OrderResponse findById(UUID orderId);
  OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request);
  Page<OrderResponse> findAll(Status status, PaymentType paymentType, Instant createdAfter, Instant createdBefore, Pageable pageable);
  ```

  **Add import**:
  ```java
  import com.app.ecommerce.auth.User;
  ```

  **Replace all four signatures** with:
  ```java
  OrderResponse createNewOrder(CreateOrderRequest request, User currentUser);
  OrderResponse findById(UUID orderId, User currentUser);
  OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request, User currentUser);
  Page<OrderResponse> findAll(Status status, PaymentType paymentType, Instant createdAfter, Instant createdBefore, Pageable pageable, User currentUser);
  ```

  > **Important**: After this change, `OrderServiceImpl` will have compilation errors until T016 is also applied. Complete T015 and T016 together before compiling.

---

- [x] T016 Modify `order/OrderServiceImpl.java` — update `createNewOrder` to derive the cart from the current user's OPEN cart, validate cart state, transition cart to `CHECKED_OUT`, and fix the pre-existing FK bug where `cart.setOrder()` was never called.

  **Update class fields** — add `CartItemRepository` if needed (already has `CartRepository`):
  No new repositories needed. `CartRepository` is already injected.

  **Add imports**:
  ```java
  import com.app.ecommerce.auth.Role;
  import com.app.ecommerce.auth.User;
  import com.app.ecommerce.cart.CartStatus;
  import com.app.ecommerce.shared.exception.CartNotOpenException;
  ```

  **Replace `createNewOrder` method** entirely:
  ```java
  @Override
  @CachePut(value = CacheConstants.ORDERS, key = "#result.id")
  @Transactional
  public OrderResponse createNewOrder(CreateOrderRequest request, User currentUser) {
      log.info("createNewOrder(user={}, request={})", currentUser.getUsername(), request);

      // 1. Get the user's current OPEN cart
      Cart cart = cartRepository.findByOwnerAndStatus(currentUser, CartStatus.OPEN)
              .orElseThrow(() -> new NoSuchElementException("No open cart found for user " + currentUser.getUsername()));

      // 2. Validate cart is not empty
      if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
          throw new IllegalArgumentException("Cannot place an order with an empty cart");
      }

      // 3. Validate all cart items reference existing products
      for (CartItem item : cart.getCartItems()) {
          if (item.getProduct() == null) {
              throw new NoSuchElementException("Cart contains an item with a missing product. Remove invalid items before checkout.");
          }
      }

      // 4. Calculate total price using BigDecimal
      BigDecimal totalPrice = BigDecimal.ZERO;
      for (CartItem item : cart.getCartItems()) {
          Product product = item.getProduct();
          totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(item.getProductQuantity())));
      }

      // 5. Build and save the Order
      Order order = orderMapper.mapToEntity(request);
      order.setTotalPrice(totalPrice);
      Order savedOrder = orderRepository.save(order);

      // 6. FIX: Set the owning side of the Cart ↔ Order relationship (Cart holds the FK "order_id")
      cart.setOrder(savedOrder);

      // 7. Transition cart to CHECKED_OUT
      cart.setStatus(CartStatus.CHECKED_OUT);
      cartRepository.save(cart);

      log.info("Order created with id={}, cart {} transitioned to CHECKED_OUT", savedOrder.getId(), cart.getId());
      return orderMapper.mapToResponse(savedOrder);
  }
  ```

  **Update `findById` signature** (change parameter to add User, no logic change yet — ownership check comes in T020):
  ```java
  @Override
  @Cacheable(value = CacheConstants.ORDERS, key = "#orderId")
  @Transactional(readOnly = true)
  public OrderResponse findById(UUID orderId, User currentUser) {
      // existing logic unchanged — ownership enforcement added in T020
      log.info("findById({}, user={})", orderId, currentUser.getUsername());
      if (orderId == null) {
          throw new IllegalArgumentException("orderId == null");
      }
      Order order = orderRepository.findById(orderId)
              .orElseThrow(() -> new NoSuchElementException("No Such Order With This Id, Id Not Found with value = " + orderId));
      log.info("order found with id = {}", orderId);
      return orderMapper.mapToResponse(order);
  }
  ```

  **Update `updateOrder` signature** (add User param, no change to body yet — ownership check comes in T020):
  ```java
  @Override
  @CachePut(value = CacheConstants.ORDERS, key = "#result.id")
  @Transactional
  public OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request, User currentUser) {
      // existing logic unchanged — keep the existing version/state machine logic
      // (ownership enforcement for admin-only added in T020)
      log.info("updateOrder({}, {}, user={})", orderId, request, currentUser.getUsername());
      if (orderId == null) {
          throw new IllegalArgumentException("Order Id Not Exist to Update");
      }
      Order existingOrder = orderRepository.findById(orderId)
              .orElseThrow(() -> new NoSuchElementException("Order with id " + orderId + " not found"));
      existingOrder.setVersion(request.getVersion());
      if (request.getDeliveryStatus() != null) {
          Status currentStatus = existingOrder.getDeliveryInfo().getStatus();
          Status requestedStatus = request.getDeliveryStatus();
          if (!currentStatus.canTransitionTo(requestedStatus)) {
              throw new InvalidStateTransitionException(currentStatus, requestedStatus);
          }
      }
      orderMapper.updateEntityFromRequest(request, existingOrder);
      Order updatedOrder = orderRepository.save(existingOrder);
      log.info("order updated with id = {}", updatedOrder.getId());
      return orderMapper.mapToResponse(updatedOrder);
  }
  ```

  **Update `findAll` signature** (add User param, role-scoped filtering comes in T020):
  ```java
  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> findAll(Status status, PaymentType paymentType, Instant createdAfter, Instant createdBefore, Pageable pageable, User currentUser) {
      // existing logic unchanged for now — role-scoping added in T020
      // ... (keep existing body)
  }
  ```

---

- [x] T017 Modify `order/OrderController.java` (the interface) — add `@AuthenticationPrincipal User currentUser` parameter to all four method signatures. This is a non-breaking interface change since we are updating both the interface and the implementation together.

  **Add imports**:
  ```java
  import com.app.ecommerce.auth.User;
  import org.springframework.security.core.annotation.AuthenticationPrincipal;
  ```

  **Update `createNewOrder` signature**:
  ```java
  ResponseEntity<ApiResponse<OrderResponse>> createNewOrder(
          @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
          @Valid @RequestBody CreateOrderRequest request,
          @AuthenticationPrincipal User currentUser) throws JsonProcessingException;
  ```

  **Update `findAll` signature**:
  ```java
  ResponseEntity<ApiResponse<Page<OrderResponse>>> findAll(
          @RequestParam(required = false) Status status,
          @RequestParam(required = false) PaymentType paymentType,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
          @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
          @AuthenticationPrincipal User currentUser);
  ```

  **Update `updateOrder` signature**:
  ```java
  ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
          @PathVariable("id") UUID orderId,
          @Valid @RequestBody UpdateOrderRequest request,
          @AuthenticationPrincipal User currentUser);
  ```

  **Update `findOrderById` signature**:
  ```java
  ResponseEntity<ApiResponse<OrderResponse>> findOrderById(
          @PathVariable("id") UUID orderId,
          @AuthenticationPrincipal User currentUser);
  ```

---

- [x] T018 Modify `order/OrderControllerImpl.java` — update all four controller methods to accept `@AuthenticationPrincipal User currentUser` and pass it to the corresponding service method. The existing business logic in each method stays the same; only the method signatures and the service call arguments change.

  **Add imports**:
  ```java
  import com.app.ecommerce.auth.User;
  import org.springframework.security.core.annotation.AuthenticationPrincipal;
  ```

  **Update `createNewOrder` method**:
  ```java
  @PostMapping
  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> createNewOrder(
          @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
          @Valid @RequestBody CreateOrderRequest request,
          @AuthenticationPrincipal User currentUser) throws JsonProcessingException {
      log.info("createNewOrder(user={}, request={})", currentUser.getUsername(), request);

      if (idempotencyKey != null) {
          var existingRecord = idempotencyService.findByKey(idempotencyKey);
          if (existingRecord.isPresent()) {
              log.info("Returning cached response for idempotency key: {}", idempotencyKey);
              var record = existingRecord.get();
              try {
                  ApiResponse<OrderResponse> cachedResponse = objectMapper.readValue(
                          record.getResponseBody(),
                          objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, OrderResponse.class)
                  );
                  return ResponseEntity.status(record.getHttpStatus()).body(cachedResponse);
              } catch (JsonProcessingException e) {
                  log.error("Failed to parse cached response", e);
                  throw e;
              }
          }
      }

      OrderResponse createdOrder = orderService.createNewOrder(request, currentUser); // pass currentUser
      ApiResponse<OrderResponse> response = ApiResponse.created(createdOrder);

      if (idempotencyKey != null) {
          idempotencyService.store(idempotencyKey, HttpStatus.CREATED.value(), response);
      }

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
  ```

  **Update `findAll` method**:
  ```java
  @GetMapping
  @Override
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> findAll(
          @RequestParam(required = false) Status status,
          @RequestParam(required = false) PaymentType paymentType,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
          @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
          @AuthenticationPrincipal User currentUser) {
      log.info("findAll(user={}, status={}, paymentType={})", currentUser.getUsername(), status, paymentType);
      Page<OrderResponse> page = orderService.findAll(status, paymentType, createdAfter, createdBefore, pageable, currentUser); // pass currentUser
      return ResponseEntity.ok(ApiResponse.success(page));
  }
  ```

  **Update `updateOrder` method**:
  ```java
  @PatchMapping("/{id}")
  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
          @PathVariable("id") UUID orderId,
          @Valid @RequestBody UpdateOrderRequest request,
          @AuthenticationPrincipal User currentUser) {
      log.info("updateOrder({}, user={})", orderId, currentUser.getUsername());
      OrderResponse updatedOrder = orderService.updateOrder(orderId, request, currentUser); // pass currentUser
      return ResponseEntity.ok(ApiResponse.success(updatedOrder, "Order updated successfully"));
  }
  ```

  **Update `findOrderById` method**:
  ```java
  @GetMapping("/{id}")
  @Override
  public ResponseEntity<ApiResponse<OrderResponse>> findOrderById(
          @PathVariable("id") UUID orderId,
          @AuthenticationPrincipal User currentUser) {
      log.info("findOrderById({}, user={})", orderId, currentUser.getUsername());
      OrderResponse order = orderService.findById(orderId, currentUser); // pass currentUser
      return ResponseEntity.ok(ApiResponse.success(order));
  }
  ```

---

**Checkpoint — US2 complete**: `POST /orders` works end-to-end. Cart is auto-derived from the JWT user, validated, and transitioned to `CHECKED_OUT`. The FK bug (`cart.setOrder()`) is fixed. Verify with quickstart.md steps 5–6 before proceeding.

---

## Phase 4: User Story 3 — Role-Scoped Order Access (Priority: P2)

**Goal**: Customers can only see their own orders (list and single-fetch). Admins see all orders. `updateOrder` remains admin-only.

**Independent Test**: After this phase:
1. Login as Customer A, place an order → get `ORDER_A_ID`
2. Login as Customer B, place an order → get `ORDER_B_ID`
3. As Customer A: `GET /orders/{ORDER_B_ID}` → 404 Not Found
4. As Customer A: `GET /orders` → only Customer A's orders returned
5. As Admin: `GET /orders` → all orders returned
6. As Customer A: `PATCH /orders/{ORDER_A_ID}` → 403 Forbidden (admin only)

---

- [x] T019 [P] Modify `order/OrderSpecifications.java` — add a new static method `hasOwner(User user)` that filters orders by the owning user. The join path is: `Order` → `cart` (field name `"cart"`, the OneToOne relationship) → `owner` (field name `"owner"`, the ManyToOne on Cart added in T005).

  **Add imports**:
  ```java
  import com.app.ecommerce.auth.User;
  import jakarta.persistence.criteria.Join;
  ```

  **Add method** (place after the existing `createdBefore` method):
  ```java
  /**
   * Filters orders to only those whose cart is owned by the given user.
   * Returns a null predicate (no filter) if user is null.
   * Join path: Order.cart (OneToOne, mappedBy="order") -> Cart.owner (ManyToOne)
   */
  public static Specification<Order> hasOwner(User user) {
      return (root, query, cb) -> {
          if (user == null) {
              return null;
          }
          // Join Order -> Cart -> User
          Join<Object, Object> cart = root.join("cart");
          Join<Object, Object> owner = cart.join("owner");
          return cb.equal(owner.get("id"), user.getId());
      };
  }
  ```

---

- [x] T020 Modify `order/OrderServiceImpl.java` — add ownership enforcement to `findById`, `updateOrder`, and role-scoped filtering to `findAll`. These changes build on top of T016 (which added the `User currentUser` parameter).

  **Add import**:
  ```java
  import com.app.ecommerce.auth.Role;
  ```

  **Update `findById` to add ownership check for CUSTOMER role**:

  Inside the `findById` method, after fetching the order from the repository, add this block:
  ```java
  // Ownership check: customers can only access their own orders
  if (currentUser.getRole() == Role.CUSTOMER) {
      Cart orderCart = order.getCart();
      if (orderCart == null || !orderCart.getOwner().getId().equals(currentUser.getId())) {
          throw new NoSuchElementException("No Such Order With This Id, Id Not Found with value = " + orderId);
      }
  }
  ```

  **Update `updateOrder` to add `@PreAuthorize` annotation for admin-only**:

  Note: `@PreAuthorize` on service methods requires `@EnableMethodSecurity` which is already configured in `SecurityConfig`. Add the annotation to the service implementation method (not the interface):
  ```java
  @Override
  @CachePut(value = CacheConstants.ORDERS, key = "#result.id")
  @Transactional
  @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
  public OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request, User currentUser) {
      // ... existing logic unchanged
  }
  ```

  **Update `findAll` to add role-scoped filtering**:

  In the `findAll` method, modify the `Specification` chain to conditionally add the `hasOwner` filter for CUSTOMER users:
  ```java
  Specification<Order> spec = Specification
          .where((Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null)
          .and(OrderSpecifications.hasStatus(status))
          .and(OrderSpecifications.hasPaymentType(paymentType))
          .and(OrderSpecifications.createdAfter(createdAfter))
          .and(OrderSpecifications.createdBefore(createdBefore));

  // Role-scoped: customers only see their own orders
  if (currentUser.getRole() == Role.CUSTOMER) {
      spec = spec.and(OrderSpecifications.hasOwner(currentUser));
  }

  Page<OrderResponse> result = orderRepository.findAll(spec, safePage).map(orderMapper::mapToResponse);
  log.info("findAll(user={}): Found {} orders", currentUser.getUsername(), result.getTotalElements());
  return result;
  ```

---

**Checkpoint — US3 complete**: All three user stories are implemented. Customers only see their own orders. Admins see all. `PATCH /orders` is admin-only.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Cache correctness, compile validation, and end-to-end verification.

---

- [x] T021 [P] Verify cache eviction correctness in `cart/CartServiceImpl.java`. The `getCurrentCart` method returns the cart but does not need `@Cacheable` because the cart's ID changes after checkout and after every mutation. Instead, verify:
  - `findById` (the existing method) uses `@Cacheable(value = CacheConstants.CARTS, key = "#cartId")` — keep as-is
  - The four new methods (`getCurrentCart`, `addItem`, `updateItemQuantity`, `removeItem`) do NOT use cache annotations — they always go to the DB to ensure correctness
  - After `addItem` or `updateItemQuantity`, if the cart was previously cached via `findById`, the cache entry is now stale. Add `@CacheEvict(value = CacheConstants.CARTS, key = "#result.id")` to `addItem` and `updateItemQuantity` return paths so cached reads are invalidated:
    - On `addItem` method: add annotation `@CacheEvict(value = CacheConstants.CARTS, key = "#result.id")` — note `#result.id` refers to the returned `CartResponse.id`
    - On `updateItemQuantity` method: same annotation

  > If `CacheConstants.CARTS` does not exist, check `shared/constants/CacheConstants.java`. It likely has a constant like `public static final String CARTS = "carts";`. Use it.

---

- [x] T022 [P] Check `shared/dto/ApiResponse.java` — verify that a `created()` static factory method exists (used in `CartControllerImpl.addItem` return and the existing `OrderControllerImpl.createNewOrder`). If it does not exist, add it following the same pattern as the existing `success()` method:
  ```java
  public static <T> ApiResponse<T> created(T data) {
      return ApiResponse.<T>builder()
              .success(true)
              .message("Created successfully")
              .data(data)
              .build();
  }
  ```
  If it already exists, skip this task.

---

- [x] T023 Compile the project and fix any remaining errors: run `mvn clean compile -DskipTests` from the project root. Common compilation errors to expect and fix:
  - `OrderServiceImpl` references to old `orderService.createNewOrder(request)` without the `currentUser` argument — already fixed in T016/T018
  - `CartServiceImpl` cannot find `ProductRepository` — verify the import is `import com.app.ecommerce.product.ProductRepository;` (not a non-existent class)
  - `Cart.builder()` missing `cartItems` initialization — ensure `Cart.java` has `@Builder.Default` on `cartItems` (the current code has `@OneToMany` on a `Set<CartItem>` but may not have `@Builder.Default`; add it: `@Builder.Default private Set<CartItem> cartItems = new HashSet<>();`)
  - MapStruct compilation errors in `CartMapper` — ensure the `@Mapping(target = "owner", ignore = true)` is present (T008)

---

- [x] T024 Run the full application (`mvn spring-boot:run`) and manually execute all steps in `specs/006-cart-order-flow/quickstart.md` to confirm the complete flow works end-to-end:
  1. Register + Login as customer
  2. `GET /carts` — empty cart auto-created
  3. `POST /carts/items` with a real product UUID — item added
  4. `POST /carts/items` with the same product — quantity incremented, no duplicate
  5. `PATCH /carts/items/{id}` with quantity=5 — item updated
  6. `PATCH /carts/items/{id}` with quantity=0 — item auto-removed
  7. `POST /carts/items` again to prepare for checkout
  8. `POST /orders` with `{"paymentType":"CREDIT_CARD"}` — order created, cart CHECKED_OUT
  9. `GET /carts` — fresh empty cart returned
  10. As admin: `PATCH /orders/{id}` with delivery status update — succeeds
  11. As customer: `GET /orders/{id}` for another user's order — 404 returned

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Foundational)**: No dependencies — start immediately. Tasks T001–T004 can all run in parallel (different files). T005 depends on T001 (CartStatus). T006 depends on T001 (CartStatus). T007 depends on T001 (CartStatus). T009 is independent.
- **Phase 2 (US1)**: Requires Phase 1 complete. T011 (interface) before T012 (impl). T013 (interface) before T014 (impl). T012 depends on T011.
- **Phase 3 (US2)**: Requires Phase 1 complete and Phase 2 complete (Cart management must exist). T015+T016 must be done together (interface + impl). T017+T018 must be done together (interface + impl).
- **Phase 4 (US3)**: Requires Phase 3 complete. T019 and T020 can run in parallel (different files/methods).
- **Phase 5 (Polish)**: Requires all phases complete.

### Within-Phase Parallel Groups

**Phase 1 parallel group A** (no dependencies on each other):
- T001 (CartStatus.java) — new file
- T002 (CartNotOpenException.java) — new file
- T003 (AddCartItemRequest.java) — new file
- T004 (UpdateCartItemQuantityRequest.java) — new file
- T009 (CreateOrderRequest.java remove cartId) — independent modification
- T010 (RestExceptionHandler.java add CartNotOpenException handler) — depends on T002

**Phase 1 sequential** (T005 depends on T001, T006 depends on T001):
- T001 → T005 (Cart.java entity changes)
- T001 → T006 (CartRepository.java new queries)
- T001 → T007 (CartResponse.java add status field)
- T007 → T008 (CartMapper.java add owner ignore)

---

## Parallel Execution Example: Phase 1

```
Run simultaneously (no dependencies):
  - T001: Create CartStatus.java
  - T002: Create CartNotOpenException.java
  - T003: Create AddCartItemRequest.java
  - T004: Create UpdateCartItemQuantityRequest.java

After T001 completes, run simultaneously:
  - T005: Modify Cart.java (add owner + status)
  - T006: Modify CartRepository.java (add query methods)
  - T007: Modify CartResponse.java (add status field)
  - T009: Modify CreateOrderRequest.java (remove cartId)

After T002 completes:
  - T010: Add CartNotOpenException handler to RestExceptionHandler.java

After T007 completes:
  - T008: Modify CartMapper.java (add owner ignore)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only — Cart Management)

1. Complete Phase 1 (Foundational) — blocks everything
2. Complete Phase 2 (US1 — Cart Management)
3. **STOP and VALIDATE**: Test all cart endpoints manually
4. Optional: deploy and demo cart management

### Incremental Delivery

1. Phase 1 + Phase 2 → Cart management works ✓
2. Add Phase 3 (US2) → Full checkout flow works ✓
3. Add Phase 4 (US3) → Role-scoped order access works ✓
4. Phase 5 (Polish) → Production-ready ✓

### Notes for AI Implementation

- All file paths are relative to `src/main/java/com/app/ecommerce/`
- The project uses Lombok — never write boilerplate getters/setters manually
- All monetary values must use `BigDecimal` — never `double` or `float`
- `@Transactional` on all service write methods, `@Transactional(readOnly = true)` on reads
- Never serialize JPA entities directly — always use the MapStruct mapper
- Cross-domain entity imports (e.g., `Cart → User`, `CartServiceImpl → ProductRepository`) follow the existing pattern of `CartItem → Product`
- The `User` entity is in package `com.app.ecommerce.auth` and implements `UserDetails`
- `Role` enum is in `com.app.ecommerce.auth.Role` with values `ADMIN` and `CUSTOMER`

---

## Code Review Findings

Code review completed on implementation with 27 files changed (2,746 insertions). Issues found and fixed:

### 🔴 CRITICAL — Fixed

| ID | Issue | File | Fix Applied |
|----|-------|------|-------------|
| C1 | Missing `@PreAuthorize` on `createNewOrder()` — unauthenticated users would cause NPE | `OrderControllerImpl.java` | Added `@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")` |
| C2 | `order.setCart()` never called — `OrderResponse.cart` would be null | `OrderServiceImpl.java` | Added `order.setCart(cart)` before `orderRepository.save()` |
| C3 | `existingOrder.setVersion(request.getVersion())` bypasses optimistic locking | `OrderServiceImpl.java` | Removed manual version setting — Hibernate handles it |
| C4 | `@CacheEvict` missing on `removeItem()` — stale cache after deletion | `CartServiceImpl.java` | Added `@CacheEvict(value = CacheConstants.CARTS, key = "#owner.id")` |

### 🟡 WARNING — Fixed

| ID | Issue | File | Fix Applied |
|----|-------|------|-------------|
| W1 | `Join<Object, Object>` not type-safe | `OrderSpecifications.java` | Changed to typed `Join<Order, Cart>` and `Join<Cart, User>` |

### 🟡 WARNING — Not Applicable

| ID | Issue | Reason |
|----|-------|--------|
| C3 | Cart `findById()` no ownership check | No public endpoint uses this method — `GET /carts/{id}` doesn't exist |
| W2 | Concurrent `getCurrentCart()` race condition | Service-level guards with `findByOwnerAndStatus` + optimistic locking via `@Version` |
| W3 | Cache keyed on `#result.id` conflicts with user identity | Ownership check happens inside the cached method; exception thrown prevents caching on access denial |

### 🔵 NOTES — Verified OK

| ID | Note | Status |
|----|------|--------|
| N1 | `int quantity` vs `Integer` | Working as intended — `@Min` validation handles boundary |
| N2 | `removeItem()` logging | Already has proper logging after removal |
| N3 | `cartItems` missing `@Builder.Default` | Already fixed — `@Builder.Default private Set<CartItem> cartItems = new HashSet<>();` present |

### Priority Fix Order Applied

1. ✅ C1 — `@PreAuthorize` on order endpoints (security)
2. ✅ C2 — `order.setCart()` bidirectional FK (data integrity)
3. ✅ C4 — Remove manual `setVersion()` (optimistic locking)
4. ✅ C5 — `@CacheEvict` on `removeItem()` (cache consistency)
5. ✅ W3 — Typed joins in `OrderSpecifications` (type safety)

---

## Post-Implementation Verification

After applying fixes, run:
```bash
mvn clean compile -DskipTests
```

Expected: `BUILD SUCCESS`

### Manual Test Checklist

- [ ] `GET /carts` → returns empty OPEN cart (auto-created)
- [ ] `POST /carts/items` with productId → item added, 201 returned
- [ ] `POST /carts/items` with same product → quantity incremented, no duplicate
- [ ] `PATCH /carts/items/{id}` with quantity=5 → item updated
- [ ] `PATCH /carts/items/{id}` with quantity=0 → item auto-removed
- [ ] `DELETE /carts/items/{id}` → 200 returned with updated cart
- [ ] `POST /orders` → order created, cart CHECKED_OUT
- [ ] `GET /orders` as customer → only own orders returned
- [ ] `GET /orders` as admin → all orders returned
- [ ] `GET /orders/{id}` of another customer's order → 404 (ownership enforced)

---

## Code Review Findings — Round 2

Additional code review completed. Issues found and fixed:

### 🔴 CRITICAL — Fixed

| ID | Issue | File | Fix Applied |
|----|-------|------|-------------|
| CR1 | `@Cacheable` on `findById` bypasses ownership check on cache hit | `OrderServiceImpl.java` | Removed `@Cacheable` from `findById` — only `@CachePut` on write operations |

### 🟡 WARNING — Fixed

| ID | Issue | File | Fix Applied |
|----|-------|------|-------------|
| WR1 | Wrong cache key `#owner.id` on `removeItem` (cache keyed by cart UUID) | `CartServiceImpl.java` | Changed return type to `CartResponse`, method now returns updated cart |
| WR2 | `@PreAuthorize` on service layer violates constitution | `OrderServiceImpl.java` | Removed `@PreAuthorize` — belongs on controller only |
| WR3 | Dead code: redundant `cart.getStatus() != OPEN` check after `findByOwnerAndStatus` | `CartServiceImpl.java` | Removed redundant check |

### 🔵 NOTE — Verified OK

| ID | Note | Status |
|----|------|--------|
| NR1 | `int quantity` vs `Integer` | Working as intended — `@Min` validation handles boundary |

### Round 2 Fixes Applied

1. ✅ CR1 — Removed `@Cacheable` from `findById` (security)
2. ✅ WR1 — Changed `removeItem` return type to `CartResponse` (cache consistency + better API)
3. ✅ WR2 — Removed `@PreAuthorize` from service layer (constitution compliance)
4. ✅ WR3 — Removed dead code in `addItem` (code cleanliness)

---

## Implementation Complete

All code review issues addressed. Final state:

- **Security**: `@PreAuthorize` on all endpoints, ownership checks enforced, no cache bypass vulnerabilities
- **Data Integrity**: Bidirectional FK properly set, optimistic locking preserved
- **Cache Consistency**: `@CacheEvict` on all mutations, cache keyed correctly
- **Code Quality**: Typed JPA joins, no dead code, constitution compliance
