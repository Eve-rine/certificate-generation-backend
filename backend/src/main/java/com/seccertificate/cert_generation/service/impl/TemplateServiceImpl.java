package com.seccertificate.cert_generation.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seccertificate.cert_generation.model.Template;
import com.seccertificate.cert_generation.repository.TemplateRepository;
import com.seccertificate.cert_generation.repository.UserRepository;
import com.seccertificate.cert_generation.service.TemplateService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public TemplateServiceImpl(TemplateRepository templateRepository,
                               UserRepository userRepository,
                               ObjectMapper objectMapper) {
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
    public Template saveTemplate(MultipartFile file, JsonNode schemaNode, String customerId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("template file is required");
        }

        if (file.getSize() > MAX_HTML_BYTES) {
            throw new IllegalArgumentException("template file too large");
        }

        byte[] bytes = file.getBytes();
        String rawHtml = new String(bytes, StandardCharsets.UTF_8);

        // 1) sanitize HTML: remove scripts, event handlers, data: URIs, etc.
        String safeHtml = Jsoup.clean(rawHtml,
                Safelist.relaxed()
                        .preserveRelativeLinks(true)
                        .addAttributes(":all", "class", "id", "style")
        );

        // 2) extract placeholders from sanitized HTML
        Set<String> placeholders = extractPlaceholders(safeHtml);

        // 3) if no schema provided, auto-generate a minimal one from placeholders
        if (schemaNode == null) {
            schemaNode = generateMinimalSchema(placeholders);
        } else {
            // Optional: basic size check for schema if it was provided as a string elsewhere
            String schemaStr = objectMapper.writeValueAsString(schemaNode);
            if (schemaStr.getBytes(StandardCharsets.UTF_8).length > MAX_SCHEMA_BYTES) {
                throw new IllegalArgumentException("provided schema too large");
            }

            // Optional: further validation with a JSON Schema validator can be added here.
            // Also ensure schema declares placeholders; you may choose to auto-add missing props or reject.
        }

        // 4) Persist template (set fields, timestamps)
        Template template = new Template();
        template.setCustomerId(customerId);
        template.setName(file.getOriginalFilename());
        template.setHtml(safeHtml);
        template.setJsonSchema(schemaNode);
        template.setCreatedAt(Instant.now());

        return templateRepository.save(template);
    }

    @Override
    public String getCustomerIdForUser(String username) {
        return userRepository.findByUsername(username).getCustomerId();
    }

    // ----- helpers -----

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
}