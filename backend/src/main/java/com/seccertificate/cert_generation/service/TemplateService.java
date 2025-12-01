package com.seccertificate.cert_generation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.seccertificate.cert_generation.model.Template;
import org.springframework.web.multipart.MultipartFile;

public interface TemplateService {
//    void saveTemplate(MultipartFile file, String customerId) throws Exception;
    Template saveTemplate(MultipartFile file, JsonNode schemaNode, String customerId) throws Exception;
    String getCustomerIdForUser(String username);
}
