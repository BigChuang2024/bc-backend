package com.haderacher.bcbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "data")
    private String data;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "jobs_skills",
            joinColumns = @JoinColumn(name = "skill_id"),
            inverseJoinColumns = @JoinColumn(name = "job_id")
    )
    private Set<Job> jobs = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_skills",
            joinColumns = @JoinColumn(name = "skill_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();

}
