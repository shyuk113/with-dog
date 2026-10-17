package com.example.withdog.feed.infrastructure.messaging;

import com.example.withdog.feed.FeedEventTransport;
import com.example.withdog.feed.infrastructure.RegionFeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

//feed.post 토픽을 구독해 동네 피드(Redis Sorted Set)에 반영한다
//예외를 잡지 않고 던져서 FeedKafkaConfig의 에러 핸들러가 재시도 -> DLT 처리하도록 한다
//ZADD/ZREM은 같은 메시지를 여러 번 처리해도 결과가 같아(멱등) at-least-once 중복 수신에도 안전하다
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = FeedEventTransport.PROPERTY, havingValue = FeedEventTransport.KAFKA)
public class RegionFeedKafkaConsumer {

    public static final String GROUP_ID = "region-feed";

    private final RegionFeedRepository regionFeedRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = FeedTopics.POST, groupId = GROUP_ID)
    public void consume(String payload) {
        PostFeedMessage message = objectMapper.readValue(payload, PostFeedMessage.class);
        switch (message.type()) {
            case CREATED -> regionFeedRepository.add(message.regionCode(), message.postId());
            case DELETED -> regionFeedRepository.remove(message.regionCode(), List.of(message.postId()));
        }
    }
}
