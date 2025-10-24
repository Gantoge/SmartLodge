package com.example.hotel;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class HotelHttpIT {

    @Autowired
    private MockMvc mockMvc;

    private String tokenAdmin() {
        byte[] bytes = "dev-secret-please-change".getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject("1")
                .addClaims(Map.of("scope", "ADMIN", "username", "admin"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(bytes))
                .compact();
    }

    @Test
    void adminCanCreateHotel() throws Exception {
        mockMvc.perform(post("/hotels")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"H\",\"city\":\"C\",\"address\":\"A\"}"))
                .andExpect(status().isOk());
    }
}


