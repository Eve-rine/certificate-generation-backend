package com.seccertificate.cert_generation.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PdfGeneratorServiceTest {

    @Test
    void generatePdf_fromSimpleHtml_returnsPdfBytes() throws Exception {
        PdfGeneratorService svc = new PdfGeneratorService();
        String html = "<html><body><h1>Hello</h1><p>Test</p></body></html>";
        byte[] pdf = svc.generatePdfFromHtml(html);
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(10);
        // PDF files start with %PDF
        String header = new String(pdf, 0, Math.min(4, pdf.length));
        assertThat(header).contains("%PDF");
    }
}
