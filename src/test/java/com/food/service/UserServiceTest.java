package com.food.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.food.entity.User;
import com.food.exception.AppException;
import com.food.exception.NotFoundException;
import com.food.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Test getUserById - Success")
    void testGetUserById_Success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Test getUserById - User Not Found")
    void testGetUserById_UserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        assertEquals("User id not found", exception.getMessage());
    }

    @Test
    @DisplayName("Test getAllUsers - Success")
    void testGetAllUsers_Success() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("Test User 1");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Test User 2");
        user2.setEmail("user2@example.com");

        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test User 1", result.get(0).getName());
        assertEquals("Test User 2", result.get(1).getName());
    }

    @Test
    @DisplayName("Test getAllUsers - No Users")
    void testGetAllUsers_NoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test getUserById - Exception Handling")
    void testGetUserById_ExceptionHandling() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenThrow(new RuntimeException("Database error"));

        AppException exception = assertThrows(AppException.class, () -> {
            userService.getUserById(userId);
        });

        assertTrue(exception.getMessage().contains("Database error"));
    }

    @Test
    @DisplayName("Test getAllUsers - Exception Handling")
    void testGetAllUsers_ExceptionHandling() {
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        AppException exception = assertThrows(AppException.class, () -> {
            userService.getAllUsers();
        });

        assertTrue(exception.getMessage().contains("Database error"));
    }
}
