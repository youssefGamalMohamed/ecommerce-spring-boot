package com.app.ecommerce.service.impl;

import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.mappers.ProductMapper;
import com.app.ecommerce.repository.ProductRepo;
import com.app.ecommerce.service.framework.IProductService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProductService implements IProductService {

	private final ProductRepo productRepo;
	private final ProductMapper productMapper;

	@Override
	public Product save(Product product) {
		log.info("save({})", product);
		Product newProduct = productMapper.mapToEntity(product);
		log.info("save(): Done Successfully, new product created with id = {}", newProduct.getId());
		return productRepo.save(newProduct);
	}

	@Override
	public Set<Product> findAllByCategoryName(String categoryName) {
		log.info("findAllByCategoryName({})", categoryName);
		return productRepo.findByCategories_Name(categoryName);
	}

	@Override
	public Product updateById(Long productId, Product newDataForProduct) {
		if (productId == null)
			throw new IllegalArgumentException("productId == null");

		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new IdNotFoundException(
						"Can Not Update Product , Id Not Found with value = " + productId));

		productMapper.updateEntityFromEntity(newDataForProduct, product);

		Product updatedProductData = productRepo.save(product);

		log.info("updated product with id = {}", updatedProductData.getId());
		return updatedProductData;
	}

	@Override
	public void deleteById(Long productId) {
		if (productId == null)
			throw new IllegalArgumentException("productId == null");

		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new IdNotFoundException("Product Id = " + productId + " Not Found to Delete"));

		log.info("product exists with id = {}", product.getId());

		productRepo.deleteById(productId);
	}
}
