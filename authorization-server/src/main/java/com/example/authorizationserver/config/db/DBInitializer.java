package com.example.authorizationserver.config.db;


import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class DBInitializer {


    private final UsersTableInitializer usersTableInitializer;
    private final ClientTableInitializer clientTableInitializer;


    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            usersTableInitializer.addAllUsers();
            clientTableInitializer.addAllClients();
        };
    }
}
