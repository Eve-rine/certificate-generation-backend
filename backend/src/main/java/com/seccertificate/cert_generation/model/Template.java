package com.seccertificate.cert_generation.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.seccertificate.cert_generation.convert.JsonNodeStringConverter;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "templates")
public class Template {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String html;

    @Column(columnDefinition = "text")
    private String css;

    // use converter to persist JsonNode as text
    @Convert(converter = JsonNodeStringConverter.class)
    @Column(name = "json_schema", columnDefinition = "text")
    private JsonNode jsonSchema;

    private Instant createdAt;

    // getters / setters ...

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(@Nullable String originalFilename) { this.name = originalFilename; }

    public String getHtml() { return html; }
    public void setHtml(String html) { this.html = html; }

    public String getCss() { return css; }
    public void setCss(String css) { this.css = css; }

    public JsonNode getJsonSchema() { return jsonSchema; }
    public void setJsonSchema(JsonNode jsonSchema) { this.jsonSchema = jsonSchema; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // convenience
    public void setContent(String s) { this.html = s; }
}