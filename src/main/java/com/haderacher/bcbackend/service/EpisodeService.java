package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.dto.EpisodeDto;
import com.haderacher.bcbackend.model.Episode;
import com.haderacher.bcbackend.mq.producer.KnowledgeProducer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EpisodeService {

    private KnowledgeProducer knowledgeProducer;
    
    public Episode sendToBuildGraph(EpisodeDto episodeDto) {

        return knowledgeProducer.sendToBuildGraph(episodeDto);
    }
}
