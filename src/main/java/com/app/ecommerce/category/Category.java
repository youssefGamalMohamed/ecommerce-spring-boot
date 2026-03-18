package com.app.ecommerce.category;

import com.app.ecommerce.product.Product;
import com.app.ecommerce.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Table(name = "Category")
@Entity(name = "Category")
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DynamicUpdate
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String name;

    @Version
    private Long version;

    @ToString.Exclude
    @Builder.Default
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    private Set<Product> products = new HashSet<>();
}
