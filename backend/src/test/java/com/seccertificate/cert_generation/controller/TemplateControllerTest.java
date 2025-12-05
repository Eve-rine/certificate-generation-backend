package com.seccertificate.cert_generation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seccertificate.cert_generation.dto.TemplateDto;
import com.seccertificate.cert_generation.model.Template;
import com.seccertificate.cert_generation.service.TemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateControllerTest {

    @Mock
    TemplateService templateService;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    TemplateController templateController;

    @Mock
    MultipartFile file;

    @Mock
    MultipartFile logo;

    @Mock
    MultipartFile signature;

    @Mock
    Authentication authentication;

    @BeforeEach
    void setup() {
        when(authentication.getName()).thenReturn("testuser");
        when(templateService.getCustomerIdForUser("testuser")).thenReturn("cid");
    }

    @Test
    void uploadTemplate_valid_returnsCreated() throws Exception {
        Template saved = new Template();
        UUID uuid = UUID.randomUUID();
        saved.setId(uuid);

        when(templateService.saveTemplate(anyString(), any(MultipartFile.class), any(), any(), any(), anyString()))
                .thenReturn(saved);

        var resp = templateController.uploadTemplate("test", file, logo, signature, null, authentication);
        assertEquals(201, resp.getStatusCode().value());
        Map body = (Map) resp.getBody();
        assertEquals(uuid, body.get("id"));
        verify(templateService).saveTemplate(eq("test"), eq(file), eq(logo), eq(signature), isNull(), eq("cid"));
    }

    @Test
    void listTemplates_returnsList() {
        TemplateDto dto = new TemplateDto();
        dto.setName("Template1");
        when(templateService.listTemplatesForCustomer("cid"))
                .thenReturn(Collections.singletonList(dto));
        List<TemplateDto> result = templateController.listTemplates(authentication).getBody();
        assertEquals(1, result.size());
        assertEquals("Template1", result.get(0).getName());
        verify(templateService).listTemplatesForCustomer("cid");
    }

    @Test
    void getTemplate_returnsDto() {
        UUID uuid = UUID.randomUUID();
        TemplateDto dto = new TemplateDto();
        dto.setName("TemplateX");
        when(templateService.getTemplateForCustomer("cid", uuid)).thenReturn(dto);
        var resp = templateController.getTemplate(uuid.toString(), authentication);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(dto, resp.getBody());
        verify(templateService).getTemplateForCustomer("cid", uuid);
    }

    @Test
    void updateTemplate_returnsUpdated() throws Exception {
        Template updated = new Template();
        UUID uuid = UUID.randomUUID();
        updated.setId(uuid);
        when(templateService.updateTemplate(anyString(), anyString(), any(), any(), any(), anyString()))
                .thenReturn(updated);
        var resp = templateController.updateTemplate(uuid.toString(), "NewName", file, logo, signature, authentication);
        assertEquals(200, resp.getStatusCode().value());
        Map body = (Map) resp.getBody();
        assertEquals(uuid, body.get("id"));
        verify(templateService).updateTemplate(eq(uuid.toString()), eq("NewName"), eq(file), eq(logo), eq(signature), eq("cid"));
    }

    @Test
    void deleteTemplate_returnsOk() {
        UUID uuid = UUID.randomUUID();
        when(templateService.deleteTemplateForCustomer("cid", uuid)).thenReturn(true);
        var resp = templateController.deleteTemplate(uuid.toString(), authentication);
        assertEquals(200, resp.getStatusCode().value());
        Map body = (Map) resp.getBody();
        assertEquals("Template deleted", body.get("message"));
        verify(templateService).deleteTemplateForCustomer("cid", uuid);
    }
}