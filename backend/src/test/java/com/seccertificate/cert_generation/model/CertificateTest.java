package com.seccertificate.cert_generation.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CertificateTest {

    @Test
    void gettersAndSetters_WorkCorrectly() throws Exception {
        Certificate cert = new Certificate();

        UUID id = UUID.randomUUID();
        UUID tid = UUID.randomUUID();
        String customerId = "cid";
        String storage = "/path";
        String signature = "sig";
        Instant now = Instant.now();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode data = mapper.readTree("{\"foo\":1}");

        cert.setId(id);
        cert.setTemplateId(tid);
        cert.setCustomerId(customerId);
        cert.setStoragePath(storage);
        cert.setSignature(signature);
        cert.setIssuedAt(now);
        cert.setData(data);
        cert.setRevoked(true);

        assertEquals(id, cert.getId());
        assertEquals(tid, cert.getTemplateId());
        assertEquals(customerId, cert.getCustomerId());
        assertEquals(storage, cert.getStoragePath());
        assertEquals(signature, cert.getSignature());
        assertEquals(now, cert.getIssuedAt());
        assertEquals(data, cert.getData());
        cert.setRevoked(false);
    }
}