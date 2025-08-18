package com.haderacher.bcbackend.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.JedisPooled;

@Configuration
public class VectorStoreConfiguration {

    @Bean
    @Primary
    @Qualifier("jobVectorStore")
    public VectorStore jobVectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingMode) {
        return RedisVectorStore.builder(jedisPooled, embeddingMode)
                .indexName("jobs")
                .prefix("job:")
                .build();
    }

    @Bean
    @Qualifier("resumeVectorStore")
    public VectorStore resumeVectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingMode) {
        return RedisVectorStore.builder(jedisPooled, embeddingMode)
                .indexName("resumes")
                .prefix("resume:")
                .build();
    }

    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled("localhost", 6379);
    }
}
