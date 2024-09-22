package com.app.ecommerce.config;


import com.app.ecommerce.repository.UserRepo;
import com.app.ecommerce.security.filters.JwtAuthenticationFilter;
import com.app.ecommerce.security.handler.CustomBearerTokenAccessDeniedHandler;
import com.app.ecommerce.security.handler.CustomBearerTokenAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true
)
public class SecurityConfiguration {

    @Lazy
    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Lazy
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Lazy
    @Autowired
    private CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler;
    @Lazy
    @Autowired
    private CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint;


    @Lazy
    @Autowired
    private UserRepo repository;

    private  String[] whiteListEndPoints =
            {
                "/auth/register",
                "/auth/login",
                "/auth/refresh-token",
                "/auth/verify-registration/**",
                "/auth/forget-password/**",
                "/auth/reset-password",
                "/swagger-resources",
                "/swagger-resources/**",
                "/configuration/ui",
                "/configuration/security",
                "/swagger-ui/**",
                "/webjars/**",
                "/swagger-ui.html",
                "/api-docs/**"
            };





    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .authorizeHttpRequests()
                .requestMatchers(whiteListEndPoints)
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .accessDeniedHandler(customBearerTokenAccessDeniedHandler)
                .authenticationEntryPoint(customBearerTokenAuthenticationEntryPoint)
        ;

        return http.build();
    }

}


