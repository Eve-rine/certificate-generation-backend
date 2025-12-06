package com.seccertificate.cert_generation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class CertificateSignatureService {

    @Value("${certificate.signature.secret-key}")
    private String secretKey;

    /**
     * Generate a cryptographic signature for a certificate
     * Signature = HMAC-SHA256(certificateId + customerId + timestamp + content)
     */
    public String generateSignature(String certificateId, String customerId,
                                    String timestamp, String content) {
        try {
            String data = certificateId + "|" + customerId + "|" + timestamp + "|" + content;

            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256Hmac.init(secretKeySpec);

            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * Generate a short verification code for display (like "A3F-G7K-M9P")
     */
    public String generateVerificationCode(String signature) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(signature.getBytes());

            // Convert to base32-like string and take first 9 characters
            String encoded = Base64.getEncoder().encodeToString(hash)
                    .toUpperCase()
                    .replaceAll("[^A-Z0-9]", "")
                    .substring(0, 9);

            return String.format("%s-%s-%s",
                    encoded.substring(0, 3),
                    encoded.substring(3, 6),
                    encoded.substring(6, 9));

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate verification code", e);
        }
    }

    /**
     * Calculate SHA-256 hash of content (for tamper detection)
     */
    public String calculateContentHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hash", e);
        }
    }
}