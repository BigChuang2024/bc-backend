package com.haderacher.bcbackend.service.mapper;

import com.haderacher.bcbackend.dto.CreateJobDto;
import com.haderacher.bcbackend.dto.UpdateJobDto;
import com.haderacher.bcbackend.model.Job;

import java.util.ArrayList;
import java.util.List;

public final class JobMapper {

    private JobMapper() {}

    public static Job fromCreateDto(CreateJobDto dto) {
        Job job = new Job();
        job.setName(dto.getName());
        job.setDescription(dto.getDescription());
        job.setCompany(dto.getCompany());
        job.setRecruiter(dto.getRecruiter());
        job.setAddress(dto.getAddress());
        job.setDegreeReq(dto.getDegreeReq());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setSalaryType(dto.getSalaryType());
        job.setSalaryAnnual(dto.getSalaryAnnual());
        job.setCategory(dto.getCategory());
        List<String> labels = dto.getLabels() == null ? new ArrayList<>() : new ArrayList<>(dto.getLabels());
        job.setLabels(labels);
        return job;
    }

    public static void updateFromDto(UpdateJobDto dto, Job job) {
        job.setName(dto.getName());
        job.setDescription(dto.getDescription());
        job.setCompany(dto.getCompany());
        job.setRecruiter(dto.getRecruiter());
        job.setAddress(dto.getAddress());
        job.setDegreeReq(dto.getDegreeReq());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setSalaryType(dto.getSalaryType());
        job.setSalaryAnnual(dto.getSalaryAnnual());
        job.setCategory(dto.getCategory());
        List<String> labels = dto.getLabels() == null ? new ArrayList<>() : new ArrayList<>(dto.getLabels());
        job.setLabels(labels);
    }
}
