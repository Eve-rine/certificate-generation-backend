package com.seccertificate.cert_generation.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void gettersAndSetters_WorkCorrectly() {
        Customer cust = new Customer();
        UUID id = UUID.randomUUID();
        String name = "Alice Co";
        String email = "alice@email.com";

        cust.setId(id);
        cust.setName(name);
        cust.setEmail(email);

        assertEquals(id, cust.getId());
        assertEquals(name, cust.getName());
        assertEquals(email, cust.getEmail());
    }
}