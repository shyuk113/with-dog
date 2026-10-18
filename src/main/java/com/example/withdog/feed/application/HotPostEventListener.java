package com.example.withdog.feed.application;

import com.example.withdog.feed.FeedEventTransport;
import com.example.withdog.post.domain.event.PostEngagementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

//feed.event.transport=direct(기본값)일 때 좋아요/댓글을 커밋 직후 바로 인기글 점수에 반영한다
//kafka일 때는 KafkaFeedEventRelay -> HotPostKafkaConsumer 경로로 반영된다
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = FeedEventTransport.PROPERTY, havingValue = FeedEventTransport.DIRECT, matchIfMissing = true)
public class HotPostEventListener {

    private final HotPostService hotPostService;

    //이미 커밋된 좋아요/댓글 요청을 Redis 장애로 실패시키지 않도록 예외는 기록만 한다
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostEngagement(PostEngagementEvent event) {
        try {
            //프로세스 내 전달은 중복 수신이 없으므로 매번 새 eventId를 쓴다
            hotPostService.record(UUID.randomUUID().toString(), event.type(), event.postId(),
                    event.regionCode(), event.count(), Instant.now());
        } catch (Exception e) {
            log.warn("동네 인기글 점수 반영 실패 postId={}: {}", event.postId(), e.getMessage());
        }
    }
}
