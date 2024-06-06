package com.example.authorizationserver.config.db;


import com.example.authorizationserver.entities.AuthorityEntity;
import com.example.authorizationserver.entities.UserEntity;
import com.example.authorizationserver.models.RegisterNewUserRequestBody;
import com.example.authorizationserver.repositories.UserRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@AllArgsConstructor
public class UsersTableInitializer {

    private final UserRepo userRepo;
    private final ObjectMapper objectMapper;

    public void addAllUsers() throws JsonProcessingException {

        AuthorityEntity READ = AuthorityEntity.builder().name("READ").build();

        AuthorityEntity WRITE = AuthorityEntity.builder().name("WRITE").build();

        AuthorityEntity DELETE = AuthorityEntity.builder().name("DELETE").build();

        AuthorityEntity UPDATE = AuthorityEntity.builder().name("UPDATE").build();

        AuthorityEntity ROLE_ADMIN = AuthorityEntity.builder().name("ROLE_ADMIN").build();

        AuthorityEntity ROLE_USER = AuthorityEntity.builder().name("ROLE_USER").build();


        RegisterNewUserRequestBody admin_registerNewUserRequestBody = RegisterNewUserRequestBody.builder()
                .username("ecommerce-admin-portal@gmail.com")
                .password("P@ssw0rd@123")
                .name("ecommerce-admin-portal")
                .given_name("ecommerce-admin-portal")
                .family_name("ecommerce-admin-portal-family-name")
                .middle_name("ecommerce-admin-portal")
                .nick_name("ecommerce-admin-portal-admin-nickname")
                .preferred_username("admin-preferred-name")
                .email("ecommerce-admin-portal@gmail.com")
                .email_verified(false)
                .gender("MALE")
                .birthdate("22/01/1999")
                .zoneinfo("America/New_York")
                .locale("en-US")
                .phone_number("1234567890")
                .phone_number_verified(false)
                .profile("http://pictures/1")
                .picture("http://pictures/1")
                .website("http://www/1")
                .build();


        String admin_json_claims = objectMapper.writeValueAsString(admin_registerNewUserRequestBody);
        UserEntity admin = UserEntity.builder()
                .username("ecommerce-admin-portal@gmail.com")
                .password("P@ssw0rd@123")
                .claims(admin_json_claims)
                .authorities(Set.of(READ , WRITE , DELETE , UPDATE, ROLE_ADMIN))
                .authProviderName("AUTHORIZATION_SERVER_ME")
                .build();
        userRepo.save(admin);


        //--------------------------------------------------------------------------------------------

        RegisterNewUserRequestBody user1_registerNewUserRequestBody = RegisterNewUserRequestBody.builder()
                .username("user1@gmail.com")
                .password("user1_1234")
                .name("user1")
                .given_name("user1")
                .family_name("user1-family-name")
                .middle_name("user1")
                .nick_name("user1-nickname")
                .preferred_username("user1-preferred-name")
                .email("user1@gmail.com")
                .email_verified(false)
                .gender("MALE")
                .birthdate("22/01/1999")
                .zoneinfo("America/New_York")
                .locale("en-US")
                .phone_number("1234567890")
                .phone_number_verified(false)
                .profile("http://pictures/1")
                .picture("http://pictures/1")
                .website("http://www/1")
                .build();

        String user1_json_claims = objectMapper.writeValueAsString(user1_registerNewUserRequestBody);
        UserEntity user1 = UserEntity.builder()
                .username("user1@gmail.com")
                .password("user1_1234")
                .claims(user1_json_claims)
                .authorities(Set.of(ROLE_USER))
                .authProviderName("AUTHORIZATION_SERVER_ME")
                .build();
        userRepo.save(user1);

    }
}
