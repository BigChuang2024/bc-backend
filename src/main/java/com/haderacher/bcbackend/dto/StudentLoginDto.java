package com.haderacher.bcbackend.dto;

import lombok.Data;

@Data
public class StudentLoginDto {
    private String username;
    private String password;
}