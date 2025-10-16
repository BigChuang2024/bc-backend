package com.haderacher.bcbackend.controller;

import com.haderacher.bcbackend.common.ApiResponse;
import com.haderacher.bcbackend.dto.StudentLoginDto;
import com.haderacher.bcbackend.dto.StudentRegistrationDto;
import com.haderacher.bcbackend.service.UserService;
import com.haderacher.bcbackend.util.JwtUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {

    private final UserService userService;

    private final JwtUtil jwtUtil;

    UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/students/register")
    public ApiResponse<String> studentRegister(@RequestBody StudentRegistrationDto studentRegistrationDto) {
        String token = userService.registerUserAndGetToken(studentRegistrationDto);
        return ApiResponse.success("token: " + token);
    }

    @GetMapping("/students/login")
    public ApiResponse<String> studentLogin(StudentLoginDto studentLoginDto) {
        String token = userService.loginUserAndGetToken(studentLoginDto);
        return  ApiResponse.success("token: " + token);
    }
}

