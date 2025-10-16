package com.haderacher.bcbackend.repository;

import com.haderacher.bcbackend.model.Resume;
import com.haderacher.bcbackend.model.ResumeContent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResumeContentRepository extends MongoRepository<ResumeContent, String> {
}
