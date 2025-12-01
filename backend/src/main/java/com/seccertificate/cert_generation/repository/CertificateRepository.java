package com.seccertificate.cert_generation.repository;

import java.util.UUID;
import java.util.List;

import com.seccertificate.cert_generation.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    List<Certificate> findByCustomerId(String customerId);
}
