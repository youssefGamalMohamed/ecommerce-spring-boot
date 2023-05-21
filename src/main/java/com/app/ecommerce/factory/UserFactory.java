package com.app.ecommerce.factory;


import com.app.ecommerce.entity.Admin;
import com.app.ecommerce.entity.Customer;
import com.app.ecommerce.entity.User;
import com.app.ecommerce.enums.Role;
import com.app.ecommerce.models.request.RegisterRequestBody;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserFactory {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Map<Role,User> userMap;


    @PostConstruct
    public void PosConstructor() {
        this.userMap = Map.of(
                Role.ROLE_USER , Customer.builder().build() ,
                Role.ROLE_ADMIN , Admin.builder().build()
        );
    }

    public User getUser(RegisterRequestBody registerRequestBody) {


        User user = userMap.get(registerRequestBody.getRole());

        user.setFirstname(registerRequestBody.getFirstname());
        user.setLastname(registerRequestBody.getLastname());
        user.setEmail(registerRequestBody.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestBody.getPassword()));
        user.setRole(registerRequestBody.getRole());

        return user;
    }
}
