package com.seccertificate.cert_generation.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seccertificate.cert_generation.dto.TemplateDto;
import com.seccertificate.cert_generation.model.Template;
import com.seccertificate.cert_generation.repository.TemplateRepository;
import com.seccertificate.cert_generation.repository.UserRepository;
import com.seccertificate.cert_generation.service.ImageStorageService;
import com.seccertificate.cert_generation.service.TemplateService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of TemplateService with:
 * - HTML sanitization (jsoup)
 * - Optional JSON Schema handling (JsonNode)
 * - Auto-generation of a minimal JSON Schema when none is provided
 * - Basic size limits and placeholder extraction
 */
@Service
public class TemplateServiceImpl implements TemplateService {

    private static final int MAX_HTML_BYTES = 200_000;   // 200 KB
    private static final int MAX_SCHEMA_BYTES = 100_000; // 100 KB

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_\\-.]+)\\s*}}");

    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ImageStorageService imageStorageService;


    public TemplateServiceImpl(TemplateRepository templateRepository,
                               UserRepository userRepository,
                               ObjectMapper objectMapper,
                               ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
        this.templateRepository = templateRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * New method used by controller: accepts multipart HTML file plus an optional JsonNode schema.
     * Performs sanitization, placeholder extraction, minimal-schema generation (when needed),
     * and persists the Template (jsonSchema stored as JsonNode -> jsonb).
     *
     * Returns the persisted Template.
     */

    @Override
    public Template saveTemplate(
            String name,
            MultipartFile file,
            MultipartFile logo,
            MultipartFile signature,
            JsonNode schemaNode,
            String customerId
    ) throws Exception {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Template name is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Template file is required");
        }
        if (file.getSize() > MAX_HTML_BYTES) {
            throw new IllegalArgumentException("Template file too large");
        }
        if (templateRepository.existsByNameAndCustomerId(name, customerId)) {
            throw new IllegalArgumentException("Template name must be unique per customer");
        }

        String rawHtml = new String(file.getBytes(), StandardCharsets.UTF_8);

        String cleanedHtml = Jsoup.clean(
                rawHtml,
                Safelist.relaxed()
                        .preserveRelativeLinks(true)
                        .addAttributes(":all", "class", "id", "style")
        );

        Document xhtmlDoc = Jsoup.parse(cleanedHtml);
        xhtmlDoc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)     // force XHTML mode
                .escapeMode(Entities.EscapeMode.xhtml)
                .prettyPrint(false);

        String safeHtml = xhtmlDoc.html();

        safeHtml = safeHtml.replaceAll("(?i)<br\\b([^>]*)>", "<br$1/>");

        String lower = safeHtml.toLowerCase();
        if (!lower.contains("<html")) {
            safeHtml = "<html><body>" + safeHtml + "</body></html>";
        } else if (!lower.contains("<body")) {
            safeHtml = safeHtml.replaceFirst("(?i)(<html[^>]*>)", "$1<body>") + "</body>";
        }

        Set<String> placeholders = extractPlaceholders(safeHtml);

        if (schemaNode == null) {
            schemaNode = generateMinimalSchema(placeholders);
        } else {
            String schemaStr = objectMapper.writeValueAsString(schemaNode);
            if (schemaStr.getBytes(StandardCharsets.UTF_8).length > MAX_SCHEMA_BYTES) {
                throw new IllegalArgumentException("Provided schema is too large");
            }
        }

        String logoUrl = null;
        if (logo != null && !logo.isEmpty()) {
            logoUrl = imageStorageService.upload(customerId, logo);
        }

        String signatureUrl = null;
        if (signature != null && !signature.isEmpty()) {
            signatureUrl = imageStorageService.upload(customerId, signature);
        }

        Template template = new Template();
        template.setCustomerId(customerId);
        template.setName(name);
        template.setHtml(safeHtml);
        template.setJsonSchema(schemaNode);
        template.setLogoUrl(logoUrl);
        template.setSignatureUrl(signatureUrl);
        template.setCreatedAt(Instant.now());

        return templateRepository.save(template);
    }


    @Override
    public String getCustomerIdForUser(String username) {
        return userRepository.findByUsername(username).getCustomerId();
    }

    private Set<String> extractPlaceholders(String html) {
        Matcher m = PLACEHOLDER.matcher(html);
        Set<String> found = new HashSet<>();
        while (m.find()) {
            found.add(m.group(1));
        }
        return found;
    }

    private JsonNode generateMinimalSchema(Set<String> placeholders) {
        // { "type":"object", "properties": { p: {"type":"string"} }, "required": [...] }
        var root = objectMapper.createObjectNode();
        root.put("type", "object");
        var props = objectMapper.createObjectNode();
        var required = objectMapper.createArrayNode();

        for (String p : placeholders) {
            var pNode = objectMapper.createObjectNode();
            pNode.put("type", "string");
            props.set(p, pNode);
            required.add(p);
        }

        root.set("properties", props);
        root.set("required", required);
        return root;
    }

    @Override
    public List<TemplateDto> listTemplatesForCustomer(String customerId) {
        return templateRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TemplateDto getTemplateForCustomer(String customerId, UUID templateId) {
        return templateRepository.findByIdAndCustomerId(templateId, customerId)
                .map(this::toDto)
                .orElse(null);
    }


    private TemplateDto toDto(Template template) {
        TemplateDto dto = new TemplateDto();
        dto.setId(template.getId().toString());
        dto.setName(template.getName());
        dto.setHtml(template.getHtml());
//        dto.setCss(template.getCss());
//        dto.setJsonSchema(template.getJsonSchema());
        dto.setLogoUrl(template.getLogoUrl());
        dto.setSignatureUrl(template.getSignatureUrl());
        dto.setCreatedAt(template.getCreatedAt());
//        dto.setUpdatedAt(template.getUpdatedAt());
        return dto;
    }
    @Override
    public Template updateTemplate(String templateId, String name, MultipartFile file, MultipartFile logo, MultipartFile signature, String customerId) throws Exception {
        Optional<Template> optTmpl = templateRepository.findByIdAndCustomerId(UUID.fromString(templateId), customerId);
        if (optTmpl.isEmpty()) return null;
        Template template = optTmpl.get();

        template.setName(name);

        // Update HTML
        if (file != null && !file.isEmpty()) {
            String rawHtml = new String(file.getBytes(), StandardCharsets.UTF_8);
            String safeHtml = Jsoup.clean(
                    rawHtml,
                    Safelist.relaxed().preserveRelativeLinks(true).addAttributes(":all", "class", "id", "style")
            );
            String lower = safeHtml.toLowerCase();
            if (!lower.contains("<html")) {
                safeHtml = "<html><body>" + safeHtml + "</body></html>";
            } else if (!lower.contains("<body")) {
                safeHtml = safeHtml.replaceFirst("(?i)(<html[^>]*>)", "$1<body>") + "</body>";
            }
            safeHtml = safeHtml.replaceAll("<br(?!/)>", "<br/>");
            safeHtml = safeHtml
                    // Normalize BR tags
                    .replaceAll("(?i)<br\\s*/?>", "<br/>")
                    .replaceAll("(?i)<br\\s*//?>", "<br/>")

                    // Replace weird JSX-style tags
                    .replaceAll("(?i)<br\\s*//\\s*>", "<br/>")

                    // Remove stray slashes
                    .replaceAll("(?i)<br\\s*//>", "<br/>");

            template.setHtml(safeHtml);
        }

        // Update logo/signature only if a new file is supplied
        if (logo != null && !logo.isEmpty()) {
            String logoUrl = imageStorageService.upload(customerId, logo);
            template.setLogoUrl(logoUrl);
        }
        if (signature != null && !signature.isEmpty()) {
            String sigUrl = imageStorageService.upload(customerId, signature);
            template.setSignatureUrl(sigUrl);
        }

        template.setUpdatedAt(Instant.now());

        return templateRepository.save(template);
    }

    @Override
    public boolean deleteTemplateForCustomer(String customerId, UUID templateId) {
        Template template = templateRepository.findById(templateId)
                .filter(t -> t.getCustomerId().equals(customerId))
                .orElse(null);
        if (template == null) return false;
        templateRepository.delete(template);
        return true;
    }


}