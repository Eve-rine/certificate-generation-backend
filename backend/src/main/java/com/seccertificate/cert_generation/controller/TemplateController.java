package com.seccertificate.cert_generation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.seccertificate.cert_generation.model.Template;
import com.seccertificate.cert_generation.service.TemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    private final TemplateService templateService;
    private final ObjectMapper objectMapper;


    public TemplateController(TemplateService templateService, ObjectMapper objectMapper) {
        this.templateService = templateService;
        this.objectMapper = objectMapper;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "schema", required = false) String schemaJson,
            Authentication authentication) throws Exception {

        // Parse optional schema JSON string into JsonNode
        JsonNode schemaNode = null;
        if (schemaJson != null && !schemaJson.isBlank()) {
            try {
                schemaNode = objectMapper.readTree(schemaJson);
            } catch (JsonProcessingException e) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Invalid JSON schema", "message", e.getOriginalMessage()));
            }
        }

        // Resolve customerId from authenticated user
        String username = authentication.getName();
        String customerId = templateService.getCustomerIdForUser(username);

        // Persist template (service will sanitize html, validate/auto-generate schema, set createdAt, etc.)
        Template saved = templateService.saveTemplate(file, schemaNode, customerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Template uploaded successfully", "id", saved.getId()));
    }
}
