package com.example.withdog.feed.infrastructure.messaging;

import com.example.withdog.post.domain.event.EngagementType;
import com.example.withdog.post.domain.event.PostEngagementEvent;

import java.time.Instant;
import java.util.UUID;

//feed.post-engagement 토픽 메시지 (JSON). eventId는 consumer가 중복 수신을 걸러내는 데 사용한다
public record PostEngagementMessage(String eventId, EngagementType type, Long postId, String regionCode,
                                    Long userId, int count, Instant occurredAt) {

    public static PostEngagementMessage from(PostEngagementEvent event) {
        return new PostEngagementMessage(UUID.randomUUID().toString(), event.type(), event.postId(),
                event.regionCode(), event.userId(), event.count(), Instant.now());
    }
}
