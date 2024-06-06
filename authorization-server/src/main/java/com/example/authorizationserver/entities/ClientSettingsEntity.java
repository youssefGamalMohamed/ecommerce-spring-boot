package com.example.authorizationserver.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clients_settings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "client")
public class ClientSettingsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_fk")
    private Long id;

    private boolean isRequiredAuthorizationConsent;


    @OneToOne(mappedBy = "clientSettings" , fetch = FetchType.EAGER)
    private ClientEntity client;
}
