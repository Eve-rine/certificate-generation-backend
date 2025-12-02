package com.seccertificate.cert_generation.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seccertificate.cert_generation.dto.GenerateRequest;
import com.seccertificate.cert_generation.model.Certificate;
import com.seccertificate.cert_generation.model.Template;
import com.seccertificate.cert_generation.repository.CertificateRepository;
import com.seccertificate.cert_generation.repository.TemplateRepository;
import com.seccertificate.cert_generation.service.CertificateService;
import com.seccertificate.cert_generation.service.PdfGeneratorService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Iterator;
import java.util.UUID;

@Service
public class CertificateServiceImpl implements CertificateService {
    private final CertificateRepository certificateRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final TemplateRepository templateRepository;

    public CertificateServiceImpl(CertificateRepository certificateRepository, PdfGeneratorService pdfGeneratorService, TemplateRepository templateRepository) {
        this.certificateRepository = certificateRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.templateRepository = templateRepository;
    }

    @Override
    public String generateCertificate(GenerateRequest req, String customerId) {
        try {
            UUID certId = generateAndStore(
                    req.getCustomerId(),
                    req.getTemplateId(),
                    req.getTemplateHtml(),
                    req.getDataJson()
            );
            return certId.toString();
        } catch (Exception e) {
                        throw new RuntimeException("Certificate generation failed due to " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

//    @Override
//    public byte[] getSignedPdf(String id, String tenantId) {
////        return new byte[0];
//        Certificate cert = certificateRepository.findById(UUID.fromString(id))
//                .orElseThrow(() -> new IllegalArgumentException("Certificate not found"));
//
//        // Example: retrieve PDF from storage (replace with actual logic)
//        // byte[] pdf = storageService.get(cert.getStoragePath());
//
//        // For testing, return a minimal valid PDF
//        return "%PDF-1.4\n%âãÏÓ\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF".getBytes();
//    }
    @Override
    public byte[] getSignedPdf(String id, String customerId) {
        try {

            Certificate cert = certificateRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found"));

        // Assume cert.getTemplateId() and cert.getData() are available
        // You may need to fetch the template HTML using the templateId
        String templateIdStr = cert.getTemplateId() != null ? cert.getTemplateId().toString() : null;
        System.out.println("Generating PDF for Certificate ID: " + id + ", Template ID: " + templateIdStr);

        String templateHtml = cert.getTemplateId() != null
                ? fetchTemplateHtml(templateIdStr)
                : "<html><body>Certificate for {{student_name}}</body></html>"; // fallback

        // Convert cert.getData() (JsonNode) to JSON string
        String dataJson = cert.getData().toString();

        // Render HTML with data
        String renderedHtml = applyDataToTemplate(templateHtml, dataJson);

        // Generate PDF
        return pdfGeneratorService.generatePdfFromHtml(renderedHtml);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signed PDF: " + e.getMessage(), e);
        }
    }

    // Example method to fetch template HTML (implement as needed)
    private String fetchTemplateHtml(String templateId) {
        Template template = templateRepository.findById(UUID.fromString(templateId))
                .orElseThrow(() -> new IllegalArgumentException("Template not found for ID: " + templateId));
        return template.getHtml();
    }



    @Transactional
    public UUID generateAndStore(String customerId,String templateId, String templateHtml, String dataJson) throws Exception {
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
        cert.setTemplateId(templateId != null ? UUID.fromString(templateId) : null);
        ObjectMapper mapper = new ObjectMapper();
        cert.setData(mapper.readTree(dataJson));
        cert.setStoragePath(storagePath);
        cert.setSignature(signature);
        cert.setIssuedAt(Instant.now());
        cert.setRevoked(false);

        certificateRepository.save(cert);

        return cert.getId();
    }

// Example dataJson: {"student_name":"John Doe","course_name":"Java Basics","completion_date":"2024-06-10","certificate_number":"ABC123"}
    public String applyDataToTemplate(String html, String dataJson) throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(dataJson);
            String rendered = html;
            for (Iterator<String> it = data.fieldNames(); it.hasNext(); ) {
                String key = it.next();
                String value = data.get(key).asText();
                rendered = rendered.replace("{{" + key + "}}", value);
            }
            return rendered;
        }

}
