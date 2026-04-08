package com.app.ecommerce.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :id")
    void incrementFailedLoginAttempts(@Param("id") UUID id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.failedLoginAttempts = 0 WHERE u.id = :id")
    void resetFailedLoginAttempts(@Param("id") UUID id);
}
