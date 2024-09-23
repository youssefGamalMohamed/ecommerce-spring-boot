package com.youssefgamal.productservice.service.framework;

import java.util.Set;

import com.youssefgamal.productservice.entity.Product;



public interface IProductService {

    Product save(Product productDTO) throws Exception;

    Set<Product> findProductsByCategoryName(String categoryName);
    
    Product updateProductById(Product updatedProduct);

    void deleteProductById(Long productId);

}
