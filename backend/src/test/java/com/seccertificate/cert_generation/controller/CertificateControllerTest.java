package com.seccertificate.cert_generation.controller;

import com.seccertificate.cert_generation.model.Template;
import com.seccertificate.cert_generation.model.User;
import com.seccertificate.cert_generation.repository.TemplateRepository;
import com.seccertificate.cert_generation.repository.UserRepository;
import com.seccertificate.cert_generation.service.CertificateService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateControllerTest {

    @Mock CertificateService certificateService;
    @Mock TemplateRepository templateRepository;
    @Mock UserRepository userRepository;

    @InjectMocks CertificateController certificateController;

    @Mock SecurityContext securityContext;
    @Mock Authentication authentication;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void generate_valid_returnsSuccess() {
        String username = "testuser";
        String customerId = "cid";
        String templateId = UUID.randomUUID().toString();
        String certId = "CERT123";
        Map<String, String> body = new HashMap<>();
        body.put("templateId", templateId);
        body.put("dataJson", "{}");

        User user = new User();
        user.setUsername(username);
        user.setCustomerId(customerId);
        Template template = new Template();
        template.setId(UUID.fromString(templateId));
        template.setCustomerId(customerId);
        template.setHtml("<html></html>");

        when(authentication.getPrincipal()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(user);
        when(templateRepository.findById(UUID.fromString(templateId)))
                .thenReturn(Optional.of(template));
        when(certificateService.generateCertificate(any(), eq(customerId))).thenReturn(certId);

        ResponseEntity<Map<String, Object>> resp = certificateController.generate(body);

        assertEquals(200, resp.getStatusCode().value());
        assertTrue((Boolean)resp.getBody().get("success"));
        assertEquals(certId, resp.getBody().get("id"));

        verify(userRepository).findByUsername(username);
        verify(templateRepository).findById(UUID.fromString(templateId));
        verify(certificateService).generateCertificate(any(), eq(customerId));
    }

    @Test
    void generate_invalidUser_returnsError() {
        when(authentication.getPrincipal()).thenReturn("nouser");
        when(userRepository.findByUsername("nouser")).thenReturn(null);
        Map<String, String> body = Map.of("templateId", UUID.randomUUID().toString());
        ResponseEntity<Map<String, Object>> resp = certificateController.generate(body);
        assertEquals(400, resp.getStatusCode().value());
        assertFalse((Boolean)resp.getBody().get("success"));
    }

    // Add similar for getPdf, getSchema, covering failure branches as well
}