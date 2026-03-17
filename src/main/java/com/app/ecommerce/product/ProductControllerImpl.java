package com.app.ecommerce.product;

import com.app.ecommerce.shared.dto.ApiResponseDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductControllerImpl implements ProductController {

    private final ProductService productService;

    @PostMapping
    @Override
    public ResponseEntity<ApiResponseDto<ProductDto>> save(@Valid @RequestBody ProductDto productDto) {
        log.info("save({})", productDto);
        ProductDto newCreatedProduct = productService.save(productDto);
        return new ResponseEntity<>(
                ApiResponseDto.created(newCreatedProduct),
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<ProductDto>> findById(@PathVariable("id") UUID productId) {
        log.info("findById({})", productId);
        ProductDto product = productService.findById(productId);
        return ResponseEntity.ok(ApiResponseDto.success(product));
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResponseDto<Page<ProductDto>>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) UUID categoryId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("findAll(name={}, minPrice={}, maxPrice={}, categoryId={}, pageable={})", name, minPrice, maxPrice,
                categoryId, pageable);
        Page<ProductDto> page = productService.findAll(name, minPrice, maxPrice, categoryId, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(page));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<ProductDto>> updateById(@PathVariable("id") UUID productId,
                                                                 @Valid @RequestBody ProductDto updatedBody) {
        log.info("updateById({}, {})", productId, updatedBody);
        ProductDto updatedProduct = productService.updateById(productId, updatedBody);
        return new ResponseEntity<>(
                ApiResponseDto.success(updatedProduct, "Product updated successfully"),
                HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteById(@PathVariable("id") UUID productId) {
        log.info("deleteById({})", productId);
        productService.deleteById(productId);
        return new ResponseEntity<>(
                ApiResponseDto.noContent(),
                HttpStatus.NO_CONTENT);
    }

}
