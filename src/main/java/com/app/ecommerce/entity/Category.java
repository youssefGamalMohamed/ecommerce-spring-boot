package com.app.ecommerce.entity;


import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Table(name = "Category")
@Entity(name = "Category")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    
    @ManyToMany(mappedBy = "categories" , fetch = FetchType.EAGER
            , cascade = { CascadeType.PERSIST , CascadeType.MERGE , CascadeType.DETACH , CascadeType.REFRESH })
    @JsonIgnore
    private Set<Product> products = new HashSet<>();

	@Override
	public String toString() {
		return "Category [id=" + id + ", name=" + name + "]";
	}
    
    public void removeProduct(Product product) {
        this.getProducts().remove(product);
    }
}
