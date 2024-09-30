package com.youssefgamal.productservice.repository;




import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.youssefgamal.productservice.entity.Category;

import jakarta.transaction.Transactional;


@Repository
public interface CategoryRepo extends JpaRepository<Category,Long> {
	
    // Custom query to delete Category by 'id'
    @Modifying
    @Transactional
    @Query("DELETE FROM Category c WHERE c.categoryId = :categoryId")
    void deleteAllCategoriesByCategoryIdFromCategoryService(Long categoryId);
}
