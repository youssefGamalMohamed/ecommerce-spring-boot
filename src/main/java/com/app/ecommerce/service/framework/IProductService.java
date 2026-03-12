package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.Product;
import java.util.List;
import java.util.Set;


public interface IProductService {

    Product save(Product product);

    Set<Product> findAllByCategoryName(String categoryName);
    
    Product findById(Long productId);

    List<Product> findAll();
    
    Product updateById(Long productId , Product updatedProduct);

    void deleteById(Long productId);

}
