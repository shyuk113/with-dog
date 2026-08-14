package com.example.withdog.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiWebClientConfig {
    @Bean
    public WebClient aiWebClient(@Value("${ai.server.base-url}") String baseUrl){
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
