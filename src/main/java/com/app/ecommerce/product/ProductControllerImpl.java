package com.app.ecommerce.product;

import com.app.ecommerce.shared.dto.ApiResponseDto;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductControllerImpl implements ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @PostMapping
    @Override
    public ResponseEntity<ApiResponseDto<ProductDto>> save(@Valid @RequestBody ProductDto productDto) {
        log.info("save({})", productDto);
        Product newCreatedProduct = productService.save(productMapper.mapToEntity(productDto));
        return new ResponseEntity<>(
                ApiResponseDto.created(productMapper.mapToDto(newCreatedProduct)),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<ProductDto>> findById(@PathVariable("id") UUID productId) {
        log.info("findById({})", productId);
        Product product = productService.findById(productId);
        return ResponseEntity.ok(ApiResponseDto.success(productMapper.mapToDto(product)));
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResponseDto<?>> findProductsByCategoryName(@RequestParam(value = "category") String categoryName) {
        log.info("findProductsByCategoryName({})", categoryName);
        Set<Product> products = productService.findAllByCategoryName(categoryName);
        return ResponseEntity.ok(ApiResponseDto.success(productMapper.mapToDtos(products)));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<ProductDto>> updateById(@PathVariable("id") UUID productId, @Valid @RequestBody ProductDto updatedBody) {
        log.info("updateById({}, {})", productId, updatedBody);

        Product updatedProduct = productService.updateById(productId, productMapper.mapToEntity(updatedBody));
        return new ResponseEntity<>(
                ApiResponseDto.success(productMapper.mapToDto(updatedProduct), "Product updated successfully"),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteById(@PathVariable("id") UUID productId) {
        log.info("deleteById({})", productId);
        productService.deleteById(productId);
        return new ResponseEntity<>(
                ApiResponseDto.noContent(),
                HttpStatus.NO_CONTENT
        );
    }

}
