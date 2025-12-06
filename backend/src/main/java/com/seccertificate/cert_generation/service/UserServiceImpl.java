package com.seccertificate.cert_generation.service;

import com.seccertificate.cert_generation.dto.UserDto;
import com.seccertificate.cert_generation.model.User;
import com.seccertificate.cert_generation.repository.UserRepository;
import com.seccertificate.cert_generation.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(String username, String name, String password, String role, String customerId) {
        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setCustomerId(customerId);
        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setUsername(user.getUsername());
                    dto.setRole(user.getRole());
                    dto.setCustomerId(user.getCustomerId());
                    return dto;
                })
                .toList();
    }


    @Override
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    @Override
    public User updateUser(String id, User user) {
        User existing = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        existing.setName(user.getName());
        existing.setUsername(user.getUsername());
        existing.setRole(user.getRole());
        existing.setCustomerId(user.getCustomerId());
        // Optionally update password if provided
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(existing);
    }
}
