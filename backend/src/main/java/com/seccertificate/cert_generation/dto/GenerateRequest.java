package com.seccertificate.cert_generation.dto;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for certificate generation requests.
 *
 * Example JSON:
 * {
 *   "templateId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
 *   "data": { "recipientName": "Jane Doe", "course": "Security 101" },
 *   "sync": false
 * }
 */
public class GenerateRequest {

    @NotNull
    @NotEmpty
    private String templateId;

    private String customerId;
    private String templateHtml;
    private String dataJson;

    public String getCustomerId() {
        return customerId;
    }

    public String getTemplateHtml() {
        return templateHtml;
    }

    public String getDataJson() {
        return dataJson;
    }
    /**
     * Arbitrary JSON payload with values to bind into the template.
     * Using JsonNode preserves structure and is easy to pass through to templating engine.
     */
    @NotNull
    private JsonNode data;

    /**
     * If true, the API may attempt synchronous generation and return the PDF (201).
     * If false (or absent), the API enqueues an async job and returns 202 with a job id.
     */
    private Boolean sync = Boolean.FALSE;

    public GenerateRequest() {}

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public Boolean getSync() {
        return sync;
    }

    public void setSync(Boolean sync) {
        this.sync = sync;
    }
}
