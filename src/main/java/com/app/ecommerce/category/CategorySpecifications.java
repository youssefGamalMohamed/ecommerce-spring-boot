package com.app.ecommerce.category;

import org.springframework.data.jpa.domain.Specification;

public class CategorySpecifications {

    public static Specification<Category> nameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }
}
