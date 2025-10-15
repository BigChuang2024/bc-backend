package com.haderacher.bcbackend.repository;

import com.haderacher.bcbackend.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
}
