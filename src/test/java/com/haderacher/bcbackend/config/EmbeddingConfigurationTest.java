package com.haderacher.bcbackend.config;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class EmbeddingConfigurationTest {

    @Autowired
    EmbeddingModel embeddingModel;

    @Test
    public void testEmbeddingIsAvailable() {
        float[] embedded = embeddingModel.embed("测试embedding是否可用");
        Assertions.assertNotNull(embedded);
    }
}
