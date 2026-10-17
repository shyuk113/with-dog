package com.example.withdog.feed.infrastructure.messaging;

import com.example.withdog.post.domain.event.PostCreatedEvent;
import com.example.withdog.post.domain.event.PostDeletedEvent;

import java.time.Instant;
import java.util.UUID;

//feed.post 토픽 메시지 (JSON)
public record PostFeedMessage(String eventId, Type type, Long postId, String regionCode, Instant occurredAt) {

    public enum Type {
        CREATED,
        DELETED
    }

    public static PostFeedMessage from(PostCreatedEvent event) {
        return new PostFeedMessage(UUID.randomUUID().toString(), Type.CREATED, event.postId(), event.regionCode(), Instant.now());
    }

    public static PostFeedMessage from(PostDeletedEvent event) {
        return new PostFeedMessage(UUID.randomUUID().toString(), Type.DELETED, event.postId(), event.regionCode(), Instant.now());
    }
}
