package com.seccertificate.cert_generation.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    @Test
    void generateAndValidateToken_roundTrip() {
        String token = JwtUtil.generateToken("alice", "ROLE_USER");

        String subject = JwtUtil.validateToken(token);
        assertThat(subject).isEqualTo("alice");

        String role = JwtUtil.getRole(token);
        assertThat(role).isEqualTo("ROLE_USER");

        long expiry = JwtUtil.getExpiryFromToken(token);
        assertThat(expiry).isGreaterThan(System.currentTimeMillis());
    }
}
