package com.example.booking.web;

import com.example.booking.model.User;
import com.example.booking.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public User register(@RequestBody Map<String, Object> req) {
        String username = (String) req.get("username");
        String password = (String) req.get("password");
        boolean admin = req.getOrDefault("admin", false) instanceof Boolean b && b;
        return authService.register(username, password, admin);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> req) {
        String token = authService.login(req.get("username"), req.get("password"));
        return ResponseEntity.ok(Map.of("access_token", token, "token_type", "Bearer"));
    }
}


