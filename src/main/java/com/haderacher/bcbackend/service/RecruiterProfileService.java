package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.model.RecruiterProfile;
import com.haderacher.bcbackend.repository.RecruiterProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class RecruiterProfileService{
    @Autowired
    private RecruiterProfileRepository recruiterProfileRepository;

    public List<RecruiterProfile> getAllRecruiters() {
        return recruiterProfileRepository.findAll();
    }
}
