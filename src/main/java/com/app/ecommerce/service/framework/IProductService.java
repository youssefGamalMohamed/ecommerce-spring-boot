package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.Product;
import java.util.Set;


public interface IProductService {

    Product save(Product productDTO);

    Set<Product> findProductsByCategoryName(String categoryName);
    
    Product updateProductById(Long productId , Product updatedProductRequestBody);

    void deleteProductById(Long productId);

}
