package com.seccertificate.cert_generation.controller;

import com.seccertificate.cert_generation.dto.GenerateRequest;
import com.seccertificate.cert_generation.model.User;
import com.seccertificate.cert_generation.service.CertificateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {
    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generate(@RequestBody GenerateRequest req) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            String certId = certificateService.generateCertificate(req, user.getCustomerId());
            return ResponseEntity.ok(certId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Certificate generation failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable String id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
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
