package com.haderacher.bcbackend.model.valueobject;

import lombok.Data;

@Data
public class WorkExperience {
    private String company_name;
    private String role_name;
    private String start_date;
    private String end_date;
    private String description;
}
