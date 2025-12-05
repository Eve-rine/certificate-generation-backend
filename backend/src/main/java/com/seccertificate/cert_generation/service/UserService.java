package com.seccertificate.cert_generation.service;

import com.seccertificate.cert_generation.dto.UserDto;
import com.seccertificate.cert_generation.model.User;

import java.util.List;

public interface UserService {
    User createUser(String username,String name, String password, String role, String customerId);
    User findByUsername(String username);

    List<UserDto> getAllUsers();

    void deleteUser(String id);
    User updateUser(String id, User user);
}

