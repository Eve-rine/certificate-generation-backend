package com.seccertificate.cert_generation.dto;

import java.time.Instant;

public class TemplateDto {
    private String id;
    private String name;
    private String logoUrl;
    private String signatureUrl;
    private Instant createdAt;
    private String html;

    public void setId(String string) {
        this.id = string;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLogoUrl() {
        return logoUrl;
    }
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
    public String getSignatureUrl() {
        return signatureUrl;
    }
    public void setSignatureUrl(String signatureUrl) {
        this.signatureUrl = signatureUrl;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    public String getHtml() {
        return html;
    }
    public void setHtml(String html) {
        this.html = html;
    }
}
