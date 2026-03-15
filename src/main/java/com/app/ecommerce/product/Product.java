package com.app.ecommerce.product;

import com.app.ecommerce.cart.CartItem;
import com.app.ecommerce.category.Category;
import com.app.ecommerce.shared.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.DynamicUpdate;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "Product")
@Entity(name = "Product")
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DynamicUpdate
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private double price;

    @Column
    private Integer quantity;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH,
            CascadeType.REFRESH })
    @JoinTable(name = "product_category", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<CartItem> cartItem;
}
