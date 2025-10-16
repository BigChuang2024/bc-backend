package com.haderacher.bcbackend.model.valueobject;

import lombok.Data;

@Data
public class ProjectExperience {
    private String project_name;
    private String start_date;
    private String end_date;
    private String description;
}
