package com.example.authorizationserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "scopes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "clients")
public class ScopeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String value;

    @ManyToMany(mappedBy = "scopes", fetch = FetchType.EAGER)
    private Set<ClientEntity> clients;

}
