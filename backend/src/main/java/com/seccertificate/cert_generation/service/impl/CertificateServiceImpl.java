package com.seccertificate.cert_generation.service.impl;

import com.seccertificate.cert_generation.dto.GenerateRequest;
import com.seccertificate.cert_generation.model.Certificate;
import com.seccertificate.cert_generation.repository.CertificateRepository;
import com.seccertificate.cert_generation.service.CertificateService;
import com.seccertificate.cert_generation.service.PdfGeneratorService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CertificateServiceImpl implements CertificateService {
    private final CertificateRepository certificateRepository;
    private final PdfGeneratorService pdfGeneratorService;

    public CertificateServiceImpl(CertificateRepository certificateRepository, PdfGeneratorService pdfGeneratorService) {
        this.certificateRepository = certificateRepository;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @Override
    public String generateCertificate(GenerateRequest req, String customerId) {
        try {
            UUID certId = generateAndStore(
                    req.getCustomerId(),
                    req.getTemplateHtml(),
                    req.getDataJson()
            );
            return certId.toString();
        } catch (Exception e) {
                        throw new RuntimeException("Certificate generation failed due to " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] getSignedPdf(String id, String tenantId) {
        return new byte[0];
    }

    @Transactional
    public UUID generateAndStore(String customerId, String templateHtml, String dataJson) throws Exception {
        if (customerId == null) {
            throw new IllegalStateException("No customerId provided");
        }

        String renderedHtml = applyDataToTemplate(templateHtml, dataJson);
        byte[] pdf = pdfGeneratorService.generatePdfFromHtml(renderedHtml);

        String signature = "placeholder-signature";
        String storagePath = "minio://" + customerId + "/certificates/" + UUID.randomUUID() + ".pdf";

        Certificate cert = new Certificate();
        cert.setId(UUID.randomUUID());
        cert.setCustomerId(customerId);
        cert.setTemplateId(null);
        cert.setData(dataJson);
        cert.setStoragePath(storagePath);
        cert.setSignature(signature);
        cert.setIssuedAt(Instant.now());
        cert.setRevoked(false);

        certificateRepository.save(cert);

        return cert.getId();
    }

    private String applyDataToTemplate(String html, String dataJson) {
        return html.replace("{{data}}", dataJson);
    }
}
