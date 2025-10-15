package com.haderacher.bcbackend.model;

import com.haderacher.bcbackend.model.valueobject.Authority;
import com.haderacher.bcbackend.model.valueobject.Education;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Deprecated
@Builder
@Entity // 标记这是一个 JPA 实体
@Table(name = "students") // 映射到数据库表名为 'students'
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student implements CredentialsContainer, UserDetails {

    @Id // 标记为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增长策略
    private Long id;

    @Column(nullable = false, unique = true, length = 50) // 用户名，不允许为空，唯一，最大长度50
    private String username;

    @Column(nullable = false, length = 100) // 密码，不允许为空，需要加密存储
    private String password;

    @Column(unique = true, length = 100) // 邮箱，不允许为空，唯一，最大长度100
    private String email;

    @Column(length = 50) // 真实姓名
    private String fullName;

    @Column(nullable = false, length = 20) // 手机号
    private String phoneNumber;

    @Column(length = 100) // 所在大学
    private String university;

    @Column(length = 50) // 专业
    private String major;

    @Column(length = 20) // 学历 (例如：本科, 硕士, 博士)
    private String degree;

    private Integer graduationYear; // 毕业年份

    @Lob // 大对象类型，适用于存储长文本（例如：自我介绍、个人描述）
    @Column(columnDefinition = "TEXT") // 明确指定数据库字段类型为 TEXT
    private String selfIntroduction;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "student_educations",
            joinColumns = @JoinColumn(name = "student_id")
    )
    private List<Education> educationHistory = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "student_authorities",
            joinColumns =  @JoinColumn(name = "student_id")
    )
    private List<Authority> authorities = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Resume> resumes = new HashSet<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.authorities = new ArrayList<>();
        authorities.add(new Authority(Authority.RoleType.ROLE_STUDENT));
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
        Student student = (Student) o;
        return getId() != null && Objects.equals(getId(), student.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}

