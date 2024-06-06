package com.example.authorizationserver.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clients")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientId;

    private String clientSecret;

    private String clientName;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "clients_authentication_methods_join_table",
            joinColumns = { @JoinColumn(name = "client_fk") },
            inverseJoinColumns = { @JoinColumn(name = "authentication_method_fk") }
    )
    private Set<AuthenticationMethodEntity> authenticationMethods;


    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "clients_grant_type_join_table",
            joinColumns = { @JoinColumn(name = "client_fk") },
            inverseJoinColumns = { @JoinColumn(name = "grant_type_fk") }
    )
    private Set<GrantTypeEntity> grantTypes;


    @OneToMany(mappedBy = "client" , fetch = FetchType.EAGER , cascade = CascadeType.ALL)
    private Set<RedirectUriEntity> redirectUris;

    @OneToMany(mappedBy = "client" , fetch = FetchType.EAGER , cascade = CascadeType.ALL)
    private Set<PostLogoutUriEntity> postLogoutUris;


    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "clients_scopes_join_table",
            joinColumns = { @JoinColumn(name = "client_fk") },
            inverseJoinColumns = { @JoinColumn(name = "scope_fk") }
    )
    private Set<ScopeEntity> scopes;


    @OneToOne(fetch = FetchType.EAGER , cascade = CascadeType.ALL)
    @JoinColumn(name = "token_settings_fk")
    private TokenSettingsEntity tokenSettings;


    @OneToOne(fetch = FetchType.EAGER , cascade = CascadeType.ALL)
    @JoinColumn(name = "client_settings_fk")
    private ClientSettingsEntity clientSettings;

}