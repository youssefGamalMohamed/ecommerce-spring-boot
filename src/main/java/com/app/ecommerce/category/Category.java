package com.app.ecommerce.category;

import com.app.ecommerce.shared.entity.BaseEntity;
import com.app.ecommerce.product.Product;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "Category")
@Entity(name = "Category")
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String name;

    @ToString.Exclude
    @Builder.Default
    @ManyToMany(mappedBy = "categories", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    private Set<Product> products = new HashSet<>();
}
