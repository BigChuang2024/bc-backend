package com.haderacher.bcbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecruiterRegistrationDto extends BaseRegistrationDto {
    @NotBlank(message = "Company name can't be empty")
    private String companyName;
}
