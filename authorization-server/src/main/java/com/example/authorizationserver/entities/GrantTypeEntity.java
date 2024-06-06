package com.example.authorizationserver.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "grant_types")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "clients")
public class GrantTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;

    @ManyToMany(mappedBy = "grantTypes" , fetch = FetchType.EAGER)
    private Set<ClientEntity> clients;


}
