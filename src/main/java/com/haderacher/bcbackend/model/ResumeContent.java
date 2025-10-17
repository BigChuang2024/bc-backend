package com.haderacher.bcbackend.model;

import com.haderacher.bcbackend.model.valueobject.CompetitionExperience;
import com.haderacher.bcbackend.model.valueobject.InternshipExperience;
import com.haderacher.bcbackend.model.valueobject.ProjectExperience;
import com.haderacher.bcbackend.model.valueobject.WorkExperience;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "resume_contents")
@AllArgsConstructor
@Builder
public class ResumeContent {

    @Id
    private String id; // Mongo ObjectId as String

    // 关联 MySQL 中的 resume.id
    private Long resumeId;


    // 生成的 markdown
    private String markdown;

    // 提取出的纯文本
    private String text;

    // 常用提取字段（冗余索引/查询友好）
    private List<String> targetPositons; // 投递方向
    private List<String> skills; // 掌握的技术
    private List<InternshipExperience> internship_experiences; // 实习经历
    private List<WorkExperience> work_experiences; // 工作经历
    private List<ProjectExperience> project_experiences; // 项目经历
    private List<String> certifications; // 证书
    private List<CompetitionExperience> competition_experiences; // 竞赛经历
    private List<Map<String, Object>> educations;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
