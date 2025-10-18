package com.haderacher.bcbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;


@Getter
@Setter
@Entity
@Table(name = "resumes",
        indexes = {@Index(name = "idx_user_file", columnList = "user_id,file_name")},
        uniqueConstraints = {@UniqueConstraint(name = "uk_user_filename", columnNames = {"user_id", "file_name"})}
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // 外键列
    private User user; // 多对一关联到 User

    @Column(name = "file_name")
    private String fileName; // 对象存储中的fileKey

    private String fileType; // 文件类型，例如 "pdf", "docx"

    private String mongoId; // MongoDB中的文件ID

    @Enumerated(EnumType.STRING)
    private ParseStatus status; // 枚举类型，表示简历状态

    @Column(columnDefinition = "TEXT") // 自我介绍或概述
    private String summary;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // --- 生命周期回调 ---
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Resume resume = (Resume) o;
        return getId() != null && Objects.equals(getId(), resume.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
