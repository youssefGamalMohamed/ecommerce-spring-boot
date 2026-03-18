package com.app.ecommerce.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findByAccessToken(String accessToken);

    Optional<Token> findByRefreshToken(String refreshToken);

    List<Token> findAllByUserAndRevokedFalseAndExpiredFalse(User user);

    @Modifying
    @Query("UPDATE Token t SET t.revoked = true WHERE t.user = :user AND t.revoked = false AND t.expired = false")
    void revokeAllValidTokensByUser(@Param("user") User user);
}
