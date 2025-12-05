package com.seccertificate.cert_generation.service;

import com.seccertificate.cert_generation.dto.GenerateRequest;

import java.util.UUID;

public interface CertificateService {
    String generateCertificate(GenerateRequest req, String customerId);
    byte[] getSignedPdf(String id, String tenantId);
    void deleteCustomer(String id);
}

