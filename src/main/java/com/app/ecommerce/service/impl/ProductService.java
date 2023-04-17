package com.app.ecommerce.service.impl;


import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.models.request.ProductDTO;
import com.app.ecommerce.repository.CategoryRepo;
import com.app.ecommerce.repository.ProductRepo;
import com.app.ecommerce.service.framework.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService implements IProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CategoryRepo categoryRepo;


    @Override
    public boolean addProduct(ProductDTO productDTO) {

        Set<Category> categories = new HashSet<>();
        productDTO.getCategoriesId()
                .forEach(id -> categories.add(categoryRepo.findById(id).get()));

        Product product = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .description(productDTO.getDescription())
                .categories(categories)
                .build();
        productRepo.save(product);
        return false;
    }


	@Override
	public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
		return productRepo.findByCategoriesId(categoryId)
				.stream()
				.map(product -> ProductDTO.builder()
						.id(product.getId())
						.name(product.getName())
						.description(product.getDescription())
						.price(product.getPrice())
						.build())
				.toList();
	}
}
