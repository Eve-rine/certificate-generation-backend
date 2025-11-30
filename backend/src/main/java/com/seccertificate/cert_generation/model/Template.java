package com.seccertificate.cert_generation.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "templates")
public class Template {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String customerId;
    @Column(nullable = false)
    private String name;
    @Column(columnDefinition = "text")
    private String html;
    @Column(columnDefinition = "text")
    private String css;
    @Column(columnDefinition = "jsonb")
    private String jsonSchema;
    private Instant createdAt;
}