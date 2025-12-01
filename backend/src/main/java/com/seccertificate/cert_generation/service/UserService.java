package com.seccertificate.cert_generation.service;

import com.seccertificate.cert_generation.model.User;

import java.util.List;

public interface UserService {
    User createUser(String username, String password, String role, String customerId);
    User findByUsername(String username);

    List<User> getAllUsers();
}

