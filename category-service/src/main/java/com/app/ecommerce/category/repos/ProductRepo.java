package com.app.ecommerce.category.repos;

import com.app.ecommerce.category.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepo extends JpaRepository<Product, Long> {
}
