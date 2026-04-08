package com.app.ecommerce.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Set<Product> findByCategoriesId(UUID categoryId);

    Set<Product> findByCategories_Name(String name);

    @EntityGraph(attributePaths = "categories")
    @Override
    Optional<Product> findById(UUID id);

    @EntityGraph(attributePaths = "categories")
    Page<Product> findAll(org.springframework.data.jpa.domain.Specification<Product> spec, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Product p WHERE p.id = :id")
    int deleteProductById(@Param("id") UUID id);

}
