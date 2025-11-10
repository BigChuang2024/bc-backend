package com.haderacher.bcbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
public class BcBackendApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(BcBackendApplication.class, args);
    }

}
