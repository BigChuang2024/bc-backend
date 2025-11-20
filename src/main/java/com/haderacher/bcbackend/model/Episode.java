package com.haderacher.bcbackend.model;

import com.haderacher.bcbackend.mq.producer.KnowledgeProducer;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Entity
@NoArgsConstructor
public class Episode {
    @Id()
    private Long id;
    String Title;
    String content;
    String description;
    KnowledgeProducer.TYPE type;
}
