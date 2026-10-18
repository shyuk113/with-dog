package com.example.withdog.feed.infrastructure.messaging;

import com.example.withdog.feed.FeedEventTransport;
import com.example.withdog.feed.application.HotPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

//feed.post-engagement 토픽을 구독해 동네 인기글 점수에 반영한다
//ZINCRBY는 멱등하지 않으므로 eventId로 중복 수신을 걸러낸다 (HotPostRepository.incrementOnce)
//예외는 던져서 FeedKafkaConfig의 에러 핸들러가 재시도 -> DLT 처리하도록 한다
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = FeedEventTransport.PROPERTY, havingValue = FeedEventTransport.KAFKA)
public class HotPostKafkaConsumer {

    public static final String GROUP_ID = "hot-post";

    private final HotPostService hotPostService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = FeedTopics.POST_ENGAGEMENT, groupId = GROUP_ID)
    public void consume(String payload) {
        PostEngagementMessage message = objectMapper.readValue(payload, PostEngagementMessage.class);
        hotPostService.record(message.eventId(), message.type(), message.postId(),
                message.regionCode(), message.count(), message.occurredAt());
    }
}
