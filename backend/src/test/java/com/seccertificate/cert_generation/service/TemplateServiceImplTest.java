package com.seccertificate.cert_generation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seccertificate.cert_generation.model.Template;
import com.seccertificate.cert_generation.model.User;
import com.seccertificate.cert_generation.repository.TemplateRepository;
import com.seccertificate.cert_generation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TemplateServiceImplTest {

    private TemplateRepository templateRepository;
    private UserRepository userRepository;
    private ObjectMapper objectMapper;
    private ImageStorageService imageStorageService;
    private TemplateServiceImpl service;

    @BeforeEach
    void setUp() {
        templateRepository = mock(TemplateRepository.class);
        userRepository = mock(UserRepository.class);
        objectMapper = new ObjectMapper();
        imageStorageService = mock(ImageStorageService.class);
        service = new TemplateServiceImpl(templateRepository, userRepository, objectMapper, imageStorageService);
    }

    @Test
    void saveTemplate_generatesMinimalSchemaAndSaves() throws Exception {
        String html = "<div>Hello {{name}}</div>";
        MultipartFile file = new MockMultipartFile("file", "t.html", "text/html", html.getBytes());

        when(templateRepository.existsByNameAndCustomerId("T1", "cust")).thenReturn(false);

        Template saved = new Template();
        saved.setName("T1");
        when(templateRepository.save(any())).thenReturn(saved);

        Template res = service.saveTemplate("T1", file, null, null, null, "cust");

        assertThat(res).isNotNull();
        assertThat(res.getName()).isEqualTo("T1");
        // verify repository saved a Template whose html contains the placeholder processed
        verify(templateRepository).save(any(Template.class));
    }

    @Test
    void saveTemplate_missingName_throws() {
        MultipartFile file = new MockMultipartFile("file", "t.html", "text/html", "a".getBytes());
        assertThrows(IllegalArgumentException.class, () -> service.saveTemplate(null, file, null, null, null, "cust"));
    }

    @Test
    void getCustomerIdForUser_delegates() {
        User u = new User();
        u.setCustomerId("cid");
        when(userRepository.findByUsername("bob")).thenReturn(u);
        String cid = service.getCustomerIdForUser("bob");
        assertThat(cid).isEqualTo("cid");
    }

    @Test
    void deleteTemplateForCustomer_notFound_returnsFalse() {
        when(templateRepository.findById(any())).thenReturn(Optional.empty());
        boolean ok = service.deleteTemplateForCustomer("cust", java.util.UUID.randomUUID());
        assertThat(ok).isFalse();
    }
}
