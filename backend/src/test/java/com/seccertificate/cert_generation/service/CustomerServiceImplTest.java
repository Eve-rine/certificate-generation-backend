package com.seccertificate.cert_generation.service;

import com.seccertificate.cert_generation.model.Customer;
import com.seccertificate.cert_generation.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    private CustomerRepository customerRepository;
    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        service = new CustomerServiceImpl(customerRepository);
    }

    @Test
    void createCustomer_savesNameAndEmail() {
        Customer input = new Customer();
        input.setName("Alice");
        input.setEmail("alice@example.com");

        Customer saved = new Customer();
        saved.setId(UUID.randomUUID());
        saved.setName("Alice");
        saved.setEmail("alice@example.com");

        when(customerRepository.save(any())).thenReturn(saved);

        Customer result = service.createCustomer(input);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        Customer toSave = captor.getValue();
        assertThat(toSave.getName()).isEqualTo("Alice");
        assertThat(toSave.getEmail()).isEqualTo("alice@example.com");

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Alice");
    }

    @Test
    void getAllCustomers_delegatesToRepository() {
        when(customerRepository.findAll()).thenReturn(List.of(new Customer()));
        List<Customer> list = service.getAllCustomers();
        assertThat(list).hasSize(1);
        verify(customerRepository).findAll();
    }

    @Test
    void deleteCustomer_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        service.deleteCustomer(id);
        verify(customerRepository).deleteById(id);
    }

    @Test
    void updateCustomer_existing_updatesAndSaves() {
        UUID id = UUID.randomUUID();
        Customer existing = new Customer();
        existing.setId(id);
        existing.setName("Old");
        existing.setEmail("old@example.com");

        Customer update = new Customer();
        update.setName("New");
        update.setEmail("new@example.com");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Customer res = service.updateCustomer(id, update);

        assertThat(res.getName()).isEqualTo("New");
        assertThat(res.getEmail()).isEqualTo("new@example.com");
        verify(customerRepository).save(existing);
    }

    @Test
    void updateCustomer_missing_throws() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updateCustomer(id, new Customer()));
    }
}
