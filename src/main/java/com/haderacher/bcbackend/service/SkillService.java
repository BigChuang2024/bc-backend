package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.dto.CreateSkillDto;
import com.haderacher.bcbackend.dto.UpdateSkillDto;
import com.haderacher.bcbackend.exception.ResourceNotFoundException;

import com.haderacher.bcbackend.model.Skill;
import com.haderacher.bcbackend.repository.SkillRepository;
import com.haderacher.bcbackend.service.mapper.SkillMapper;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
public class SkillService {
    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<Skill> findAll() {
        return skillRepository.findAll();
    }

    public Page<Skill> findAll(Pageable pageable) {
        return skillRepository.findAll(pageable);
    }

    public Skill findById(String id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));
    }
    //这里字段很少，mapper？？
    @Transactional
    public Skill create(CreateSkillDto dto) {
        Skill skill = SkillMapper.fromCreateDto(dto);
        return skillRepository.save(skill);
    }

    @Transactional
    public Skill update(String id, UpdateSkillDto dto) {
        Skill existing = findById(id);
        SkillMapper.updateFromDto(dto, existing);
        return skillRepository.save(existing);
    }

    @Transactional
    public void delete(String id) {
        Skill existing = findById(id);
        skillRepository.delete(existing);

    }
}
