package com.app.ecommerce.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        // Return a default system user for now. 
        // This should be updated when Spring Security is integrated to return the currently logged-in user.
        return () -> Optional.of("SYSTEM_USER");
    }
}
