package com.haderacher.bcbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.*;

@Entity
@Table(name = "job")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "recruiter")
    private String recruiter;

    @Column(name = "address")
    private String address;

    @Column(name = "degree_req")
    private String degreeReq;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    @Column(name = "salary_type")
    private String salaryType;

    @Column(name = "salary_annual")
    private Integer salaryAnnual;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_labels", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "label")
    private List<String> labels = new ArrayList<>();

    @Column(name = "category")
    private String category;

    @ManyToMany(mappedBy = "jobs", fetch = FetchType.LAZY)
    private Set<Skill> skills = new HashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Job job = (Job) o;
        return getId() != null && Objects.equals(getId(), job.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
