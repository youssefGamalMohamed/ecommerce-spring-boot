package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.Product;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public interface IProductService {

    Product save(Product product);

    Set<Product> findAllByCategoryName(String categoryName);
    
    Product findById(UUID productId);

    List<Product> findAll();
    
    Product updateById(UUID productId , Product updatedProduct);

    void deleteById(UUID productId);

}
