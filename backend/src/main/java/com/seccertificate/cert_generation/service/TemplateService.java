package com.seccertificate.cert_generation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.seccertificate.cert_generation.dto.TemplateDto;
import com.seccertificate.cert_generation.model.Template;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface TemplateService {
    Template saveTemplate( String name,
                               MultipartFile file,
                               MultipartFile logo,
                               MultipartFile signature,
                               JsonNode schemaNode,
                               String customerId) throws Exception;
    String getCustomerIdForUser(String username);
    List<TemplateDto> listTemplatesForCustomer(String customerId);
    TemplateDto getTemplateForCustomer(String customerId, UUID templateId);
    Template updateTemplate(String templateId, String name, MultipartFile file, MultipartFile logo, MultipartFile signature, String customerId) throws Exception;

    boolean deleteTemplateForCustomer(String customerId, UUID uuid);
}
