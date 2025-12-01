package com.seccertificate.cert_generation.controller;

import com.seccertificate.cert_generation.dto.LoginResponse;
import com.seccertificate.cert_generation.security.JwtUtil;
import com.seccertificate.cert_generation.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestParam String username, @RequestParam String password) {
        var user = userService.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            String token = JwtUtil.generateToken(username, user.getRole());
            long expiry = JwtUtil.getExpiryFromToken(token);
            return new LoginResponse(token, expiry,user.getRole());
        }
        throw new RuntimeException("Invalid credentials");
    }
}
