package com.seccertificate.cert_generation.service;

import com.seccertificate.cert_generation.model.Customer;
import java.util.List;
import java.util.UUID;

public interface CustomerService {
    Customer createCustomer(Customer customer);
    List<Customer> getAllCustomers();

    void deleteCustomer(UUID id);
    Customer updateCustomer(UUID id, Customer customer);
}

