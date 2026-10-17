package com.example.withdog.feed.infrastructure.messaging;

import com.example.withdog.comment.application.CommentService;
import com.example.withdog.comment.application.dto.CreateCommentRequest;
import com.example.withdog.feed.infrastructure.RegionFeedRepository;
import com.example.withdog.post.application.PostService;
import com.example.withdog.post.application.dto.CreatePostRequest;
import com.example.withdog.post.domain.event.EngagementType;
import com.example.withdog.post.infrastructure.PostRepository;
import com.example.withdog.user.application.UserService;
import com.example.withdog.user.application.dto.UpdateUserProfileRequest;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

//실제 Kafka 경로(커밋 -> 발행 -> consumer -> Redis)를 임베디드 브로커로 검증한다 (CI에 Kafka 서비스 불필요)
//AFTER_COMMIT 발행을 검증해야 하므로 테스트 트랜잭션(롤백)을 쓰지 않고, 만든 데이터를 직접 정리한다
@SpringBootTest(properties = "feed.event.transport=kafka")
@ActiveProfiles("local")
@EmbeddedKafka(bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class FeedKafkaIntegrationTest {

    private static final String GANGNAM = "11680"; //MockRegionClient 기준 강남 좌표 -> 11680
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    @Autowired private PostService postService;
    @Autowired private CommentService commentService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private RegionFeedRepository regionFeedRepository;
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final List<Long> createdUserIds = new ArrayList<>();
    private final List<long[]> createdPosts = new ArrayList<>(); // {postId, authorId}

    private Long author;
    private Long neighbor;

    @BeforeEach
    void setUp() {
        regionFeedRepository.deleteFeed(GANGNAM);
        author = createUser("카프카작성자", "kafka-author@example.com", 37.4979, 127.0276);
        neighbor = createUser("카프카이웃", "kafka-neighbor@example.com", 37.5000, 127.0300);
    }

    @AfterEach
    void tearDown() {
        for (long[] post : createdPosts) {
            if (postRepository.existsById(post[0])) {
                postService.deletePost(post[0], post[1]);
            }
        }
        //삭제 메시지가 consumer에서 처리될 때까지 기다린 뒤 정리 (다른 테스트에 남은 id가 섞이지 않도록)
        awaitUntil(() -> regionFeedRepository.findPostIds(GANGNAM, null, 10).isEmpty());
        userRepository.deleteAllById(createdUserIds);
        regionFeedRepository.deleteFeed(GANGNAM);
    }

    @Test
    void 글을_작성하면_Kafka를_거쳐_동네_피드에_반영된다() {
        Long postId = write(author, "카프카로 전달되는 글");

        awaitUntil(() -> regionFeedRepository.findPostIds(GANGNAM, null, 10).contains(postId));
    }

    @Test
    void 글을_삭제하면_Kafka를_거쳐_동네_피드에서_제거된다() {
        Long postId = write(author, "지울 글");
        awaitUntil(() -> regionFeedRepository.findPostIds(GANGNAM, null, 10).contains(postId));

        postService.deletePost(postId, author);

        awaitUntil(() -> !regionFeedRepository.findPostIds(GANGNAM, null, 10).contains(postId));
    }

    @Test
    void 좋아요와_댓글은_engagement_토픽으로_발행된다() {
        Long postId = write(author, "반응 받을 글");
        postService.likePost(postId, neighbor);
        commentService.createComment(new CreateCommentRequest("좋은 코스네요", null), postId, neighbor);

        List<PostEngagementMessage> messages = new ArrayList<>();
        try (KafkaConsumer<String, String> consumer = consumerOf(FeedTopics.POST_ENGAGEMENT)) {
            pollUntil(consumer, record -> {
                PostEngagementMessage message = objectMapper.readValue(record.value(), PostEngagementMessage.class);
                if (postId.equals(message.postId())) {
                    messages.add(message);
                }
                return messages.size() >= 2;
            });
        }

        assertThat(messages).extracting(PostEngagementMessage::type)
                .containsExactlyInAnyOrder(EngagementType.LIKED, EngagementType.COMMENTED);
        assertThat(messages).allSatisfy(message -> {
            assertThat(message.regionCode()).isEqualTo(GANGNAM);
            assertThat(message.userId()).isEqualTo(neighbor);
            assertThat(message.eventId()).isNotBlank();
        });
    }

    @Test
    void 처리할_수_없는_메시지는_재시도하지_않고_DLT로_보내진다() {
        String poison = "not-json-" + UUID.randomUUID();
        kafkaTemplate.send(FeedTopics.POST, GANGNAM, poison);

        try (KafkaConsumer<String, String> consumer = consumerOf(FeedTopics.POST + FeedTopics.DLT_SUFFIX)) {
            pollUntil(consumer, record -> poison.equals(record.value()));
        }
    }

    private Long createUser(String name, String email, double lat, double lon) {
        User user = userRepository.save(User.createUserLocal(name, email, "encoded-password", "서울"));
        createdUserIds.add(user.getId());
        userService.updateUserProfile(new UpdateUserProfileRequest(name, "서울", lat, lon, null), user.getId(), user.getId());
        return user.getId();
    }

    private Long write(Long authorId, String title) {
        Long postId = postService.createPost(new CreatePostRequest(title, "내용", null), authorId).id();
        createdPosts.add(new long[]{postId, authorId});
        return postId;
    }

    private KafkaConsumer<String, String> consumerOf(String topic) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class));
        consumer.subscribe(List.of(topic));
        return consumer;
    }

    private void pollUntil(KafkaConsumer<String, String> consumer, Predicate<ConsumerRecord<String, String>> done) {
        long deadline = System.currentTimeMillis() + TIMEOUT.toMillis();
        while (System.currentTimeMillis() < deadline) {
            for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                if (done.test(record)) {
                    return;
                }
            }
        }
        throw new AssertionError("제한 시간 안에 기대한 메시지를 받지 못했습니다");
    }

    private void awaitUntil(BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + TIMEOUT.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
        }
        throw new AssertionError("제한 시간 안에 조건을 만족하지 못했습니다");
    }
}
