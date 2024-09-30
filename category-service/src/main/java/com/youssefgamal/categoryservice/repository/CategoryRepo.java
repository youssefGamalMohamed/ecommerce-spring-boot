package com.youssefgamal.categoryservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.youssefgamal.categoryservice.entity.Category;

@Repository
public interface CategoryRepo extends JpaRepository<Category,Long> {
	
	Optional<Category> findByName(String name);

}
