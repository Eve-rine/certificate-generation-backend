package com.seccertificate.cert_generation.repository;

import java.util.List;
import java.util.UUID;

import com.seccertificate.cert_generation.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TemplateRepository extends JpaRepository<Template, UUID> {
    List<Template> findByCustomerId(String customerId);

    Template findLatestByCustomerId(String customerId);
}
