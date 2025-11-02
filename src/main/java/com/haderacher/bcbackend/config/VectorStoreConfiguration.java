package com.haderacher.bcbackend.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.JedisPooled;

@Configuration
public class VectorStoreConfiguration {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Bean
    @Primary
    @Qualifier("jobVectorStore")
    public VectorStore jobVectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingMode) {
        return RedisVectorStore.builder(jedisPooled, embeddingMode)
                .initializeSchema(true)
                .indexName("custom-index")
                .prefix("job:")
                .build();
    }

    @Bean
    @Qualifier("resumeVectorStore")
    public VectorStore resumeVectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingMode) {
        return RedisVectorStore.builder(jedisPooled, embeddingMode)
                .indexName("custom-index")
                .prefix("resume:")
                .build();
    }

    @Bean
    public JedisPooled jedisPooled() {

        return new JedisPooled(redisHost, 6379);
    }
}
