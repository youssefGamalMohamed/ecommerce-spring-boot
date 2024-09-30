package com.youssefgamal.productservice.service.framework;

import java.util.Set;

import com.youssefgamal.productservice.entity.Product;



public interface IProductService {

    Product save(Product productDTO) throws Exception;
    
    Product updateProductById(Product updatedProduct);

    void deleteProductById(Long productId);

	Set<Product> findAll();

	void deleteAllProductsWithCategoryId(Long category_id);

	Product findById(Long id);

}
