package com.haderacher.bcbackend.mq.message;

import lombok.Data;

@Data
public class KnowledgePayloadMessage {
    private String title;
    private String content;
    private String type; // "text" or "json"
    private String description;
}
