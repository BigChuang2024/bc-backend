package com.haderacher.bcbackend.dto;


import lombok.Data;

@Data
public class ResumeVo {
    private String fileName;
    private String markdown;
    private Object json;
    private String timestamp;
}
