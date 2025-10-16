package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.dto.StudentLoginDto;
import com.haderacher.bcbackend.dto.StudentRegistrationDto;
import com.haderacher.bcbackend.model.Role;
import com.haderacher.bcbackend.model.User;
import com.haderacher.bcbackend.repository.RoleRepository;
import com.haderacher.bcbackend.repository.StudentProfileRepository;
import com.haderacher.bcbackend.repository.UserRepository;
import com.haderacher.bcbackend.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private StudentProfileRepository studentProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerStudent_success() {
        StudentRegistrationDto dto = mock(StudentRegistrationDto.class);
        when(dto.getUsername()).thenReturn("alice");
        when(dto.getPassword()).thenReturn("plain");
        when(dto.getPhone()).thenReturn("10086");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByPhone("10086")).thenReturn(false);

        Role role = new Role();
        role.setName("ROLE_STUDENT");
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(role));

        when(passwordEncoder.encode("plain")).thenReturn("encodedPlain");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("alice");
        savedUser.setPassword("encodedPlain");
        savedUser.setRoles(Collections.singleton(role));
        savedUser.setEnabled(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerStudent(dto);

        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        assertTrue(result.getRoles().stream().anyMatch(r -> "ROLE_STUDENT".equals(r.getName())));
        verify(studentProfileRepository, times(1)).save(any());
    }

    @Test
    void registerStudent_usernameExists_throws() {
        StudentRegistrationDto dto = mock(StudentRegistrationDto.class);
        when(dto.getUsername()).thenReturn("bob");
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerStudent(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("username"));
    }

    @Test
    void loginUserAndGetToken_success() {
        StudentLoginDto dto = mock(StudentLoginDto.class);
        when(dto.getUsername()).thenReturn("carol");
        when(dto.getPassword()).thenReturn("rawPass");

        User user = new User();
        user.setUsername("carol");
        // UserService currently compares raw password directly
        user.setPassword("rawPass");

        when(userRepository.findByUsername("carol")).thenReturn(Optional.of(user));
        when(jwtUtil.toToken(user)).thenReturn("jwt-token-123");

        String token = userService.loginUserAndGetToken(dto);
        assertEquals("jwt-token-123", token);
    }

    @Test
    void loginUserAndGetToken_invalid_throws() {
        StudentLoginDto dto = mock(StudentLoginDto.class);
        when(dto.getUsername()).thenReturn("dave");

        when(userRepository.findByUsername("dave")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> userService.loginUserAndGetToken(dto));
    }

    @Test
    void loadUserByUsername_success_and_notFound() {
        User user = new User();
        user.setUsername("eve");

        when(userRepository.findByUsername("eve")).thenReturn(Optional.of(user));
        assertEquals(user, userService.loadUserByUsername("eve"));

        when(userRepository.findByUsername("noone")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("noone"));
    }

    @Test
    void getCurrentUser_returnsPrincipal() {
        User user = new User();
        user.setUsername("me");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        UserDetails current = UserService.getCurrentUser();
        assertEquals(user.getUsername(), current.getUsername());
    }
}
