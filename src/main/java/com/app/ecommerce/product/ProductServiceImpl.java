package com.app.ecommerce.product;

import com.app.ecommerce.category.Category;
import com.app.ecommerce.category.CategoryService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;

    @Override
    public Product save(Product product) {
        log.info("save({})", product);
        Set<UUID> categoryIds = product.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        Set<Category> managedCategories = categoryService.getCategories(categoryIds);
        Product productToSave = productMapper.mapToEntity(product, managedCategories);
        Product savedProduct = productRepository.save(productToSave);
        log.info("save(): Done Successfully, new product created with id = {}", savedProduct.getId());
        return savedProduct;
    }

    @Override
    public Set<Product> findAllByCategoryName(String categoryName) {
        log.info("findAllByCategoryName({})", categoryName);
        Set<Product> products = productRepository.findByCategories_Name(categoryName);
        log.info("findAllByCategoryName(): Found {} products for category '{}'", products.size(), categoryName);
        return products;
    }

    @Override
    public Product findById(UUID productId) {
        log.info("findById({})", productId);
        if (productId == null) {
            throw new IllegalArgumentException("productId == null");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product Id = " + productId + " Not Found"));
        log.info("findById(): Found product with id = {}", productId);
        return product;
    }

    @Override
    public List<Product> findAll() {
        log.info("findAll()");
        return productRepository.findAll();
    }

    @Override
    public Product updateById(UUID productId, Product newDataForProduct) {
        log.info("updateById({}, {})", productId, newDataForProduct);
        if (productId == null) {
            throw new IllegalArgumentException("productId == null");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Can Not Update Product , Id Not Found with value = " + productId));
        log.info("updateById(): Found product with id = {}", productId);

        Set<UUID> categoryIds = newDataForProduct.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        Set<Category> managedCategories = categoryService.getCategories(categoryIds);
        productMapper.updateEntityFromEntity(newDataForProduct, managedCategories, product);

        Product updatedProductData = productRepository.save(product);

        log.info("updated product with id = {}", updatedProductData.getId());
        return updatedProductData;
    }

    @Override
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
