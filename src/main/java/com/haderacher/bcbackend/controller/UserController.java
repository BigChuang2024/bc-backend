package com.haderacher.bcbackend.controller;

import com.haderacher.bcbackend.common.ApiResponse;
import com.haderacher.bcbackend.dto.RecruiterLoginDto;
import com.haderacher.bcbackend.dto.RecruiterRegistrationDto;
import com.haderacher.bcbackend.dto.StudentLoginDto;
import com.haderacher.bcbackend.dto.StudentRegistrationDto;
import com.haderacher.bcbackend.model.UserRole;
import com.haderacher.bcbackend.service.RecruiterProfileService;
import com.haderacher.bcbackend.service.UserService;
import com.haderacher.bcbackend.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RecruiterProfileService recruiterProfileService;

    UserController(UserService userService, RecruiterProfileService recruiterProfileService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.recruiterProfileService = recruiterProfileService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/students/register")
    public ApiResponse<String> studentRegister(@Valid @RequestBody StudentRegistrationDto studentRegistrationDto) {
        String token = userService.registerAndGetToken(studentRegistrationDto,UserRole.STUDENT);
        return ApiResponse.success("token: " + token);
    }

    @PostMapping("/students/login")
    public ApiResponse<String> studentLogin(@Valid @RequestBody StudentLoginDto studentLoginDto) {
        String token = userService.loginUserAndGetToken(studentLoginDto);
        return  ApiResponse.success("token: " + token);
    }

    @PostMapping("/recruiters/register")
    public ApiResponse<String> recruiterRegister(@Valid @RequestBody RecruiterRegistrationDto recruiterRegistrationDto) {

        String token = userService.registerAndGetToken(recruiterRegistrationDto, UserRole.RECRUITER);
        //若成功
        return ApiResponse.success("token: " + token);
    }
    @PostMapping("/recruiters/login")
    public ApiResponse<String> recruiterLogin(@Valid @RequestBody RecruiterLoginDto recruiterLoginDto) {
        String token = userService.loginUserAndGetToken(recruiterLoginDto);
        return  ApiResponse.success("token: " + token);
    }

}

