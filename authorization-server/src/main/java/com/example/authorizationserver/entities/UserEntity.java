package com.example.authorizationserver.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String username;
    private String password;

    // stores all OAuth2User and OidcUser for any provider ( ME , GOOGLE , GITHUB , FACEBOOK )
    @Column(columnDefinition = "json")
    private String claims;

    // this variable holds the name of the provider ( ME , GITHUB , GOOGLE , FACEBOOK , OKTA )
    // to know that which provider that this user related to ( ME , GITHUB , GOOGLE , FACEBOOK , OKTA )
    private String authProviderName = "ME";

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "users_authorities_join_table",
            joinColumns = { @JoinColumn(name = "user_fk") },
            inverseJoinColumns = { @JoinColumn(name = "authority_fk") }
    )
    private Set<AuthorityEntity> authorities;

}