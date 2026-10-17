package com.example.withdog.feed.infrastructure.messaging;

import com.example.withdog.feed.FeedEventTransport;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.core.JacksonException;

@Configuration
@ConditionalOnProperty(name = FeedEventTransport.PROPERTY, havingValue = FeedEventTransport.KAFKA)
public class FeedKafkaConfig {

    //로컬 단일 브로커 기준 replicas=1 (운영 클러스터에서는 3 이상 권장)
    @Bean
    public NewTopic feedPostTopic() {
        return TopicBuilder.name(FeedTopics.POST).partitions(FeedTopics.PARTITIONS).replicas(1).build();
    }

    //DLT는 원본과 같은 파티션 번호로 보내므로 파티션 수를 맞춘다
    @Bean
    public NewTopic feedPostDeadLetterTopic() {
        return TopicBuilder.name(FeedTopics.POST + FeedTopics.DLT_SUFFIX).partitions(FeedTopics.PARTITIONS).replicas(1).build();
    }

    @Bean
    public NewTopic feedPostEngagementTopic() {
        return TopicBuilder.name(FeedTopics.POST_ENGAGEMENT).partitions(FeedTopics.PARTITIONS).replicas(1).build();
    }

    //일시적 장애(Redis 등)는 1초 간격 3회 재시도 후 DLT로 보내고 다음 메시지를 계속 처리한다
    //JSON 파싱 실패처럼 재시도해도 결과가 같은 메시지는 바로 DLT로 보낸다
    @Bean
    public CommonErrorHandler feedKafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + FeedTopics.DLT_SUFFIX, record.partition()));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
        errorHandler.addNotRetryableExceptions(JacksonException.class);
        return errorHandler;
    }
}
