package com.seccertificate.cert_generation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void gettersAndSetters_WorkCorrectly() {
        User user = new User();

        user.setUsername("john");
        user.setPassword("pw");
        user.setRole("ADMIN");
        user.setCustomerId("cid");
        user.setName("John Doe");

        assertEquals("john", user.getUsername());
        assertEquals("pw", user.getPassword());
        assertEquals("ADMIN", user.getRole());
        assertEquals("cid", user.getCustomerId());
        assertEquals("John Doe", user.getName());
    }
}
