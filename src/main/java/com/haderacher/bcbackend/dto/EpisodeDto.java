package com.haderacher.bcbackend.dto;

import com.haderacher.bcbackend.mq.producer.KnowledgeProducer;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class EpisodeDto {
    String title;
    String content;
    String desc;
    KnowledgeProducer.TYPE type;
}
