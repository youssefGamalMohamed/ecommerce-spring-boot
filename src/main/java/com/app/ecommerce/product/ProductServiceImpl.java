package com.app.ecommerce.product;

import com.app.ecommerce.category.Category;
import com.app.ecommerce.category.CategoryDto;
import com.app.ecommerce.category.CategoryService;
import com.app.ecommerce.shared.constants.CacheConstants;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public Page<ProductDto> findAll(String name, Double minPrice, Double maxPrice, UUID categoryId, Pageable pageable) {
        log.info("findAll(name={}, minPrice={}, maxPrice={}, categoryId={}, pageable={})", name, minPrice, maxPrice,
                categoryId, pageable);

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("minPrice must be less than or equal to maxPrice");
        }

        Sort safeSort = sanitizeSort(pageable.getSort());
        PageRequest safePage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);

        Specification<Product> spec = Specification
                .where((Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null)
                .and(ProductSpecifications.nameLike(name))
                .and(ProductSpecifications.priceGte(minPrice))
                .and(ProductSpecifications.priceLte(maxPrice))
                .and(ProductSpecifications.hasCategory(categoryId));

        Page<ProductDto> result = productRepository.findAll(spec, safePage).map(productMapper::mapToDto);
        log.info("findAll(): Found {} products", result.getTotalElements());
        return result;
    }

    private Sort sanitizeSort(Sort sort) {
        Set<String> allowedFields = Set.of("name", "price", "createdAt");
        if (sort == null || sort.isUnsorted()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Sort.Order[] orders = sort.get().map(order -> {
            if (allowedFields.contains(order.getProperty())) {
                return order;
            }
            return Sort.Order.desc("createdAt");
        }).toArray(Sort.Order[]::new);
        return Sort.by(orders);
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
