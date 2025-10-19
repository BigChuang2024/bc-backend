package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.dto.*;
import com.haderacher.bcbackend.model.*;
import com.haderacher.bcbackend.repository.RecruiterProfileRepository;
import com.haderacher.bcbackend.repository.RoleRepository;
import com.haderacher.bcbackend.repository.StudentProfileRepository;
import com.haderacher.bcbackend.repository.UserRepository;
import com.haderacher.bcbackend.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RecruiterProfileRepository recruiterProfileRepository;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       StudentProfileRepository studentProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, RecruiterProfileRepository recruiterProfileRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.recruiterProfileRepository = recruiterProfileRepository;
    }

    public <T extends BaseRegistrationDto> User registerUser(T dto, UserRole role) {
        // 验证用户信息 可以更多
        validateRegistration(dto);

        User user = createUser(dto, role);
        User savedUser = userRepository.save(user);

        // 创建对应的Profile
        createUserProfile(savedUser, dto, role);

        return savedUser;
    }
    /**
     * 验证注册信息
     * @param dto
     */
    private void validateRegistration(BaseRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (userRepository.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("Error: Phone is already in use!");
        }
    }

    /**
     * user实体创建
     * @param dto
     * @param role
     * @return
     */
    private User createUser(BaseRegistrationDto dto, UserRole role) {
        Role userRole = roleRepository.findByName(role.getRoleName())
                .orElseThrow(() -> new RuntimeException("角色未找到: " + role.getRoleName()));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setRoles(Collections.singleton(userRole));
        user.setEnabled(true);
        return user;
    }
    private <T extends BaseRegistrationDto> void createUserProfile(User user, T dto, UserRole role) {
        switch (role) {
            case STUDENT:
                StudentProfile studentProfile = new StudentProfile();
                studentProfile.setUser(user);
                studentProfileRepository.save(studentProfile);
                break;

            case RECRUITER:
                RecruiterRegistrationDto recruiterDto = (RecruiterRegistrationDto) dto;
                RecruiterProfile recruiterProfile = new RecruiterProfile();
                recruiterProfile.setUser(user);
                recruiterProfile.setCompanyName(recruiterDto.getCompanyName());
                recruiterProfileRepository.save(recruiterProfile);
                break;

            default:
                throw new RuntimeException("不支持的用户角色: " + role);
        }
    }
    public <T extends BaseRegistrationDto> String registerAndGetToken(T dto, UserRole role) {
        User user = registerUser(dto, role);
        return jwtUtil.toToken(user);
    }
//  下面的是注册  //

//    /**
//     * 注册一个新学生，并自动赋予 ROLE_STUDENT 角色
//     * @param dto 包含学生注册信息的 DTO
//     * @return 创建好的 User 对象
//     */
//    @Transactional // 1. 开启事务管理
//    public User registerStudent(StudentRegistrationDto dto) {
//        // 检查用户名或邮箱是否已存在
//        if (userRepository.existsByUsername(dto.getUsername())) {
//            throw new RuntimeException("Error: Username is already taken!");
//        }
//        if (userRepository.existsByPhone(dto.getPhone())) {
//            throw new RuntimeException("Error: Phone is already in use!");
//        }
//
//        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
//                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//
//        User user = new User();
//        user.setUsername(dto.getUsername());
//        user.setPassword(passwordEncoder.encode(dto.getPassword()));
//        user.setRoles(Collections.singleton(studentRole));
//        user.setEnabled(true);
//
//        // 先保存 User 以获取其生成的 ID
//        User savedUser = userRepository.save(user);
//
//        // 创建 StudentProfile 实体
//        StudentProfile profile = new StudentProfile();
//
//        // 只需要建立实体间的关联即可
//        profile.setUser(savedUser);
//
//        // ***** 不要手动设置 ID！ *****
//        // profile.setId(savedUser.getId()); // <-- 删除这一行
//
//        // 保存 profile，JPA 会自动从关联的 user 对象中获取 ID
//        studentProfileRepository.save(profile);
//
//        return savedUser;
//    }
//    public User registerRecruiter(RecruiterRegistrationDto dto){
//        // 检查用户名或邮箱是否已存在
//        if (userRepository.existsByUsername(dto.getUsername())) {
//            throw new RuntimeException("Error: Username is already taken!");
//        }
//        if (userRepository.existsByPhone(dto.getPhone())) {
//            throw new RuntimeException("Error: Phone is already in use!");
//        }
//
//        Role recruiterRole = roleRepository.findByName("ROLE_RECRUITER")
//                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//
//        User user = new User();
//        user.setUsername(dto.getUsername());
//        user.setPassword(passwordEncoder.encode(dto.getPassword()));
//        user.setRoles(Collections.singleton(recruiterRole));
//        user.setEnabled(true);
//
//        // 先保存 User 以获取其生成的 ID
//        User savedUser = userRepository.save(user);
//        RecruiterProfile Profile = new RecruiterProfile();
//        Profile.setUser(savedUser);
//        Profile.setCompanyName(dto.getCompanyName());
//        recruiterProfileRepository.save(Profile);
//        return savedUser;
//    }
//
//    @Transactional
//    public String registerUserAndGetToken(StudentRegistrationDto dto) {
//        User user = registerStudent(dto);
//        // 生成 JWT token 的逻辑
//        return jwtUtil.toToken(user);
//    }
//    //方法重载，注册招聘者 //或者泛型？
//    @Transactional
//    public String registerUserAndGetToken(RecruiterRegistrationDto dto) {
//        User user = registerRecruiter(dto);
//        return jwtUtil.toToken(user);
//    }


    @Transactional
    public String loginUserAndGetToken(BaseLoginDto dto) {
        // 1. 根据用户名从数据库查找用户实体
        Optional<User> optionalUser = userRepository.findByUsername(dto.getUsername());

        // 2. 获取用户输入的原始密码
        String rawPassword = dto.getPassword();

        // 3. 检查用户是否存在，并使用 passwordEncoder.matches() 验证密码
        if (optionalUser.isEmpty() || !passwordEncoder.matches(rawPassword, optionalUser.get().getPassword())) {
            // 如果用户不存在或密码不匹配，抛出认证异常
            throw new BadCredentialsException("Invalid username or password!");
        }

        // 4. 认证成功，为用户生成 JWT
        User user = optionalUser.get();
        return jwtUtil.toToken(user);
    }



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
    }

    public static UserDetails getCurrentUserDetails() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public User getCurrentUser() {
        UserDetails currentUserDetails = getCurrentUserDetails();
        Optional<User> username = userRepository.findByUsername(currentUserDetails.getUsername());
        if (username.isPresent()) {
            return username.get();
        } else {
            throw new UsernameNotFoundException("User Not Found with username: " + currentUserDetails.getUsername());
        }
    }
}
