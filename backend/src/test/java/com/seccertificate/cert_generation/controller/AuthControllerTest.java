package com.seccertificate.cert_generation.controller;

import com.seccertificate.cert_generation.dto.LoginResponse;
import com.seccertificate.cert_generation.model.User;
import com.seccertificate.cert_generation.security.JwtUtil;
import com.seccertificate.cert_generation.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedpassword";
    private static final String ROLE = "USER";
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    private static final long EXPIRY = 1234567890L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername(USERNAME);
        testUser.setPassword(ENCODED_PASSWORD);
        testUser.setRole(ROLE);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        // Arrange
        when(userService.findByUsername(USERNAME)).thenReturn(testUser);
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.generateToken(USERNAME, ROLE)).thenReturn(TOKEN);
            jwtUtilMock.when(() -> JwtUtil.getExpiryFromToken(TOKEN)).thenReturn(EXPIRY);

            // Act
            LoginResponse response = authController.login(USERNAME, PASSWORD);

            // Assert
            assertNotNull(response);
            assertEquals(TOKEN, response.getToken());
            assertEquals(EXPIRY, response.getExpiry());
            assertEquals(ROLE, response.getRole());

            verify(userService).findByUsername(USERNAME);
            verify(passwordEncoder).matches(PASSWORD, ENCODED_PASSWORD);
            jwtUtilMock.verify(() -> JwtUtil.generateToken(USERNAME, ROLE));
            jwtUtilMock.verify(() -> JwtUtil.getExpiryFromToken(TOKEN));
        }
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowRuntimeException() {
        // Arrange
        when(userService.findByUsername(USERNAME)).thenReturn(testUser);
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authController.login(USERNAME, PASSWORD));

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userService).findByUsername(USERNAME);
        verify(passwordEncoder).matches(PASSWORD, ENCODED_PASSWORD);
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowRuntimeException() {
        // Arrange
        when(userService.findByUsername(USERNAME)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authController.login(USERNAME, PASSWORD));

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userService).findByUsername(USERNAME);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WithEmptyUsername_ShouldThrowRuntimeException() {
        // Arrange
        when(userService.findByUsername("")).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authController.login("", PASSWORD));

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userService).findByUsername("");
    }

    @Test
    void login_WithEmptyPassword_ShouldThrowRuntimeException() {
        // Arrange
        when(userService.findByUsername(USERNAME)).thenReturn(testUser);
        when(passwordEncoder.matches("", ENCODED_PASSWORD)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authController.login(USERNAME, ""));

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userService).findByUsername(USERNAME);
        verify(passwordEncoder).matches("", ENCODED_PASSWORD);
    }

    @Test
    void login_WithAdminRole_ShouldReturnLoginResponseWithAdminRole() {
        // Arrange
        testUser.setRole("ADMIN");
        when(userService.findByUsername(USERNAME)).thenReturn(testUser);
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.generateToken(USERNAME, "ADMIN")).thenReturn(TOKEN);
            jwtUtilMock.when(() -> JwtUtil.getExpiryFromToken(TOKEN)).thenReturn(EXPIRY);

            // Act
            LoginResponse response = authController.login(USERNAME, PASSWORD);

            // Assert
            assertNotNull(response);
            assertEquals("ADMIN", response.getRole());
            jwtUtilMock.verify(() -> JwtUtil.generateToken(USERNAME, "ADMIN"));
        }
    }

    @Test
    void login_WhenUserServiceThrowsException_ShouldPropagateException() {
        // Arrange
        when(userService.findByUsername(USERNAME)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authController.login(USERNAME, PASSWORD));

        assertEquals("Database error", exception.getMessage());
        verify(userService).findByUsername(USERNAME);
    }
}