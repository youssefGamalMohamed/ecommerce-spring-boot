# Quickstart: JPA Search with Pagination, Filtering, and Sorting

**Branch**: `003-jpa-search-pagination` | **Date**: 2026-03-17

> **REST Best Practice**: Filtering, sorting, and pagination use query parameters on `GET /products` (the collection endpoint). No `/search` suffix.
> **No new DTOs**: Uses existing `ProductDto`, Spring `Pageable` argument resolver, and `Page<ProductDto>` as the return type.

---

## Overview

This feature replaces the existing `GET /products?category=<name>` (required, non-paginated) with a fully-capable `GET /products` collection endpoint. The controller uses Spring's `@ParameterObject Pageable` to resolve `page`, `size`, and `sort` query params automatically; individual `@RequestParam` values handle the product-specific filters.

---

## Step 1 — Add Pageable defaults to `application.properties`

```properties
# src/main/resources/application.properties
spring.data.web.pageable.default-page-size=20
spring.data.web.pageable.max-page-size=100
```

---

## Step 2 — Add `JpaSpecificationExecutor` to `ProductRepository`

```java
// src/main/java/com/app/ecommerce/product/ProductRepository.java
public interface ProductRepository extends JpaRepository<Product, UUID>,
                                           JpaSpecificationExecutor<Product> {
    // existing methods unchanged
}
```

---

## Step 3 — Create `ProductSpecifications`

```java
// src/main/java/com/app/ecommerce/product/ProductSpecifications.java
public class ProductSpecifications {

    public static Specification<Product> nameLike(String name) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Product> priceGte(Double minPrice) {
        return (root, query, cb) -> cb.ge(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLte(Double maxPrice) {
        return (root, query, cb) -> cb.le(root.get("price"), maxPrice);
    }

    public static Specification<Product> hasCategory(UUID categoryId) {
        return (root, query, cb) -> {
            Join<Product, Category> categories = root.join("categories", JoinType.INNER);
            return cb.equal(categories.get("id"), categoryId);
        };
    }
}
```

---

## Step 4 — Replace `findAllByCategoryName` in `ProductService` interface

```java
// ProductService.java (interface)
// REMOVE: Set<?> findAllByCategoryName(String name);
// ADD:
Page<ProductDto> findAll(String name, Double minPrice, Double maxPrice,
                         UUID categoryId, Pageable pageable);
```

---

## Step 5 — Implement `findAll` in `ProductServiceImpl`

```java
// ProductServiceImpl.java
private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "price", "createdAt");

@Override
public Page<ProductDto> findAll(String name, Double minPrice, Double maxPrice,
                                UUID categoryId, Pageable pageable) {
    if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
        throw new IllegalArgumentException("minPrice must be less than or equal to maxPrice");
    }

    Specification<Product> spec = Specification.where(null);
    if (name != null)       spec = spec.and(ProductSpecifications.nameLike(name));
    if (minPrice != null)   spec = spec.and(ProductSpecifications.priceGte(minPrice));
    if (maxPrice != null)   spec = spec.and(ProductSpecifications.priceLte(maxPrice));
    if (categoryId != null) spec = spec.and(ProductSpecifications.hasCategory(categoryId));

    Sort safeSort = sanitizeSort(pageable.getSort());
    Pageable safePage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);

    return productRepository.findAll(spec, safePage).map(productMapper::toDto);
}

private Sort sanitizeSort(Sort sort) {
    if (!sort.isSorted()) return Sort.by(Sort.Direction.DESC, "createdAt");
    List<Sort.Order> safe = sort.stream()
        .map(o -> ALLOWED_SORT_FIELDS.contains(o.getProperty())
                  ? o : Sort.Order.desc("createdAt"))
        .collect(Collectors.toList());
    return Sort.by(safe);
}
```

---

## Step 6 — Replace `findProductsByCategoryName` in `ProductController` interface

```java
// ProductController.java (interface)
// REMOVE: ResponseEntity<ApiResponseDto<?>> findProductsByCategoryName(@RequestParam String category);
// ADD:
@Operation(summary = "List Products",
           description = "Returns paginated products with optional filters. Public endpoint.")
@ApiResponse(responseCode = "200", description = "Products retrieved successfully")
@ApiResponse(responseCode = "400", description = "Invalid query parameters")
ResponseEntity<ApiResponseDto<Page<ProductDto>>> findAll(
    @RequestParam(required = false) String name,
    @RequestParam(required = false) Double minPrice,
    @RequestParam(required = false) Double maxPrice,
    @RequestParam(required = false) UUID categoryId,
    @ParameterObject Pageable pageable
);
```

---

## Step 7 — Replace `findProductsByCategoryName` in `ProductControllerImpl`

```java
// ProductControllerImpl.java
// REMOVE the existing @GetMapping method that maps to findProductsByCategoryName
// ADD:
@GetMapping
@Override
public ResponseEntity<ApiResponseDto<Page<ProductDto>>> findAll(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) UUID categoryId,
        @ParameterObject
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable) {

    log.info("findAll(name={}, minPrice={}, maxPrice={}, categoryId={}, pageable={})",
             name, minPrice, maxPrice, categoryId, pageable);
    Page<ProductDto> page = productService.findAll(name, minPrice, maxPrice, categoryId, pageable);
    return ResponseEntity.ok(ApiResponseDto.success(page));
}
```

---

## Step 8 — Whitelist `GET /products/**` in `SecurityConfiguration`

```java
.requestMatchers(HttpMethod.GET, "/products/**").permitAll()
```

---

## Verification

```bash
# Start the application
mvn spring-boot:run

# List all products (no filters, default pagination)
curl "http://localhost:8080/products"

# Explicit pagination
curl "http://localhost:8080/products?page=0&size=5"

# Sort by price ascending
curl "http://localhost:8080/products?sort=price,asc"

# Filter by name
curl "http://localhost:8080/products?name=headphone&sort=price,asc"

# Filter by price range
curl "http://localhost:8080/products?minPrice=50&maxPrice=200"

# Filter by category UUID
curl "http://localhost:8080/products?categoryId=<uuid>"

# Combined filter + sort + pagination
curl "http://localhost:8080/products?name=phone&minPrice=100&sort=price,desc&page=0&size=5"

# Invalid sort → should return 200 sorted by createdAt,desc (no error)
curl "http://localhost:8080/products?sort=unknown,asc"

# Invalid price range → should return 400
curl "http://localhost:8080/products?minPrice=300&maxPrice=50"

# No auth header → should succeed (public endpoint)
curl "http://localhost:8080/products?page=0&size=5"
```
