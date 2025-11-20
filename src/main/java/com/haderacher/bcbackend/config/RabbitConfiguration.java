package com.haderacher.bcbackend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    public static final String RESUME_EXCHANGE = "resume-exchange";
    public static final String RESUME_PARSE_QUEUE = "resume.parse.queue";
    public static final String RESUME_EMBED_QUEUE = "resume.embed.queue";
    public static final String RESUME_EXTRACT_QUEUE = "resume.extract.queue";
    public static final String RESUME_PARSE_ROUTING = "resume.parse";
    public static final String RESUME_EMBED_ROUTING = "resume.embed";
    public static final String RESUME_EXTRACT_ROUTING = "resume.extract";
    public static final String KNOWLEDGE_GRAPH_BUILD_QUEUE =  "knowledge_graph_build_queue";

    @Bean
    public Queue knowledgeGraphBuildQueue() {
        return new Queue(KNOWLEDGE_GRAPH_BUILD_QUEUE, true);
    }

    @Bean
    public TopicExchange resumeExchange() {
        return new TopicExchange(RESUME_EXCHANGE);
    }

    @Bean
    public Queue parseQueue() {
        return new Queue(RESUME_PARSE_QUEUE, true);
    }

    @Bean
    public Queue embedQueue() {
        return new Queue(RESUME_EMBED_QUEUE, true);
    }

    @Bean
    public Queue extractQueue() {
        return new Queue(RESUME_EXTRACT_QUEUE, true);
    }

    @Bean
    public Binding parseBinding(Queue parseQueue, TopicExchange resumeExchange) {
        return BindingBuilder.bind(parseQueue).to(resumeExchange).with(RESUME_PARSE_ROUTING);
    }

    @Bean
    public Binding embedBinding(Queue embedQueue, TopicExchange resumeExchange) {
        return BindingBuilder.bind(embedQueue).to(resumeExchange).with(RESUME_EMBED_ROUTING);
    }

    @Bean
    public Binding extractBinding(Queue extractQueue, TopicExchange resumeExchange) {
        return BindingBuilder.bind(extractQueue).to(resumeExchange).with(RESUME_EXTRACT_ROUTING);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate rt = new RabbitTemplate(connectionFactory);
        rt.setMessageConverter(converter);
        return rt;
    }
}
