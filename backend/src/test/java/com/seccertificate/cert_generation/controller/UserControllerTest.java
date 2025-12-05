package com.seccertificate.cert_generation.controller;

import com.seccertificate.cert_generation.model.User;
import com.seccertificate.cert_generation.service.UserService;
import com.seccertificate.cert_generation.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    UserService userService;

    @InjectMocks
    UserController userController;

    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setName("Test User");
        user.setPassword("pw");
        user.setRole("ADMIN");
        user.setCustomerId("cid");
    }

    @Test
    void createUser_returnsUser() {
        when(userService.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(user);
        User result = userController.createUser(user);
        assertEquals("testuser", result.getUsername());
        verify(userService).createUser(
                eq(user.getUsername()),
                eq(user.getName()),
                eq(user.getPassword()),
                eq(user.getRole()),
                eq(user.getCustomerId())
        );
    }

    @Test
    void getAllUsers_returnsList() {
        UserDto dto = new UserDto(); dto.setUsername("testuser");
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(dto));
        List<UserDto> users = userController.getAllUsers();
        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getUsername());
        verify(userService).getAllUsers();
    }

    @Test
    void deleteUser_callsService() {
        userController.deleteUser("uid123");
        verify(userService).deleteUser("uid123");
    }

    @Test
    void updateUser_returnsUser() {
        when(userService.updateUser(eq("uid123"), any(User.class))).thenReturn(user);
        User result = userController.updateUser("uid123", user);
        assertEquals("testuser", result.getUsername());
        verify(userService).updateUser("uid123", user);
    }
}