package com.haderacher.bcbackend.mq.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haderacher.bcbackend.config.RabbitConfiguration;
import com.haderacher.bcbackend.dto.EpisodeDto;
import com.haderacher.bcbackend.model.Episode;
import com.haderacher.bcbackend.mq.message.KnowledgePayloadMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class KnowledgeProducer {

    private AmqpTemplate rabbitTemplate;


    public Episode sendToBuildGraph(EpisodeDto episodeDto) {
        try {
            KnowledgePayloadMessage payload = new KnowledgePayloadMessage();
            payload.setTitle(episodeDto.getTitle());
            payload.setContent(episodeDto.getContent());
            payload.setType(episodeDto.getType().toString());
            payload.setDescription(episodeDto.getDesc());

            String QUEUE_NAME = RabbitConfiguration.KNOWLEDGE_GRAPH_BUILD_QUEUE;
            rabbitTemplate.convertAndSend(QUEUE_NAME, payload);
            log.info("Sent task to Python consumer: {}", payload.getTitle());

        } catch (Exception e) {
            log.error("Error while sending task to Python consumer", e);
        }
        return new Episode(null, episodeDto.getTitle(), episodeDto.getContent(), episodeDto.getDesc(), episodeDto.getType());
    }

    public enum TYPE {
        text("text"),
        json("json")

        ;
        final String type;
        TYPE(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }
}