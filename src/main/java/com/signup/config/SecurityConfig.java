package com.signup.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API endpoints
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
                // Allow access to H2 console FIRST
                .requestMatchers("/h2-console/**").permitAll()
                // Allow access to Swagger UI and related endpoints
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**").permitAll()
                // Allow access to public auth endpoints
//                .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/refresh", "/api/auth/forgot-password", "/api/auth/reset-password", "/api/auth/test-public", "/api/auth/users").permitAll()
                // Allow favicon and error endpoints
                            .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/users").permitAll()
                .requestMatchers("/favicon.ico", "/error").permitAll()
                // Require authentication for all other endpoints
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Allow H2 console to work (disable frame options)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
                .contentTypeOptions(contentTypeOptions -> {})
            );

        return http.build();
    }

    // Add CORS configuration to allow all origins and methods
    @Bean
    public org.springframework.web.servlet.config.annotation.WebMvcConfigurer corsConfigurer() {
        return new org.springframework.web.servlet.config.annotation.WebMvcConfigurer() {
            @Override
            public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
} 