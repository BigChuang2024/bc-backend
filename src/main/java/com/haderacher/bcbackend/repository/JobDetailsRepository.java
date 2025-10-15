package com.haderacher.bcbackend.repository;

import com.haderacher.bcbackend.model.JobDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JobDetailsRepository extends MongoRepository<JobDetails, String> {
}