package com.app.ecommerce.auth;

import com.app.ecommerce.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "tokens")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Token extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 500)
    private String accessToken;

    @Column(unique = true, nullable = false, length = 500)
    private String refreshToken;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean expired = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
