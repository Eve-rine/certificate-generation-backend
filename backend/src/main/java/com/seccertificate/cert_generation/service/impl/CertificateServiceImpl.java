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
    private final CertificateRepository customerRepository;

    public CertificateServiceImpl(CertificateRepository certificateRepository, PdfGeneratorService pdfGeneratorService, TemplateRepository templateRepository, CertificateRepository customerRepository) {
        this.certificateRepository = certificateRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.templateRepository = templateRepository;
        this.customerRepository = customerRepository;
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

    @Override
    public byte[] getSignedPdf(String id, String customerId) {
        try {
            Certificate cert = certificateRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new IllegalArgumentException("Certificate not found"));

            String templateIdStr = cert.getTemplateId() != null ? cert.getTemplateId().toString() : null;
            System.out.println("Generating PDF for Certificate ID: " + id + ", Template ID: " + templateIdStr);

            String templateHtml = cert.getTemplateId() != null
                    ? fetchTemplateHtml(templateIdStr)
                    : "<html><body>Certificate for {{student_name}}</body></html>"; // fallback

            // Convert cert.getData() (JsonNode) to JSON string
            String dataJson = cert.getData().toString();

            // Render HTML with data (string replace)
            String renderedHtml = applyDataToTemplate(templateHtml, dataJson);

            // SANITIZE before PDF (critical fix)
            String sanitized = sanitizeHtmlForPdf(renderedHtml);

            // Generate PDF with cleaned HTML
            return pdfGeneratorService.generatePdfFromHtml(sanitized);

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
    public UUID generateAndStore(
            String customerId,
            String templateId,
            String templateHtml,
            String dataJson
    ) throws Exception {

        if (customerId == null) {
            throw new IllegalStateException("No customerId provided");
        }

        String renderedHtml = applyDataToTemplate(templateHtml, dataJson);

        renderedHtml = sanitizeForFop(renderedHtml);

        String safeHtml = sanitizeHtmlForPdf(renderedHtml);
        byte[] pdf = pdfGeneratorService.generatePdfFromHtml(safeHtml);

        String storagePath =
                "minio://" + customerId + "/certificates/" + UUID.randomUUID() + ".pdf";

        Certificate cert = new Certificate();
        cert.setId(UUID.randomUUID());
        cert.setCustomerId(customerId);
        cert.setTemplateId(templateId != null ? UUID.fromString(templateId) : null);
        cert.setData(new ObjectMapper().readTree(dataJson));
        cert.setStoragePath(storagePath);
        cert.setSignature("placeholder-signature");
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

    private String sanitizeForFop(String html) {

        if (html == null) return "";

        return html
                .replaceAll("<br>", "<br/>")
                .replaceAll("<br />", "<br/>")

                .replaceAll("<hr>", "<hr/>")

                .replaceAll("(<img[^>]*)(?<!/)>", "$1/>")

                .replaceAll("(<input[^>]*)(?<!/)>", "$1/>")

                .replaceAll("[^\\x09\\x0A\\x0D\\x20-\\xFFFD]", "");
    }

    private String sanitizeHtmlForPdf(String html) {
        if (html == null) return "";

        return html
                // Correct invalid <br> variations
                .replaceAll("(?i)<br\\s*/*>", "<br/>")
                .replaceAll("(?i)<br\\s*//?>", "<br/>")     // <br //> -> <br/>

                // Fix self-closing tags
                .replaceAll("(?i)(<img[^>]*)(?<!/)>", "$1/>")
                .replaceAll("(?i)(<hr[^>]*)(?<!/)>", "$1/>")
                .replaceAll("(?i)(<input[^>]*)(?<!/)>", "$1/>")

                // Drop invalid XML chars
                .replaceAll("[^\\x09\\x0A\\x0D\\x20-\\xFFFD]", "");
    }

    public void deleteCustomer(String id) {
        customerRepository.deleteById(UUID.fromString(id));
    }
}
