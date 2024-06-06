package com.example.authorizationserver.services;

import com.example.authorizationserver.mappers.ClientMapper;
import com.example.authorizationserver.repositories.ClientRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Log4j2
public class ClientService implements RegisteredClientRepository {

    private final ClientRepo clientRepo;

    public ClientService(ClientRepo clientRepo) {
        this.clientRepo = clientRepo;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        clientRepo.save(ClientMapper.toEntity(registeredClient));
    }

    @Override
    public RegisteredClient findById(String id) {
        var client =
                clientRepo.findById(Integer.valueOf(id))
                        .orElseThrow();
        log.info(">>>>>>>>>>>>>> Client From DB = " + client);
        var registeredClient =  ClientMapper.toModel(client);
        log.info(">>>>>>>>>>>>>> RegisteredClient = " + registeredClient);
        return registeredClient;
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        var client =
                clientRepo.findByClientId(clientId)
                        .orElseThrow();
        log.info(">>>>>>>>>>>>>> Client From DB = " + client);
        var registeredClient =  ClientMapper.toModel(client);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + registeredClient.toString());
        return registeredClient;
    }
}