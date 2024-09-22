package com.youssefgamal.categoryservice.entity;


import jakarta.persistence.*;
import lombok.*;

@Table(name = "Category")
@Entity(name = "Category")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

}
