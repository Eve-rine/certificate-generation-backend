package com.seccertificate.cert_generation.service;

import com.seccertificate.cert_generation.dto.GenerateRequest;

public interface CertificateService {
    String generateCertificate(GenerateRequest req, String tenantId);
    byte[] getSignedPdf(String id, String tenantId);
}

