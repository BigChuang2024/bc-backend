package com.haderacher.bcbackend.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractRequestMessage {
    private String username;
    private String contentId;
}

