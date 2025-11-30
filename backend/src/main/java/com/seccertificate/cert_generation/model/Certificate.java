package com.seccertificate.cert_generation.model;


import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "certificates")
public class Certificate {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String tenantId;
    private UUID templateId;
    @Column(columnDefinition = "jsonb")
    private String data;
    private String storagePath;
    private String signature; // digital signature metadata (JWS or PKCS7)
    private Instant issuedAt;
    private boolean revoked;
}
