package com.youssefgamal.productservice.entity;


import jakarta.persistence.*;
import lombok.*;


@Table(name = "Category")
@Entity(name = "Category")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "product")
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryIdAutoIncrement;
    
    private Long id;

    private String name;

    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")
    Product product = new Product();
    
}
