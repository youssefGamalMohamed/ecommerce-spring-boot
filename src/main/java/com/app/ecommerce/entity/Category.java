package com.app.ecommerce.entity;


import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;


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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @ToString.Exclude
    @Builder.Default
    @ManyToMany(mappedBy = "categories" , fetch = FetchType.EAGER, cascade = { CascadeType.REMOVE , CascadeType.MERGE , CascadeType.DETACH , CascadeType.REFRESH })
    private Set<Product> products = new HashSet<>();
}
