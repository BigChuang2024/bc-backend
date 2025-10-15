package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.dto.StudentRegistrationDto;
import com.haderacher.bcbackend.model.Role;
import com.haderacher.bcbackend.model.StudentProfile;
import com.haderacher.bcbackend.model.User;
import com.haderacher.bcbackend.repository.RoleRepository;
import com.haderacher.bcbackend.repository.StudentProfileRepository;
import com.haderacher.bcbackend.repository.UserRepository;
import com.haderacher.bcbackend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest()
class UserServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private StudentProfileRepository studentProfileRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @Autowired
    JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        studentProfileRepository = mock(StudentProfileRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, roleRepository, studentProfileRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void registerStudent_success() {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setUsername("testuser");
        dto.setPassword("123456");
        dto.setPhone("12345678901");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByPhone("12345678901")).thenReturn(false);

        Role role = new Role();
        role.setName("ROLE_STUDENT");
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(role));

        when(passwordEncoder.encode("123456")).thenReturn("encodedPwd");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPwd");
        user.setRoles(Collections.singleton(role));
        user.setEnabled(true);

        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.registerStudent(dto);

        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPwd", result.getPassword());
        assertTrue(result.getRoles().contains(role));
        assertTrue(result.isEnabled());

        ArgumentCaptor<StudentProfile> profileCaptor = ArgumentCaptor.forClass(StudentProfile.class);
        verify(studentProfileRepository).save(profileCaptor.capture());
        assertEquals(1L, profileCaptor.getValue().getId());
        assertEquals(user, profileCaptor.getValue().getUser());
    }

    @Test
    void registerStudent_usernameExists_throwsException() {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setUsername("testuser");
        dto.setPhone("12345678901");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerStudent(dto));
        assertEquals("Error: Username is already taken!", ex.getMessage());
    }

    @Test
    void registerStudent_phoneExists_throwsException() {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setUsername("testuser");
        dto.setPhone("12345678901");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByPhone("12345678901")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerStudent(dto));
        assertEquals("Error: Phone is already in use!", ex.getMessage());
    }

    @Test
    void registerStudent_roleNotFound_throwsException() {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setUsername("testuser");
        dto.setPhone("12345678901");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByPhone("12345678901")).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerStudent(dto));
        assertEquals("Error: Role is not found.", ex.getMessage());
    }

    @Test
    void loadUserByUsername_success() {
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertEquals(user, userService.loadUserByUsername("testuser"));
    }

    @Test
    void loadUserByUsername_notFound_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("testuser"));
    }
}