package com.example.authorizationserver.mappers;

import com.example.authorizationserver.entities.UserEntity;
import com.example.authorizationserver.models.RegisterNewUserRequestBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RegisterNewUserRequestBodyMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static UserEntity toEntity(RegisterNewUserRequestBody registerNewUserRequestBody) {
        try {
            return UserEntity.builder()
                    .username(registerNewUserRequestBody.getUsername())
                    .password(registerNewUserRequestBody.getPassword())
                    .claims(objectMapper.writeValueAsString(registerNewUserRequestBody))
                    .build();
        } catch (JsonProcessingException e) {
            log.error(">>>>>>>>> Error : Can't convert this claims " + registerNewUserRequestBody + " to json");
            throw new RuntimeException(e);
        }
    }


}
