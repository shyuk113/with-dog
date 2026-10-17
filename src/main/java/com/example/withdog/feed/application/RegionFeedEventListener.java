package com.example.withdog.feed.application;

import com.example.withdog.feed.FeedEventTransport;
import com.example.withdog.feed.infrastructure.RegionFeedRepository;
import com.example.withdog.post.domain.event.PostCreatedEvent;
import com.example.withdog.post.domain.event.PostDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

//게시글 트랜잭션이 커밋된 뒤에만 피드에 반영한다 (롤백된 글이 피드에 들어가거나, 커밋 전 id가 노출되는 것을 방지)
//feed.event.transport=direct(기본값)일 때 사용. kafka일 때는 KafkaFeedEventRelay -> RegionFeedKafkaConsumer 경로로 반영된다
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = FeedEventTransport.PROPERTY, havingValue = FeedEventTransport.DIRECT, matchIfMissing = true)
public class RegionFeedEventListener {

    private final RegionFeedRepository regionFeedRepository;

    //Redis 장애가 이미 커밋된 글 작성/삭제 요청을 실패로 만들면 안 되므로 예외는 기록만 한다
    //(누락된 글은 조회 시 DB fallback, 남은 삭제 글은 조회 시 lazy cleanup이 보완)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostCreated(PostCreatedEvent event) {
        try {
            regionFeedRepository.add(event.regionCode(), event.postId());
        } catch (Exception e) {
            log.warn("동네 피드 반영 실패 postId={}, region={}: {}", event.postId(), event.regionCode(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostDeleted(PostDeletedEvent event) {
        try {
            regionFeedRepository.remove(event.regionCode(), List.of(event.postId()));
        } catch (Exception e) {
            log.warn("동네 피드 삭제 반영 실패 postId={}, region={}: {}", event.postId(), event.regionCode(), e.getMessage());
        }
    }
}
