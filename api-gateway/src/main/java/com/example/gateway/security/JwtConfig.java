package com.example.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret:dev-secret-please-change}") String secret) {
        // Декодер JWT с симметричным ключом HMAC для демонстрационных целей
        return NimbusJwtDecoder.withSecretKey(JwtSecretKeyProvider.getHmacKey(secret)).build();
    }
}


