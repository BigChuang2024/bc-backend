// java
package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.dto.StudentLoginDto;
import com.haderacher.bcbackend.dto.StudentRegistrationDto;
import com.haderacher.bcbackend.model.Role;
import com.haderacher.bcbackend.model.StudentProfile;
import com.haderacher.bcbackend.model.User;
import com.haderacher.bcbackend.repository.RoleRepository;
import com.haderacher.bcbackend.repository.StudentProfileRepository;
import com.haderacher.bcbackend.repository.UserRepository;
import com.haderacher.bcbackend.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
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

    @Test
    void registerStudent_success_savesUserAndProfile() {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setUsername("alice");
        dto.setPassword("pwd");
        dto.setPhone("123456");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByPhone("123456")).thenReturn(false);

        Role studentRole = new Role();
        studentRole.setName("ROLE_STUDENT");
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));

        when(passwordEncoder.encode("pwd")).thenReturn("encodedPwd");

        // 模拟 save 返回已设置 id 的 user
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("alice");
        savedUser.setPassword("encodedPwd");
        savedUser.setRoles(Collections.singleton(studentRole));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerStudent(dto);

        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        verify(userRepository).save(any(User.class));
        verify(studentProfileRepository).save(any(StudentProfile.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User toSave = userCaptor.getValue();
        assertEquals("alice", toSave.getUsername());
        assertEquals("encodedPwd", toSave.getPassword());
    }

    @Test
    void registerUserAndGetToken_returnsToken() {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setUsername("bob");
        dto.setPassword("pwd2");
        dto.setPhone("888");

        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByPhone("888")).thenReturn(false);

        Role r = new Role();
        r.setName("ROLE_STUDENT");
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(r));
        when(passwordEncoder.encode("pwd2")).thenReturn("enc2");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("bob");
        savedUser.setPassword("enc2");
        savedUser.setRoles(Collections.singleton(r));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtUtil.toToken(any(User.class))).thenReturn("token-123");

        String token = userService.registerUserAndGetToken(dto);
        assertEquals("token-123", token);
        verify(jwtUtil).toToken(any(User.class));
    }

    @Test
    void loginUserAndGetToken_success() {
        StudentLoginDto dto = new StudentLoginDto();
        dto.setUsername("carol");
        dto.setPassword("rawpwd");

        User user = new User();
        user.setId(3L);
        user.setUsername("carol");
        user.setPassword("encpwd");

        when(userRepository.findByUsername("carol")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawpwd", "encpwd")).thenReturn(true);
        when(jwtUtil.toToken(user)).thenReturn("jwt-carol");

        String token = userService.loginUserAndGetToken(dto);
        assertEquals("jwt-carol", token);
    }

    @Test
    void loginUserAndGetToken_badCredentials_whenUserNotFound() {
        StudentLoginDto dto = new StudentLoginDto();
        dto.setUsername("noone");
        dto.setPassword("x");

        when(userRepository.findByUsername("noone")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> userService.loginUserAndGetToken(dto));
    }

    @Test
    void loginUserAndGetToken_badCredentials_whenPasswordMismatch() {
        StudentLoginDto dto = new StudentLoginDto();
        dto.setUsername("dave");
        dto.setPassword("wrong");

        User user = new User();
        user.setUsername("dave");
        user.setPassword("enc");

        when(userRepository.findByUsername("dave")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "enc")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.loginUserAndGetToken(dto));
    }

    @Test
    void registerStudent_duplicateUsername_throws() {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setUsername("exist");
        dto.setPassword("p");
        dto.setPhone("0");

        when(userRepository.existsByUsername("exist")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.registerStudent(dto));
        verify(userRepository, never()).save(any(User.class));
    }
}