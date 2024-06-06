package com.example.authorizationserver.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "authentication_method")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "clients")
public class AuthenticationMethodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "authentication_method")
    private String value;


    @ManyToMany(mappedBy = "authenticationMethods" , fetch = FetchType.EAGER)
    private Set<ClientEntity> clients;

}
