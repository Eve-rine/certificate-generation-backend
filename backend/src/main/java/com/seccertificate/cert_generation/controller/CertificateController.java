package com.seccertificate.cert_generation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seccertificate.cert_generation.dto.GenerateRequest;
import com.seccertificate.cert_generation.model.Template;
import com.seccertificate.cert_generation.model.User;
import com.seccertificate.cert_generation.repository.TemplateRepository;
import com.seccertificate.cert_generation.repository.UserRepository;
import com.seccertificate.cert_generation.service.CertificateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {
    private final CertificateService certificateService;
    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;


    public CertificateController(CertificateService certificateService, TemplateRepository templateRepository, UserRepository userRepository) {
        this.certificateService = certificateService;
        this.templateRepository = templateRepository;
        this.userRepository = userRepository;

    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@RequestBody Map<String, String> body) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (String) auth.getPrincipal();

            User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new IllegalStateException("User not found");
            }

            String customerId = user.getCustomerId();

            // --- Use templateId from request ---
            String templateIdStr = body.get("templateId");
            if (templateIdStr == null || templateIdStr.isBlank()) {
                throw new IllegalArgumentException("templateId is required");
            }

            UUID templateId = UUID.fromString(templateIdStr);
            Template template = templateRepository.findById(templateId)
                    .orElseThrow(() -> new IllegalArgumentException("Template not found"));

            if (!customerId.equals(template.getCustomerId())) {
                throw new IllegalStateException("Template does not belong to your customer");
            }

            String templateHtml = template.getHtml();
            if (templateHtml == null) {
                throw new IllegalStateException("Template HTML is empty");
            }

            String dataJson = body.get("dataJson");

            GenerateRequest req = new GenerateRequest();
            req.setCustomerId(customerId);
            req.setTemplateId(templateId.toString());
            req.setTemplateHtml(templateHtml);
            req.setDataJson(dataJson);

            String certId = certificateService.generateCertificate(req, customerId);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "id", certId,
                    "message", "Certificate generated successfully"
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                    "success", false,
                    "message", "Certificate generation failed: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(error);
        }
    }


    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable String id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (String) auth.getPrincipal();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new IllegalStateException("User not found");
            }
            byte[] pdf = certificateService.getSignedPdf(id, user.getCustomerId());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate.pdf\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

@GetMapping("/schema")
public ResponseEntity<Object> getSchema(@RequestParam("templateId") UUID  templateId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = (String) auth.getPrincipal();
    User user = userRepository.findByUsername(username);
    if (user == null) {
        return ResponseEntity.badRequest().body("User not found");
    }
    String customerId = user.getCustomerId();

    var template = templateRepository.findById(templateId).orElse(null);

    if (template == null || template.getJsonSchema() == null) {
        return ResponseEntity.badRequest().body("No schema found for template");
    }
    if (!customerId.equals(template.getCustomerId())) {
        return ResponseEntity.badRequest().body("Template does not belong to your customer");
    }
    Object schemaObj = new ObjectMapper().convertValue(template.getJsonSchema(), Map.class);
    return ResponseEntity.ok(schemaObj);
}


}
