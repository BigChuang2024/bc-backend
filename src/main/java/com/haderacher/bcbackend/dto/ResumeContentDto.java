package com.haderacher.bcbackend.dto;

import com.haderacher.bcbackend.model.valueobject.CompetitionExperience;
import com.haderacher.bcbackend.model.valueobject.InternshipExperience;
import com.haderacher.bcbackend.model.valueobject.ProjectExperience;
import com.haderacher.bcbackend.model.valueobject.WorkExperience;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ResumeContentDto {
    private List<String> targetPositons;
    private List<String> skills;
    private List<InternshipExperience> internship_experiences;
    private List<WorkExperience> work_experiences;
    private List<ProjectExperience> project_experiences;
    private List<String> certifications;
    private List<CompetitionExperience> competition_experiences;
    private List<String> educations;
}

