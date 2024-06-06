package com.example.authorizationserver.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;

@Entity
@Table(name = "token_settings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "client")
public class TokenSettingsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // access token time to leave
    @Column(name = "access_token_ttl")
    private Duration accessTokenTTL;

    private String type;


    @OneToOne(mappedBy = "tokenSettings" , fetch = FetchType.EAGER)
    private ClientEntity client;


}
