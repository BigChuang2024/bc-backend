package com.haderacher.bcbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    private Long id; // 主键与User实体共享

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // 将此实体的主键映射到 User 实体的 ID
    @JoinColumn(name = "id")
    private User user;

    @Column(length = 50)
    private String realName;

    @Column(length = 100)
    private String school;

    @Column(length = 100) // 所在大学
    private String university;

    @Column(length = 100)
    private String major;

    @Column(length = 20) // 学历 (例如：本科, 硕士, 博士)
    private String degree;

    private Integer graduationYear;

    @Lob // 大对象类型，适用于存储长文本（例如：自我介绍、个人描述）
    @Column(columnDefinition = "TEXT") // 明确指定数据库字段类型为 TEXT
    private String selfIntroduction;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    private LocalDate graduationDate;

}