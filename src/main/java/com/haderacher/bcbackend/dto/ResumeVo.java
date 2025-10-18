package com.haderacher.bcbackend.dto;


import lombok.Data;
import com.haderacher.bcbackend.dto.ResumeContentDto;

@Data
public class ResumeVo {
    private String fileName;
    private String markdown;
    private ResumeContentDto json;
    private String timestamp;
}
