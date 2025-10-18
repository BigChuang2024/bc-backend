package com.haderacher.bcbackend.repository;

import com.haderacher.bcbackend.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    @Transactional
    @Modifying
    @Query("UPDATE Resume r SET r.mongoId = :mongoId WHERE r.fileName = :fileName")
    void updateMongoIdByFileName(@Param("mongoId") String mongoId, @Param("fileName") String fileName);

    // find resume by its fileName (used as identifier)
    Optional<Resume> findByFileName(String fileName);
}