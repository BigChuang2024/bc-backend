package com.haderacher.bcbackend.model.valueobject;

import lombok.Data;

@Data
public class CompetitionExperience {
    private String competition_name;
    private String level;
    private String start_date;
    private String end_date;
    private String description;
}
