package com.app.ecommerce.repository;

import com.app.ecommerce.entity.Product;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepo extends JpaRepository<Product, UUID> {
	
	Set<Product> findByCategoriesId(UUID categoryId);
	Set<Product> findByCategories_Name(String name);
	
}
