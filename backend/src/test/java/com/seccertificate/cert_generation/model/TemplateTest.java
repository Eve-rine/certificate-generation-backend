package com.seccertificate.cert_generation.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TemplateTest {
    @Test
    void gettersAndSetters_WorkCorrectly() throws Exception {
        Template template = new Template();

        UUID id = UUID.randomUUID();
        String customerId = "cid";
        String name = "Cert";
        String html = "<html>";
        String css = ".style{}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonSchema = mapper.readTree("{\"required\":[]}");
        String logoUrl = "logo.png";
        String signatureUrl = "sign.png";
        Instant created = Instant.now();
        Instant updated = Instant.now();

        template.setId(id);
        template.setCustomerId(customerId);
        template.setName(name);
        template.setHtml(html);
        template.setCss(css);
        template.setJsonSchema(jsonSchema);
        template.setLogoUrl(logoUrl);
        template.setSignatureUrl(signatureUrl);
        template.setCreatedAt(created);
        template.setUpdatedAt(updated);

        assertEquals(id, template.getId());
        assertEquals(customerId, template.getCustomerId());
        assertEquals(name, template.getName());
        assertEquals(html, template.getHtml());
        assertEquals(css, template.getCss());
        assertEquals(jsonSchema, template.getJsonSchema());
        assertEquals(logoUrl, template.getLogoUrl());
        assertEquals(signatureUrl, template.getSignatureUrl());
        assertEquals(created, template.getCreatedAt());
        assertEquals(updated, template.getUpdatedAt());
    }
}