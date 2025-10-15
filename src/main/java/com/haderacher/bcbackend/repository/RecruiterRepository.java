package com.haderacher.bcbackend.repository;

import com.haderacher.bcbackend.model.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {
    Optional<Recruiter> findByUsername(String username);
}