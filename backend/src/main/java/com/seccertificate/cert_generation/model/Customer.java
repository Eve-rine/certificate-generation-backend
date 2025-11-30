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
}
