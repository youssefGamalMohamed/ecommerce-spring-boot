package com.example.authorizationserver.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "redirect_uris")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "client")
public class RedirectUriEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;

    @ManyToOne
    @JoinColumn(name = "client_fk")
    private ClientEntity client;


}
