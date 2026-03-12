package com.app.ecommerce.service.impl;

import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.mappers.ProductMapper;
import com.app.ecommerce.repository.ProductRepo;
import com.app.ecommerce.service.framework.IProductService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService implements IProductService {

	private final ProductRepo productRepo;
	private final ProductMapper productMapper;

	@Override
	public Product save(Product product) {
		log.info("save({})", product);
		Product newProduct = productMapper.mapToEntity(product);
		Product savedProduct = productRepo.save(newProduct);
		log.info("save(): Done Successfully, new product created with id = {}", savedProduct.getId());
		return savedProduct;
	}

	@Override
	public Set<Product> findAllByCategoryName(String categoryName) {
		log.info("findAllByCategoryName({})", categoryName);
		Set<Product> products = productRepo.findByCategories_Name(categoryName);
		log.info("findAllByCategoryName(): Found {} products for category '{}'", products.size(), categoryName);
		return products;
	}

	@Override
	public Product findById(Long productId) {
		log.info("findById({})", productId);
		if (productId == null) {
			throw new IllegalArgumentException("productId == null");
		}
		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new IdNotFoundException("Product Id = " + productId + " Not Found"));
		log.info("findById(): Found product with id = {}", productId);
		return product;
	}

	@Override
	public List<Product> findAll() {
		log.info("findAll()");
		return productRepo.findAll();
	}

	@Override
	public Product updateById(Long productId, Product newDataForProduct) {
		log.info("updateById({}, {})", productId, newDataForProduct);
		if (productId == null)
			throw new IllegalArgumentException("productId == null");

		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new IdNotFoundException(
						"Can Not Update Product , Id Not Found with value = " + productId));
		log.info("updateById(): Found product with id = {}", productId);

		productMapper.updateEntityFromEntity(newDataForProduct, product);

		Product updatedProductData = productRepo.save(product);

		log.info("updated product with id = {}", updatedProductData.getId());
		return updatedProductData;
	}

	@Override
	public void deleteById(Long productId) {
		log.info("deleteById({})", productId);
		if (productId == null)
			throw new IllegalArgumentException("productId == null");

		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new IdNotFoundException("Product Id = " + productId + " Not Found to Delete"));

		log.info("product exists with id = {}", product.getId());

		productRepo.deleteById(productId);
	}
}
