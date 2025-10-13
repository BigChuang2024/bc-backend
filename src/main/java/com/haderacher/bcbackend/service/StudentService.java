package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.dto.LoginStudentRequest;
import com.haderacher.bcbackend.dto.RegisterStudentRequest;
import com.haderacher.bcbackend.entity.aggregates.recruiter.Recruiter;
import com.haderacher.bcbackend.entity.aggregates.recruiter.RecruiterRepository;
import com.haderacher.bcbackend.entity.aggregates.student.Student;
import com.haderacher.bcbackend.entity.aggregates.student.StudentRepository;
import com.haderacher.bcbackend.exception.UserExistException;
import com.haderacher.bcbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Component
public class StudentService implements UserDetailsService {

    StudentService(StudentRepository studentRepository, JwtUtil jwtUtil, RecruiterRepository recruiterRepository) {
        this.jwtUtil = jwtUtil;
        this.studentRepository = studentRepository;
        this.recruiterRepository = recruiterRepository;
    }

    private final StudentRepository studentRepository;

    private final RecruiterRepository recruiterRepository;

    private final JwtUtil jwtUtil;

    /**
     * 从持久层中根据用户名读取用户，保存为 <code>UserDetail</code> ,这个函数会先查学生，如果查不到就查应聘者，如果还是查不到就会抛出<code>UsernameNotFoundException</code>
     * 这个函数主要是重写了父类方法，用于<code>spring security</code>中的其他组件使用，比如<code>DaoUserDetailService</code>
     * @param username the username identifying the user whose data is required.
     * @return 用户信息<code>UserDetails</code>
     * @throws UsernameNotFoundException 表示用户名不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return studentRepository.findByUsername(username)
                .map(student -> new User(
                        username,
                        student.getPassword(),
                        student.getAuthorities().stream()
                                .map(authority -> new SimpleGrantedAuthority(authority.toString()))
                                .toList()))
                .or(() -> recruiterRepository.findByUsername(username)
                        .map(recruiter -> new User(
                                username,
                                recruiter.getPassword(),
                                List.of(new SimpleGrantedAuthority("ROLE_RECRUITER")))))
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }


    /**
     * 注册用户并生成<code>JWT Token</code>，需要先检查用户是否已经存在，如果不存在就存储用户，并生成JWT Token。
     * @param registerStudentRequest
     * @return
     */
    @Transactional
    public String registerStudentAndGetToken(RegisterStudentRequest registerStudentRequest) {
        if (studentRepository.findByUsername(registerStudentRequest.getUsername()).isPresent()) {
            throw new UserExistException("student " + registerStudentRequest.getUsername() + " already exists");
        }
        Student student = Student.builder()
                .username(registerStudentRequest.getUsername())
                .password(registerStudentRequest.getPassword())
                .phoneNumber(registerStudentRequest.getPhone())
                .build();
        studentRepository.save(student);
        return jwtUtil.toToken(student);
    }

    @Transactional
    public String loginStudentAndGetToken(LoginStudentRequest loginStudentRequest) {
        Optional<Student> student = studentRepository.findByUsername(loginStudentRequest.getUsername());
        if (student.isEmpty() || !student.get().getPassword().equals(loginStudentRequest.getPassword())) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        return jwtUtil.toToken(student.get());
    }

    public static String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Student student) {
            return student.getUsername();
        } else if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof String username) {
            return username;
        }
        return null;
    }
}
