package com.example.withdog.feed;

//게시글 이벤트를 피드에 전달하는 방식 (feed.event.transport)
//  direct: 같은 프로세스 안에서 커밋 직후 바로 Redis에 반영 (기본값, 로컬/테스트에서 Kafka 없이 실행 가능)
//  kafka : 커밋 직후 Kafka로 발행하고, consumer가 Redis에 반영
public final class FeedEventTransport {

    public static final String PROPERTY = "feed.event.transport";
    public static final String DIRECT = "direct";
    public static final String KAFKA = "kafka";

    private FeedEventTransport() {
    }
}
