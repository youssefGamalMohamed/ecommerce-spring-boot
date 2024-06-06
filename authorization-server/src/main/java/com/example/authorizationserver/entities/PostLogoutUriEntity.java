package com.example.authorizationserver.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_logout_uris")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "client")
public class PostLogoutUriEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;

    @ManyToOne
    @JoinColumn(name = "client_fk")
    private ClientEntity client;
}
