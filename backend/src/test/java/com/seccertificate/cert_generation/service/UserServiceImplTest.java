package com.seccertificate.cert_generation.service;

import com.seccertificate.cert_generation.dto.UserDto;
import com.seccertificate.cert_generation.model.User;
import com.seccertificate.cert_generation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void createUser_encodesPasswordAndSaves() {
        when(passwordEncoder.encode("pwd")).thenReturn("encoded");

        User saved = new User();
        saved.setUsername("bob");

        when(userRepository.save(any())).thenReturn(saved);

        User result = service.createUser("bob", "Bob", "pwd", "ROLE_USER", "cust1");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User toSave = captor.getValue();
        assertThat(toSave.getPassword()).isEqualTo("encoded");
        assertThat(result.getUsername()).isEqualTo("bob");
    }

    @Test
    void findByUsername_delegates() {
        User u = new User();
        u.setUsername("sam");
        when(userRepository.findByUsername("sam")).thenReturn(u);
        User res = service.findByUsername("sam");
        assertThat(res).isEqualTo(u);
    }

    @Test
    void getAllUsers_mapsToDto() {
        User u = new User();
        u.setName("N"); u.setUsername("user"); u.setRole("R"); u.setCustomerId("c");
        when(userRepository.findAll()).thenReturn(List.of(u));
        List<UserDto> list = service.getAllUsers();
        assertThat(list).hasSize(1);
        UserDto dto = list.get(0);
        assertThat(dto.getUsername()).isEqualTo("user");
    }

    @Test
    void deleteUser_delegates() {
        service.deleteUser("id1");
        verify(userRepository).deleteById("id1");
    }

    @Test
    void updateUser_existing_updatesAndEncodesPasswordWhenProvided() {
        User existing = new User();
        existing.setName("Old"); existing.setUsername("old"); existing.setRole("R"); existing.setCustomerId("c");
        when(userRepository.findById("id")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpwd")).thenReturn("enc2");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User upd = new User();
        upd.setName("New"); upd.setUsername("new"); upd.setRole("NR"); upd.setCustomerId("nc"); upd.setPassword("newpwd");

        User res = service.updateUser("id", upd);
        assertThat(res.getName()).isEqualTo("New");
        assertThat(res.getPassword()).isEqualTo("enc2");
    }

    @Test
    void updateUser_missing_throws() {
        when(userRepository.findById("no")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updateUser("no", new User()));
    }
}
