package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.dto.CreateJobDto;
import com.haderacher.bcbackend.dto.UpdateJobDto;
import com.haderacher.bcbackend.exception.ResourceNotFoundException;
import com.haderacher.bcbackend.model.Job;
import com.haderacher.bcbackend.repository.JobRepository;
import com.haderacher.bcbackend.service.mapper.JobMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    public Page<Job> findAll(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }

    public Job findById(UUID id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
    }

    @Transactional
    public Job create(CreateJobDto dto) {
        Job job = JobMapper.fromCreateDto(dto);
        return jobRepository.save(job);
    }

    @Transactional
    public Job update(UUID id, UpdateJobDto dto) {
        Job existing = findById(id);
        JobMapper.updateFromDto(dto, existing);
        return jobRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        Job existing = findById(id);
        jobRepository.delete(existing);
    }
}
