package com.seccertificate.cert_generation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.seccertificate.cert_generation.dto.TemplateDto;
import com.seccertificate.cert_generation.model.Template;
import com.seccertificate.cert_generation.service.TemplateService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestParam(value = "signature", required = false) MultipartFile signature,
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

        // Persist template (service will sanitize html, validate/auto-generate schema, assign images, set createdAt)
        Template saved = templateService.saveTemplate(name, file, logo, signature, schemaNode, customerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Template uploaded successfully", "id", saved.getId()));
    }

    // GET /api/templates (lists all for current user)
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TemplateDto>> listTemplates(Authentication authentication) {
        String username = authentication.getName();
        String customerId = templateService.getCustomerIdForUser(username);
        List<TemplateDto> templates = templateService.listTemplatesForCustomer(customerId);
        return ResponseEntity.ok(templates);
    }

    // GET /api/templates/{id} (fetch one by id)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TemplateDto> getTemplate(@PathVariable String id, Authentication authentication) {
        String username = authentication.getName();
        String customerId = templateService.getCustomerIdForUser(username);
        TemplateDto t = templateService.getTemplateForCustomer(customerId, UUID.fromString(id));
        if (t == null) return ResponseEntity.status(404).build();
        return ResponseEntity.ok(t);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateTemplate(
            @PathVariable("id") String id,
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestParam(value = "signature", required = false) MultipartFile signature,
            Authentication authentication
    ) throws Exception {
        String username = authentication.getName();
        String customerId = templateService.getCustomerIdForUser(username);

        Template updated = templateService.updateTemplate(id, name, file, logo, signature, customerId);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Template not found or access denied"));
        }
        return ResponseEntity.ok(Map.of("message", "Template updated", "id", updated.getId()));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(
            @PathVariable String id,
            Authentication authentication
    ) {
        String username = authentication.getName();
        String customerId = templateService.getCustomerIdForUser(username);

        boolean deleted = templateService.deleteTemplateForCustomer(customerId, UUID.fromString(id));
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Template not found or access denied"));
        }
        return ResponseEntity.ok(Map.of("message", "Template deleted"));
    }

}
