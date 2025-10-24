package com.example.hotel.security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

final class JwtSecretKeyProvider {
    private JwtSecretKeyProvider() {}

    static SecretKey getHmacKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
}


