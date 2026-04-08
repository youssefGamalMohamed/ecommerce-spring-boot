package com.app.ecommerce.category;

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
public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {

    Optional<Category> findByName(String name);

    Set<Category> findByIdIn(Set<UUID> ids);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Category c WHERE c.id = :id")
    int deleteCategoryById(@Param("id") UUID id);

}
