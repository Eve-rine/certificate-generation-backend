package com.seccertificate.cert_generation.controller;

import com.seccertificate.cert_generation.dto.GenerateRequest;
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
    public ResponseEntity<String> generate(@RequestBody Map<String, String> body) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (String) auth.getPrincipal();

            User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new IllegalStateException("User not found");
            }

            String customerId = user.getCustomerId();
            // log customerId
            String templateHtml = templateRepository.findLatestByCustomerId(customerId).getHtml();
            String templateId = templateRepository.findLatestByCustomerId(customerId).getId().toString();
            if (templateHtml == null) {
                throw new IllegalStateException("No template HTML found for customer");
            }
            System.out.println("Using template HTML: " + templateHtml);

            String dataJson = body.get("dataJson");
            GenerateRequest req = new GenerateRequest();
            req.setCustomerId(customerId);
            req.setTemplateId(templateId);
            req.setTemplateHtml(templateHtml);
            req.setDataJson(dataJson);

            String certId = certificateService.generateCertificate(req, customerId);
            return ResponseEntity.ok(certId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Certificate generation failed: " + e.getMessage());
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
}
