package com.app.ecommerce.repository;

import com.app.ecommerce.entity.Category;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepo extends JpaRepository<Category,Long> {
	
	Optional<Category> findByName(String name);
	Set<Category> findByIdIn(Set<Long> ids);

}
