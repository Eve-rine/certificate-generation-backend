package com.seccertificate.cert_generation.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Customer {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String email;

    // Getters and Setters
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

}
