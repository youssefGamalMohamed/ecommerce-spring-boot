package com.app.ecommerce.product;

import com.app.ecommerce.category.Category;
import com.app.ecommerce.category.CategoryDto;
import com.app.ecommerce.category.CategoryService;
import com.app.ecommerce.shared.constants.CacheConstants;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;

    @Override
    @CacheEvict(value = CacheConstants.PRODUCTS, allEntries = true)
    public ProductDto save(ProductDto productDto) {
        log.info("save({})", productDto);
        Set<UUID> categoryIds = productDto.getCategories().stream()
                .map(CategoryDto::getId)
                .collect(Collectors.toSet());
        Set<Category> managedCategories = categoryService.getCategories(categoryIds);
        Product productToSave = productMapper.mapToEntity(productDto);
        Product mappedProduct = productMapper.mapToEntity(productToSave, managedCategories);
        Product savedProduct = productRepository.save(mappedProduct);
        log.info("save(): Done Successfully, new product created with id = {}", savedProduct.getId());
        return productMapper.mapToDto(savedProduct);
    }

    @Override
    public Set<ProductDto> findAllByCategoryName(String categoryName) {
        log.info("findAllByCategoryName({})", categoryName);
        Set<Product> products = productRepository.findByCategories_Name(categoryName);
        log.info("findAllByCategoryName(): Found {} products for category '{}'", products.size(), categoryName);
        return productMapper.mapToDtos(products);
    }

    @Override
    @Cacheable(value = CacheConstants.PRODUCTS, key = "#productId")
    public ProductDto findById(UUID productId) {
        log.info("findById({})", productId);
        if (productId == null) {
            throw new IllegalArgumentException("productId == null");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product Id = " + productId + " Not Found"));
        log.info("findById(): Found product with id = {}", productId);
        return productMapper.mapToDto(product);
    }

    @Override
    public List<ProductDto> findAll() {
        log.info("findAll()");
        return productMapper.mapToDtos(productRepository.findAll());
    }

    @Override
    @CacheEvict(value = CacheConstants.PRODUCTS, allEntries = true)
    public ProductDto updateById(UUID productId, ProductDto updatedProductDto) {
        log.info("updateById({}, {})", productId, updatedProductDto);
        if (productId == null) {
            throw new IllegalArgumentException("productId == null");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Can Not Update Product , Id Not Found with value = " + productId));
        log.info("updateById(): Found product with id = {}", productId);

        Set<UUID> categoryIds = updatedProductDto.getCategories().stream()
                .map(CategoryDto::getId)
                .collect(Collectors.toSet());
        Set<Category> managedCategories = categoryService.getCategories(categoryIds);
        Product tempProduct = productMapper.mapToEntity(updatedProductDto);
        productMapper.updateEntityFromEntity(tempProduct, managedCategories, product);

        Product updatedProductData = productRepository.save(product);

        log.info("updated product with id = {}", updatedProductData.getId());
        return productMapper.mapToDto(updatedProductData);
    }

    @Override
    @CacheEvict(value = CacheConstants.PRODUCTS, allEntries = true)
    public void deleteById(UUID productId) {
        log.info("deleteById({})", productId);
        if (productId == null) {
            throw new IllegalArgumentException("productId == null");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product Id = " + productId + " Not Found to Delete"));

        log.info("product exists with id = {}", product.getId());

        productRepository.deleteById(productId);
    }
}
