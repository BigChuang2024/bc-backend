package com.haderacher.bcbackend.repository;

import com.haderacher.bcbackend.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

}