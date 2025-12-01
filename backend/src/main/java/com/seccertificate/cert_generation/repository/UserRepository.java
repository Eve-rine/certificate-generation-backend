package com.seccertificate.cert_generation.repository;

import com.seccertificate.cert_generation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}
