package com.haderacher.bcbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CreateJobDto {
    private String name;
    private String description;
    private String company;
    private String recruiter;
    private String address;
    private String degreeReq;
    private Integer salaryMin;
    private Integer salaryMax;
    private String salaryType;
    private Integer salaryAnnual;
    private List<String> labels;
    private String category;

    public CreateJobDto() {}

}

