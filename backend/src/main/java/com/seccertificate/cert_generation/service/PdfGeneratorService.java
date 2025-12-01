package com.seccertificate.cert_generation.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

/**
 * Minimal HTML -> PDF generator using OpenHTMLToPDF.
 * In production, run this in worker pool; reuse objects where possible to increase throughput.
 */
@Service
public class PdfGeneratorService {

    public byte[] generatePdfFromHtml(String html) throws Exception {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        }
    }
}
