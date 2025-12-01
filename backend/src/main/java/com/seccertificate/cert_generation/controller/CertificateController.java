package com.seccertificate.cert_generation.controller;

import com.seccertificate.cert_generation.dto.GenerateRequest;
import com.seccertificate.cert_generation.service.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {
    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generate(@RequestBody GenerateRequest req, @RequestParam String tenantId) {
        String certId = certificateService.generateCertificate(req, tenantId);
        return ResponseEntity.ok(certId);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable String id, @RequestParam String tenantId) {
        byte[] pdf = certificateService.getSignedPdf(id, tenantId);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
}

