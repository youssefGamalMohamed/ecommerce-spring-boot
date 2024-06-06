package com.example.authorizationserver.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "authorities")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "users")
public class AuthorityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @ManyToMany(mappedBy = "authorities" , fetch = FetchType.EAGER)
    private Set<UserEntity> users;
}