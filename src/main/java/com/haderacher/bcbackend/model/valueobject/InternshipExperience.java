package com.haderacher.bcbackend.model.valueobject;

import lombok.Data;
import lombok.Value;

@Data
public class InternshipExperience {
    private String company_name;
    private String role_name;
    private String start_date;
    private String end_date;
    private String description;
}
