package com.seccertificate.cert_generation.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTests {

    @Test
    void userDto_gettersAndSetters() {
        UserDto dto = new UserDto();
        dto.setId("i1");
        dto.setName("Name");
        dto.setUsername("usr");
        dto.setRole("R");
        dto.setCustomerId("c1");

        assertThat(dto.getId()).isEqualTo("i1");
        assertThat(dto.getName()).isEqualTo("Name");
        assertThat(dto.getUsername()).isEqualTo("usr");
        assertThat(dto.getRole()).isEqualTo("R");
        assertThat(dto.getCustomerId()).isEqualTo("c1");
    }

    @Test
    void templateDto_gettersAndSetters() {
        TemplateDto t = new TemplateDto();
        t.setId("t1");
        t.setName("T");
        t.setHtml("<p/>\n");
        t.setLogoUrl("/l.png");
        t.setSignatureUrl("/s.png");
        Instant now = Instant.now();
        t.setCreatedAt(now);

        assertThat(t.getId()).isEqualTo("t1");
        assertThat(t.getName()).isEqualTo("T");
        assertThat(t.getHtml()).isEqualTo("<p/>\n");
        assertThat(t.getLogoUrl()).isEqualTo("/l.png");
        assertThat(t.getSignatureUrl()).isEqualTo("/s.png");
        assertThat(t.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void loginResponse_accessors() {
        LoginResponse r = new LoginResponse("tok", 12345L, "ROLE_USER");
        assertThat(r.getAccessToken()).isEqualTo("tok");
        assertThat(r.getExpiry()).isEqualTo(12345L);
        assertThat(r.getRole()).isEqualTo("ROLE_USER");
        assertThat(r.getToken()).isEqualTo("tok");
    }

    @Test
    void generateRequest_andTemplateUploadDto() throws Exception {
        ObjectMapper om = new ObjectMapper();
        JsonNode node = om.readTree("{\"a\":1}");

        GenerateRequest g = new GenerateRequest();
        g.setTemplateId("tmpl");
        g.setData(node);
        g.setSync(Boolean.TRUE);
        g.setCustomerId("c");
        g.setTemplateHtml("<html/>");
        g.setDataJson("{\"a\":1}");

        assertThat(g.getTemplateId()).isEqualTo("tmpl");
        assertThat(g.getData()).isEqualTo(node);
        assertThat(g.getSync()).isTrue();
        assertThat(g.getCustomerId()).isEqualTo("c");

        TemplateUploadDto tu = new TemplateUploadDto();
        tu.setName("n");
        tu.setHtml("h");
        tu.setSchema(node);

        assertThat(tu.getName()).isEqualTo("n");
        assertThat(tu.getHtml()).isEqualTo("h");
        assertThat(tu.getSchema()).isEqualTo(node);
    }
}
