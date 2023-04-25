package com.app.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.Set;

import org.hibernate.Hibernate;


@Table(name = "Product")
@Entity(name = "Product")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private double price;

    @Column
    private Integer quantity;
    
    

    @ManyToMany(fetch = FetchType.EAGER , cascade = { CascadeType.PERSIST , CascadeType.MERGE , CascadeType.REFRESH})
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Product product = (Product) o;
        return id != null && Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

	@Override
	public String toString() {
		return "Product [id=" + id + ", name=" + name + ", description=" + description + ", price=" + price
				+ ", quantity=" + quantity + "]";
	}

//	public void removeCategory(Long categoryId) {
//		Category category = this.categories.stream()
//				.filter(cate -> cate.getId() == categoryId)
//				.findFirst()
//				.get();
//		this.categories.remove(category);
//		category.getProducts().remove(this);
//	}
    
    
}
