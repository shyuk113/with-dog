package com.example.withdog.feed.infrastructure.messaging;

import com.example.withdog.feed.FeedEventTransport;
import com.example.withdog.post.domain.event.PostCreatedEvent;
import com.example.withdog.post.domain.event.PostDeletedEvent;
import com.example.withdog.post.domain.event.PostEngagementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

//커밋된 게시글/반응 이벤트를 Kafka로 발행한다 (롤백된 작업은 발행되지 않음)
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = FeedEventTransport.PROPERTY, havingValue = FeedEventTransport.KAFKA)
public class KafkaFeedEventRelay {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostCreated(PostCreatedEvent event) {
        send(FeedTopics.POST, event.regionCode(), PostFeedMessage.from(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostDeleted(PostDeletedEvent event) {
        send(FeedTopics.POST, event.regionCode(), PostFeedMessage.from(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostEngagement(PostEngagementEvent event) {
        send(FeedTopics.POST_ENGAGEMENT, event.postId().toString(), PostEngagementMessage.from(event));
    }

    //이미 커밋된 요청을 실패로 만들지 않도록 발행 실패는 기록만 한다 (유실 방지가 필요하면 Transactional Outbox로 확장)
    private void send(String topic, String key, Object message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(topic, key, payload).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Kafka 발행 실패 topic={}, key={}: {}", topic, key, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Kafka 발행 실패 topic={}, key={}: {}", topic, key, e.getMessage(), e);
        }
    }
}
