package com.app.ecommerce.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.Set;

import org.hibernate.Hibernate;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Table(name = "Category")
@Entity(name = "Category")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "categories" , fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Product> products;
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Category category = (Category) o;
        return id != null && Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

	@Override
	public String toString() {
		return "Category [id=" + id + ", name=" + name + "]";
	}
    
    
}
