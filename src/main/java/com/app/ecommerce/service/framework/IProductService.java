package com.app.ecommerce.service.framework;

import java.util.Set;

import com.app.ecommerce.entity.Product;


public interface IProductService {

    Product save(Product productDTO);

    Set<Product> findProductsByCategoryName(String categoryName);
    
    Product updateProductById(Long productId , Product updatedProductRequestBody);

    void deleteProductById(Long productId);

}
