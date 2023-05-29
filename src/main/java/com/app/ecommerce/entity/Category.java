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

    
    @ManyToMany(mappedBy = "categories" , fetch = FetchType.EAGER
            , cascade = { CascadeType.PERSIST , CascadeType.MERGE , CascadeType.DETACH , CascadeType.REFRESH })
    @JsonIgnore
    private Set<Product> products;

	@Override
	public String toString() {
		return "Category [id=" + id + ", name=" + name + "]";
	}
    
    public void removeProduct(Product product) {
        this.getProducts().remove(product);
    }
}
