package com.seccertificate.cert_generation.service;

import com.seccertificate.cert_generation.model.Customer;
import java.util.List;

public interface CustomerService {
    Customer createCustomer(Customer customer);
    List<Customer> getAllCustomers();

    void deleteCustomer(String id);
    Customer updateCustomer(String id, Customer customer);
}

