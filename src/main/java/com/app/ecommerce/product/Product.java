package com.app.ecommerce.product;

import com.app.ecommerce.cart.CartItem;
import com.app.ecommerce.category.Category;
import com.app.ecommerce.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    @Column(precision = 19, scale = 2)
    private BigDecimal price;

    @Column
    private Integer quantity;

    @Version
    private Long version;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH,
            CascadeType.REFRESH})
    @JoinTable(name = "product_category", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItem;
}
