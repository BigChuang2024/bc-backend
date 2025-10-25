package com.haderacher.bcbackend.controller;

import com.haderacher.bcbackend.common.ApiResponse;
import com.haderacher.bcbackend.dto.CreateSkillDto;
import com.haderacher.bcbackend.dto.UpdateSkillDto;
import com.haderacher.bcbackend.model.Skill;
import com.haderacher.bcbackend.repository.JobRepository;
import com.haderacher.bcbackend.repository.SkillRepository;
import com.haderacher.bcbackend.service.SkillService;
import com.haderacher.bcbackend.service.mapper.SkillMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("skills")
public class SkillController {

    private final SkillService skillService;
    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }
    @GetMapping
    public ApiResponse<Page<Skill>> list(Pageable pageable) {
        Page<Skill> page = skillService.findAll(pageable);
        return ApiResponse.success(page);
    }

    @PostMapping
    public ApiResponse<Skill> createSkill(@RequestBody CreateSkillDto dto) {
        Skill skill = skillService.create(dto);
        return ApiResponse.success(skill);
    }

    @GetMapping("/{id}")
    public ApiResponse<Skill> getSkillById(@PathVariable String id) {
        Skill skill = skillService.findById(id);
        return ApiResponse.success(skill);
    }
    @PutMapping("/{id}")
    public ApiResponse<Skill> updateSkill(@PathVariable String id, @RequestBody UpdateSkillDto dto) {
        Skill skill = skillService.update(id, dto);
        return ApiResponse.success(skill);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable String id) {
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
