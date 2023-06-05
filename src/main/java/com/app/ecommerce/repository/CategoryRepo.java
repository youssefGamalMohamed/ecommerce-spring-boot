package com.app.ecommerce.repository;

import com.app.ecommerce.entity.Category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepo extends CrudRepository<Category,Long> {
	
	Optional<Category> findByName(String name);

	List<Category> findAll(Pageable paging);

//	@Query("SELECT c.id , c.name FROM Category c")
//	List<Object[]> findAllCategoriesSS();
}
