package com.food.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.food.entity.User;
import com.food.exception.AppException;
import com.food.repository.UserRepository;
import com.food.request.payload.LoginDto;
import com.food.request.payload.UserDto;
import com.food.response.payload.UserResponseDto;
import com.food.security.service.UserDetailsImpl;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private UserDto userDto;
    private LoginDto loginDto;
    private User user;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userDto = new UserDto();
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");
        userDto.setPassword("password");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole("ROLE_USER");
        user.setCreatedAt(LocalDateTime.now());

        loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password");

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setName("Test User");
        userResponseDto.setEmail("test@example.com");
        userResponseDto.setRole("ROLE_USER");
    }

    @Test
    @DisplayName("Test Register - Success")
    void testRegister_Success() {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User registeredUser = authService.register(userDto);

        assertNotNull(registeredUser);
        assertEquals("Test User", registeredUser.getName());
        assertEquals("test@example.com", registeredUser.getEmail());
        assertEquals("ROLE_USER", registeredUser.getRole());
    }

    @Test
    @DisplayName("Test Register - Email Already Exists")
    void testRegister_EmailAlreadyExists() {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(user);

        assertThrows(AppException.class, () -> authService.register(userDto));
    }

    @Test
    @DisplayName("Test Login - Success")
    void testLogin_Success() {
        // Mock the custom UserDetailsImpl class
        UserDetailsImpl userDetailsImpl = mock(UserDetailsImpl.class);
        when(userDetailsImpl.getUsername()).thenReturn("test@example.com");
        when(userDetailsImpl.getPassword()).thenReturn("encodedPassword");
        when(userDetailsImpl.getId()).thenReturn(1L);
        when(userDetailsImpl.getRole()).thenReturn("ROLE_USER");
        when(userDetailsImpl.getName()).thenReturn("Test User");  // Mocking the name

        // Mock the authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetailsImpl, "password");

        // Mock the authentication manager to return the mocked authentication object
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        // Mock the userRepository to return the user when queried by email
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(user);

        // Call the login method in the AuthService
        UserResponseDto responseDto = authService.login(loginDto);

        // Verify the response
        assertNotNull(responseDto);
        assertEquals("Test User", responseDto.getName());
    }



    @Test
    @DisplayName("Test Login - Invalid Credentials")
    void testLogin_InvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new AppException("Invalid credentials"));

        assertThrows(AppException.class, () -> authService.login(loginDto));
    }

    @Test
    @DisplayName("Test Login - User Not Found")
    void testLogin_UserNotFound() {
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(null);

        assertThrows(AppException.class, () -> authService.login(loginDto));
    }
}
