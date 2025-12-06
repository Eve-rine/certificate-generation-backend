package com.seccertificate.cert_generation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seccertificate.cert_generation.dto.GenerateRequest;
import com.seccertificate.cert_generation.model.Certificate;
import com.seccertificate.cert_generation.model.Template;
import com.seccertificate.cert_generation.repository.CertificateRepository;
import com.seccertificate.cert_generation.repository.TemplateRepository;
import com.seccertificate.cert_generation.utils.InMemoryMultipartFile;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Iterator;
import java.util.UUID;

@Service
public class CertificateServiceImpl implements CertificateService {
    private final CertificateRepository certificateRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final TemplateRepository templateRepository;
    private final CertificateRepository customerRepository;
    private final ImageStorageService imageStorageService;
    private CertificateSignatureService signatureService;
    @Value("${storage.local.path:uploads}")
    private String localBasePath;

    public CertificateServiceImpl(CertificateRepository certificateRepository, PdfGeneratorService pdfGeneratorService, TemplateRepository templateRepository,
                                  CertificateRepository customerRepository, ImageStorageService imageStorageService,CertificateSignatureService signatureService) {
        this.signatureService = signatureService;
        this.imageStorageService = imageStorageService;
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

            // Verify ownership
            if (!customerId.equals(cert.getCustomerId())) {
                throw new IllegalStateException("Certificate does not belong to your customer");
            }

            // Check if PDF already exists in storage
            if (cert.getStoragePath() != null && !cert.getStoragePath().isEmpty()) {
                System.out.println("Retrieving cached PDF from storage: " + cert.getStoragePath());
                return readPdfFromLocalStorage(cert.getStoragePath());
            }

            // Fallback: Generate if not exists (shouldn't happen with new flow)
            System.out.println("No cached PDF found, generating new PDF for Certificate ID: " + id);
            return regenerateAndStorePdf(cert);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Read PDF from local file system
     */
    private byte[] readPdfFromLocalStorage(String storagePath) throws IOException {
        // storagePath format: /uploads/customerId/filename.pdf
        String relativePath = storagePath.replace("/uploads/", "");
        Path path = Paths.get(localBasePath, relativePath);

        if (!Files.exists(path)) {
            throw new IOException("PDF file not found at: " + storagePath);
        }

        System.out.println("Reading PDF from: " + path.toAbsolutePath());
        return Files.readAllBytes(path);
    }

    /**
     * Fallback method to regenerate PDF if storage path is missing
     */
    @Transactional
    protected byte[] regenerateAndStorePdf(Certificate cert) throws Exception {
        String templateIdStr = cert.getTemplateId() != null ? cert.getTemplateId().toString() : null;

        String templateHtml = cert.getTemplateId() != null
                ? fetchTemplateHtml(templateIdStr)
                : "<html><body>Certificate for {{student_name}}</body></html>";

        String dataJson = cert.getData().toString();
        String renderedHtml = applyDataToTemplate(templateHtml, dataJson);
        renderedHtml = sanitizeForFop(renderedHtml);
        String sanitized = sanitizeHtmlForPdf(renderedHtml);

        // ADD SIGNATURE/SECURITY CODE BEFORE GENERATING PDF
        // Check if certificate already has a verification code, if not generate it
        String verificationCode = cert.getVerificationCode();
        if (verificationCode == null || verificationCode.isEmpty()) {
            // Generate new signature if missing
            String signature = signatureService.generateSignature(
                    cert.getId().toString(),
                    cert.getCustomerId(),
                    cert.getIssuedAt().toString(),
                    dataJson
            );
            verificationCode = signatureService.generateVerificationCode(signature);

            // Update certificate with new signature
            cert.setSignature(signature);
            cert.setVerificationCode(verificationCode);
        }

        // Add signature to HTML before generating PDF
        String htmlWithSignature = addSignatureToHtml(
                sanitized,
                verificationCode,
                cert.getId().toString(),
                cert.getIssuedAt()
        );

        // Generate PDF with signature
        byte[] pdf = pdfGeneratorService.generatePdfFromHtml(htmlWithSignature);

        // Calculate content hash
        String contentHash = signatureService.calculateContentHash(pdf);
        cert.setContentHash(contentHash);

        // Store it for future use
        MultipartFile pdfFile = new InMemoryMultipartFile(
                "file",
                cert.getId() + ".pdf",
                "application/pdf",
                pdf
        );

        String storagePath = imageStorageService.upload(cert.getCustomerId(), pdfFile);

        // Update certificate with storage path
        cert.setStoragePath(storagePath);
        certificateRepository.save(cert);

        System.out.println("PDF regenerated and stored at: " + storagePath);
        System.out.println("Verification code: " + verificationCode);

        return pdf;
    }

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

        UUID certId = UUID.randomUUID();
        Instant issuedAt = Instant.now();
        String timestamp = issuedAt.toString();

        // Generate cryptographic signature BEFORE PDF generation
        String signature = signatureService.generateSignature(
                certId.toString(),
                customerId,
                timestamp,
                dataJson
        );

        String verificationCode = signatureService.generateVerificationCode(signature);

        // Render HTML with data
        String renderedHtml = applyDataToTemplate(templateHtml, dataJson);
        renderedHtml = sanitizeForFop(renderedHtml);
        String safeHtml = sanitizeHtmlForPdf(renderedHtml);

        // Add signature info to HTML BEFORE PDF generation
        String htmlWithSignature = addSignatureToHtml(
                safeHtml,
                verificationCode,
                certId.toString(),
                issuedAt
        );

        // Generate PDF with signature embedded
        byte[] pdf = pdfGeneratorService.generatePdfFromHtml(htmlWithSignature);

        // Calculate PDF content hash for tamper detection
        String contentHash = signatureService.calculateContentHash(pdf);

        // Convert PDF to MultipartFile for storage
        MultipartFile pdfFile = new InMemoryMultipartFile(
                "file",
                certId + ".pdf",
                "application/pdf",
                pdf
        );

        // Upload to storage
        String storagePath = imageStorageService.upload(customerId, pdfFile);

        // Construct certificate entity with signature data
        Certificate cert = new Certificate();
        cert.setId(certId);
        cert.setCustomerId(customerId);
        cert.setTemplateId(templateId != null ? UUID.fromString(templateId) : null);
        cert.setData(new ObjectMapper().readTree(dataJson));
        cert.setStoragePath(storagePath);
        cert.setSignature(signature);
        cert.setVerificationCode(verificationCode);
        cert.setContentHash(contentHash);
        cert.setIssuedAt(issuedAt);
        cert.setRevoked(false);

        certificateRepository.save(cert);

        System.out.println("Certificate generated with verification code: " + verificationCode);

        return cert.getId();
    }

    /**
     * Add signature information to HTML before PDF generation
     */
    private String addSignatureToHtml(String html, String verificationCode,
                                      String certificateId, Instant issuedAt) {

        String formattedDate = issuedAt.toString().substring(0, 10);

        String signatureHtml = String.format("""
        <div style="position: fixed; bottom: 20px; right: 20px; 
                    padding: 12px 16px; border: 1.5px solid #333; 
                    background: white; border-radius: 6px;
                    font-family: 'Arial', sans-serif; text-align: center;">
            <div style="font-size: 9px; color: #666; letter-spacing: 1px; 
                        margin-bottom: 6px;">
                SECURITY CODE
            </div>
            <div style="font-size: 16px; font-weight: bold; color: #000; 
                        letter-spacing: 2px; font-family: 'Courier New', monospace;">
                %s
            </div>
            <div style="font-size: 7px; color: #999; margin-top: 8px; 
                        padding-top: 6px; border-top: 1px solid #eee;">
                Certificate ID: %s<br/>
                Issued: %s
            </div>
        </div>
        """, verificationCode, certificateId.substring(0, 8), formattedDate);

        if (html.contains("</body>")) {
            return html.replace("</body>", signatureHtml + "</body>");
        } else {
            return html + signatureHtml;
        }
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
