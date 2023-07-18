package com.app.ecommerce.factory;


import com.app.ecommerce.entity.Admin;
import com.app.ecommerce.entity.Customer;
import com.app.ecommerce.entity.User;
import com.app.ecommerce.enums.Role;
import com.app.ecommerce.models.request.RegisterRequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class UserFactory {

    @Autowired
    private PasswordEncoder passwordEncoder;


    private User getUser(Role role) {

        if(role == Role.ROLE_USER)
            return Customer.builder().build();

        return Admin.builder().build();
    }

    public User getUser(RegisterRequestBody registerRequestBody) {


        User user = this.getUser(registerRequestBody.getRole());

        user.setFirstname(registerRequestBody.getFirstname());
        user.setLastname(registerRequestBody.getLastname());
        user.setEmail(registerRequestBody.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestBody.getPassword()));
        user.setRole(registerRequestBody.getRole());
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }
}
