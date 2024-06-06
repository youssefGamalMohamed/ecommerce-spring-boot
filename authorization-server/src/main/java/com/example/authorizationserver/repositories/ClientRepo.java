package com.example.authorizationserver.repositories;

import com.example.authorizationserver.entities.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepo extends JpaRepository<ClientEntity, Integer> {

    @Query("SELECT c FROM ClientEntity c WHERE c.clientId = :clientId")
    Optional<ClientEntity> findByClientId(String clientId);

}
