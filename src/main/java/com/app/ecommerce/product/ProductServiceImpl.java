package com.app.ecommerce.product;

import com.app.ecommerce.category.Category;
import com.app.ecommerce.category.CategoryService;
import com.app.ecommerce.shared.constants.CacheConstants;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;

    @Override
    @CachePut(value = CacheConstants.PRODUCTS, key = "#result.id")
    @Transactional
    public ProductResponse save(CreateProductRequest request) {
        log.info("save({})", request);
        Set<Category> managedCategories = categoryService.getCategories(request.getCategoryIds());
        Product productToSave = productMapper.mapToEntity(request);
        Product mappedProduct = productMapper.mapToEntity(productToSave, managedCategories);
        Product savedProduct = productRepository.save(mappedProduct);
        log.info("save(): Done Successfully, new product created with id = {}", savedProduct.getId());
        return productMapper.mapToResponse(savedProduct);
    }

    @Override
    @Cacheable(value = CacheConstants.PRODUCTS, key = "#productId")
    @Transactional(readOnly = true)
    public ProductResponse findById(UUID productId) {
        log.info("findById({})", productId);
        if (productId == null) {
            throw new IllegalArgumentException("productId == null");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product Id = " + productId + " Not Found"));
        log.info("findById(): Found product with id = {}", productId);
        return productMapper.mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(String name, BigDecimal minPrice, BigDecimal maxPrice, UUID categoryId, Pageable pageable) {
        log.info("findAll(name={}, minPrice={}, maxPrice={}, categoryId={}, pageable={})", name, minPrice, maxPrice,
                categoryId, pageable);

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
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

        Page<ProductResponse> result = productRepository.findAll(spec, safePage).map(productMapper::mapToResponse);
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
    @CachePut(value = CacheConstants.PRODUCTS, key = "#result.id")
    @Transactional
    public ProductResponse updateById(UUID productId, UpdateProductRequest request) {
        log.info("updateById({}, {})", productId, request);
        if (productId == null) {
            throw new IllegalArgumentException("productId == null");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Can Not Update Product , Id Not Found with value = " + productId));
        log.info("updateById(): Found product with id = {}", productId);

        product.setVersion(request.getVersion());

        if (request.getCategoryIds() != null) {
            Set<Category> managedCategories = categoryService.getCategories(request.getCategoryIds());
            productMapper.updateEntityFromRequest(request, product);
            productMapper.updateEntityFromEntity(product, managedCategories, product);
        } else {
            productMapper.updateEntityFromRequest(request, product);
        }

        Product updatedProductData = productRepository.save(product);

        log.info("updated product with id = {}", updatedProductData.getId());
        return productMapper.mapToResponse(updatedProductData);
    }

    @Override
    @CacheEvict(value = CacheConstants.PRODUCTS, key = "#productId")
    @Transactional
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
