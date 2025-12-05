package com.seccertificate.cert_generation.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import aj.org.objectweb.asm.commons.Remapper;
import com.seccertificate.cert_generation.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TemplateRepository extends JpaRepository<Template, UUID> {

    List<Template> findByCustomerId(String customerId);
    Optional<Template> findByIdAndCustomerId(UUID id, String customerId);

    boolean existsByNameAndCustomerId(String name, String customerId);
}
