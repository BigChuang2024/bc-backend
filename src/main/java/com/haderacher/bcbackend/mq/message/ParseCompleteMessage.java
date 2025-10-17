package com.haderacher.bcbackend.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParseCompleteMessage {
    private String username;
    private String contentId; // Mongo 中 ResumeContent 的 id
}
