package com.app.ecommerce.category.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;


@Table(name = "Product")
@Entity(name = "Product")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product extends BaseEntity {

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



    @ManyToOne(fetch = FetchType.EAGER , cascade = { CascadeType.PERSIST , CascadeType.MERGE , CascadeType.DETACH , CascadeType.REFRESH })
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private Category categories;
}
