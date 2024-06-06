package com.example.authorizationserver.services;


import com.example.authorizationserver.mappers.RegisterNewUserRequestBodyMapper;
import com.example.authorizationserver.models.RegisterNewUserRequestBody;
import com.example.authorizationserver.repositories.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {


    private final UserRepo userRepo;

    public boolean registerNewUser(RegisterNewUserRequestBody registerNewUserRequestBody) {
        if(userRepo.findByUsername(registerNewUserRequestBody.getUsername()).isPresent()) {
            return false;
        }
        userRepo.save(RegisterNewUserRequestBodyMapper.toEntity(registerNewUserRequestBody));
        return true;
    }

}
