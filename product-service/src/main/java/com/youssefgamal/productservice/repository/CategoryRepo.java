package com.youssefgamal.productservice.repository;




import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.youssefgamal.productservice.entity.Category;
import com.youssefgamal.productservice.entity.Product;


@Repository
public interface CategoryRepo extends JpaRepository<Category,Long> {
	
	Optional<Category> findByName(String name);
	
	Optional<Category> findByIdAndProduct(Long id, Product product);
	
	Set<Category> findByNameIgnoreCase(String name);
}
