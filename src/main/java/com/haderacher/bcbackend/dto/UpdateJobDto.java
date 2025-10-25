package com.haderacher.bcbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UpdateJobDto {

    // getters and setters
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @Deprecated
    private String company;

    @NotBlank
    private String recruiter;

    @NotBlank
    private String address;

    @NotBlank
    private String degreeReq;

    @NotNull
    private Integer salaryMin;

    private Integer salaryMax;

    private String salaryType;

    private Integer salaryAnnual;

    @NotNull
    private List<String> labels;

    @NotBlank
    private String category;

    public UpdateJobDto() {}

}

