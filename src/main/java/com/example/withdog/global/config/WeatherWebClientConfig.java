package com.example.withdog.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WeatherWebClientConfig {

    //기상청 API 전용 커넥션 풀: 외부 API가 느려져도 톰캣 스레드가 무한정 쌓이지 않도록 동시 호출 수를 제한한다
    @Bean
    public WebClient weatherWebClient(@Value("${weather.base-url}") String baseUrl) {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("weather-client-pool")
                .maxConnections(20)
                .pendingAcquireMaxCount(50)
                .pendingAcquireTimeout(Duration.ofSeconds(3))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofSeconds(3));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
