package com.seccertificate.cert_generation.dto;

import com.fasterxml.jackson.databind.JsonNode;

public class TemplateUploadDto {
    private String name;
    private String html;
    private JsonNode schema; // optional

    // getters / setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHtml() { return html; }
    public void setHtml(String html) { this.html = html; }
    public JsonNode getSchema() { return schema; }
    public void setSchema(JsonNode schema) { this.schema = schema; }
}