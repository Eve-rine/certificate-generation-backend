package com.seccertificate.cert_generation.repository;

import com.seccertificate.cert_generation.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {
}
