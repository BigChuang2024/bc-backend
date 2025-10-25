package com.haderacher.bcbackend.service.mapper;

import com.haderacher.bcbackend.dto.CreateSkillDto;
import com.haderacher.bcbackend.dto.UpdateSkillDto;
import com.haderacher.bcbackend.model.Skill;

public class SkillMapper {
    public static Skill fromCreateDto(CreateSkillDto dto) {
        Skill skill = new Skill();
        skill.setName(dto.getName());
        skill.setData(dto.getData());
        return skill;
    }

    public static void updateFromDto(UpdateSkillDto dto, Skill existing) {
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getData() != null) {
            existing.setData(dto.getData());
        }
    }
}
