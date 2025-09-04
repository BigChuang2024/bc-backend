package com.haderacher.bcbackend.entity.aggregates.recruiter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {
    Optional<Recruiter> findByUsername(String username);
}