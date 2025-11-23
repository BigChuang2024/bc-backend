package com.haderacher.bcbackend.model;

import com.haderacher.bcbackend.mq.producer.KnowledgeProducer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Entity
@NoArgsConstructor
public class Episode {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String Title;
    @Column(columnDefinition = "TEXT")
    String content;
    String description;
    KnowledgeProducer.TYPE type;
}
