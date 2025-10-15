package com.haderacher.bcbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "job_info")
public class JobDetails {
    @Id
    String id;
    String activeTimeDesc;
    String address;
    String degreeName;
    String experienceName;
    String jobLabels;
    String jobName;
    String postDescription;
    String salaryDesc;
}