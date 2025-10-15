package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.dto.StudentRegistrationDto;
import com.haderacher.bcbackend.model.Role;
import com.haderacher.bcbackend.model.StudentProfile;
import com.haderacher.bcbackend.model.User;
import com.haderacher.bcbackend.repository.RoleRepository;
import com.haderacher.bcbackend.repository.StudentProfileRepository;
import com.haderacher.bcbackend.repository.UserRepository;
import com.haderacher.bcbackend.util.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       StudentProfileRepository studentProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 注册一个新学生，并自动赋予 ROLE_STUDENT 角色
     * @param dto 包含学生注册信息的 DTO
     * @return 创建好的 User 对象
     */
    @Transactional // 1. 开启事务管理
    public User registerStudent(StudentRegistrationDto dto) {
        // 检查用户名或邮箱是否已存在
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (userRepository.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("Error: Phone is already in use!");
        }

        // 2. 查找 "ROLE_STUDENT" 角色
        // 该角色已在数据库中初始化
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        // 3. 创建 User 实体
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // 4. 加密密码
        user.setRoles(Collections.singleton(studentRole)); // 5. 赋予角色
        user.setEnabled(true);

        // 6. 创建 StudentProfile 实体
        StudentProfile profile = new StudentProfile();

        // 7. 关联 User 和 StudentProfile
        // 因为 StudentProfile 的 ID 依赖于 User，所以先保存 User
        User savedUser = userRepository.save(user);

        profile.setUser(savedUser); // 建立关联
        profile.setId(savedUser.getId()); // 设置共享主键

        studentProfileRepository.save(profile);

        return savedUser;
    }

    @Transactional
    public String registerUserAndGetToken(StudentRegistrationDto dto) {
        User user = registerStudent(dto);
        // 生成 JWT token 的逻辑
        return jwtUtil.toToken(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
    }

    public static User getCurrentUser() {
        return (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
