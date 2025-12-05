package com.seccertificate.cert_generation.repository;

import com.seccertificate.cert_generation.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}
