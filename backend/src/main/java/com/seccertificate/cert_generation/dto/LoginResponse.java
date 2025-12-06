package com.seccertificate.cert_generation.dto;

public class LoginResponse {
    private String accessToken;
    private long expiry;
    private String role;

    public LoginResponse(String accessToken, long expiry, String role) {
        this.accessToken = accessToken;
        this.expiry = expiry;
        this.role = role;

    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiry() {
        return expiry;
    }
    public String getRole() {
        return role;
    }

    public String getToken() {
        return accessToken;
    }
}
