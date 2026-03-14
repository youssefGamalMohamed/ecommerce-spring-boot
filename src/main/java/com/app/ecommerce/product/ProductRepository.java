package com.app.ecommerce.product;

import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Set<Product> findByCategoriesId(UUID categoryId);

    Set<Product> findByCategories_Name(String name);

}
