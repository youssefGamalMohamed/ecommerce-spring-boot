package com.example.authorizationserver.services;

import com.example.authorizationserver.entities.UserEntity;
import com.example.authorizationserver.wrappers.SecurityUser;
import com.example.authorizationserver.repositories.UserRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log4j2
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepo userRepo;

    public UserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        log.info(">>>>>>>>>>>>>>>>>>>>>> UserEntity = " + user.get().toString());
        return user.map(SecurityUser::new).orElseThrow(() -> new UsernameNotFoundException(":("));
    }

    public boolean saveUserIFNotExists(UserEntity userEntity) {
        if(userRepo.findByUsername(userEntity.getUsername()).isPresent())
            return false;

        userEntity = userRepo.save(userEntity);

        return true;
    }
}