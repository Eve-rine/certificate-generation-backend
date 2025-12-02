package com.seccertificate.cert_generation.model;


import com.fasterxml.jackson.databind.JsonNode;
import com.seccertificate.cert_generation.convert.JsonNodeConverter;
import com.seccertificate.cert_generation.convert.JsonNodeStringConverter;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "certificates")
public class Certificate {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String customerId;
    private UUID templateId;
    @Convert(converter = JsonNodeStringConverter.class)
    @Column(name = "data", columnDefinition = "text")
    private JsonNode data;
    private String storagePath;
    private String signature; // digital signature metadata (JWS or PKCS7)
    private Instant issuedAt;
    private boolean revoked;

    public void setId(UUID uuid) {
        this.id = uuid;
    }
    public UUID getId() {
        return id;
    }
    public String getCustomerId() {
        return customerId;
    }
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    public UUID getTemplateId() {
        return templateId;
    }
    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }
    public JsonNode getData() {
        return data;
    }
    public void setData(JsonNode data) {
        this.data = data;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
    public String getStoragePath() {
        return storagePath;
    }
    public String getSignature() {
        return signature;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }
    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant now) {
        this.issuedAt = now;
    }

    public void setRevoked(boolean b) {
        this.revoked = b;
    }
}
