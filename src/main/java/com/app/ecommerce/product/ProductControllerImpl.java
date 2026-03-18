package com.app.ecommerce.product;

import com.app.ecommerce.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductControllerImpl implements ProductController {

    private final ProductService productService;

    @PostMapping
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> save(@Valid @RequestBody CreateProductRequest request) {
        log.info("save({})", request);
        ProductResponse newCreatedProduct = productService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(newCreatedProduct));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponse<ProductResponse>> findById(@PathVariable("id") UUID productId) {
        log.info("findById({})", productId);
        ProductResponse product = productService.findById(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) UUID categoryId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("findAll(name={}, minPrice={}, maxPrice={}, categoryId={}, pageable={})", name, minPrice, maxPrice,
                categoryId, pageable);
        Page<ProductResponse> page = productService.findAll(name, minPrice, maxPrice, categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PatchMapping("/{id}")
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateById(@PathVariable("id") UUID productId,
                                                                  @Valid @RequestBody UpdateProductRequest request) {
        log.info("updateById({}, {})", productId, request);
        ProductResponse updatedProduct = productService.updateById(productId, request);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable("id") UUID productId) {
        log.info("deleteById({})", productId);
        productService.deleteById(productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.noContent());
    }

}
