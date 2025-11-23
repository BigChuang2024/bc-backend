package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.dto.EpisodeDto;
import com.haderacher.bcbackend.model.Episode;
import com.haderacher.bcbackend.mq.producer.KnowledgeProducer;
import com.haderacher.bcbackend.repository.EpisodeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EpisodeService {

    private KnowledgeProducer knowledgeProducer;

    private EpisodeRepository episodeRepository;
    
    public Episode sendToBuildGraph(EpisodeDto episodeDto) {
        Episode episode = knowledgeProducer.sendToBuildGraph(episodeDto);
        episode = episodeRepository.save(episode);
        return episode;
    }
}
