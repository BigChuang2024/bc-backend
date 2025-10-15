package com.haderacher.bcbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentRegistrationDto {

    // Getters and Setters
    private String username;
    private String password;
    private String email;

    // 学生专属信息
    private String realName;
    private String school;
    private String major;

}