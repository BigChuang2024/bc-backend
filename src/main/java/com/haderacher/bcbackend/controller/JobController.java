package com.haderacher.bcbackend.controller;

import com.haderacher.bcbackend.common.ApiResponse;
import com.haderacher.bcbackend.dto.CreateJobDto;
import com.haderacher.bcbackend.dto.UpdateJobDto;
import com.haderacher.bcbackend.model.Job;
import com.haderacher.bcbackend.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public List<Job> list() {
        return jobService.findAll();
    }

    @GetMapping("/{id}")
    public ApiResponse<Job> getById(@PathVariable UUID id) {
        Job job = jobService.findById(id);
        return ApiResponse.success(job);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Job>> create(@Valid @RequestBody CreateJobDto dto) {
        Job created = jobService.create(dto);
        // build location header if id available
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ApiResponse<Job> update(@PathVariable UUID id, @Valid @RequestBody UpdateJobDto dto) {
        Job updated = jobService.update(id, dto);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        jobService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
