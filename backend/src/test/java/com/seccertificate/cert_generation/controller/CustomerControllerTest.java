package com.seccertificate.cert_generation.controller;

import com.seccertificate.cert_generation.model.Customer;
import com.seccertificate.cert_generation.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    CustomerService customerService;

    @InjectMocks
    CustomerController customerController;

    Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("Acme Corp");
    }

    @Test
    void createCustomer_returnsCustomer() {
        when(customerService.createCustomer(any(Customer.class))).thenReturn(customer);
        Customer result = customerController.createCustomer(customer);
        assertEquals("Acme Corp", result.getName());
        verify(customerService).createCustomer(customer);
    }

    @Test
    void getAllCustomers_returnsList() {
        List<Customer> customers = Collections.singletonList(customer);
        when(customerService.getAllCustomers()).thenReturn(customers);
        List<Customer> result = customerController.getAllCustomers();
        assertEquals(1, result.size());
        assertEquals("Acme Corp", result.get(0).getName());
        verify(customerService).getAllCustomers();
    }

    @Test
    void deleteCustomer_callsService() {
        String id = UUID.randomUUID().toString();
        customerController.deleteCustomer(id);
        verify(customerService).deleteCustomer(UUID.fromString(id));
    }

    @Test
    void updateCustomer_returnsUpdated() {
        UUID id = UUID.randomUUID();
        Customer updated = new Customer(); updated.setId(id); updated.setName("New Name");
        when(customerService.updateCustomer(eq(id), any(Customer.class))).thenReturn(updated);
        Customer result = customerController.updateCustomer(id, updated);
        assertEquals("New Name", result.getName());
        verify(customerService).updateCustomer(id, updated);
    }
}
