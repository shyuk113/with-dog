package com.example.withdog.feed.application;

import com.example.withdog.comment.application.CommentService;
import com.example.withdog.comment.application.dto.CreateCommentRequest;
import com.example.withdog.feed.application.dto.HotFeedResponse;
import com.example.withdog.feed.application.dto.HotPostResponse;
import com.example.withdog.feed.infrastructure.HotPostRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.post.application.PostService;
import com.example.withdog.post.application.dto.CreatePostRequest;
import com.example.withdog.post.domain.event.EngagementType;
import com.example.withdog.post.infrastructure.PostRepository;
import com.example.withdog.user.application.UserService;
import com.example.withdog.user.application.dto.UpdateUserProfileRequest;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

//좋아요/댓글 커밋 이후(AFTER_COMMIT)에 점수가 반영되는지 검증해야 하므로 테스트 트랜잭션(롤백)을 쓰지 않고, 만든 데이터를 직접 정리한다
@SpringBootTest
@ActiveProfiles("local")
class HotPostServiceTest {

    //MockRegionClient 기준 좌표 -> 시군구
    private static final String GANGNAM = "11680";
    private static final String MAPO = "11440";

    @Autowired private HotPostService hotPostService;
    @Autowired private HotPostRepository hotPostRepository;
    @Autowired private PostService postService;
    @Autowired private CommentService commentService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private StringRedisTemplate stringRedisTemplate;

    private final List<Long> createdUserIds = new ArrayList<>();
    private final List<long[]> createdPosts = new ArrayList<>(); // {postId, authorId}

    private Long author;
    private Long neighbor;
    private Long mapoUser;

    @BeforeEach
    void setUp() {
        clearScores();
        author = createUser("인기글작성자", "hot-author@example.com", 37.4979, 127.0276);
        neighbor = createUser("인기글이웃", "hot-neighbor@example.com", 37.5000, 127.0300);
        mapoUser = createUser("마포주민", "hot-mapo@example.com", 37.5665, 126.9780);
    }

    @AfterEach
    void tearDown() {
        for (long[] post : createdPosts) {
            if (postRepository.existsById(post[0])) {
                postService.deletePost(post[0], post[1]);
            }
        }
        userRepository.deleteAllById(createdUserIds);
        clearScores();
    }

    @Test
    void 좋아요와_댓글_점수가_높은_순으로_보이고_반응없는_글은_빠진다() {
        Long liked = write(author, "좋아요 1개");
        Long commented = write(author, "댓글 2개");
        write(author, "반응 없음");

        postService.likePost(liked, neighbor);
        commentService.createComment(new CreateCommentRequest("좋아요", null), commented, neighbor);
        commentService.createComment(new CreateCommentRequest("저도요", null), commented, author);

        HotFeedResponse hot = hotPostService.getHotPosts(neighbor, 10);

        assertThat(hot.regionCode()).isEqualTo(GANGNAM);
        assertThat(hot.posts()).extracting(p -> p.post().id()).containsExactly(commented, liked);
        assertThat(hot.posts()).extracting(HotPostResponse::score).containsExactly(4.0, 1.0);
        assertThat(hot.posts().get(1).post().likedByMe()).isTrue();
    }

    @Test
    void 좋아요를_취소하면_점수가_빠지고_0점인_글은_보이지_않는다() {
        Long postId = write(author, "좋아요 취소될 글");
        postService.likePost(postId, neighbor);

        postService.unlikePost(postId, neighbor);

        assertThat(hotPostService.getHotPosts(neighbor, 10).posts()).isEmpty();
    }

    @Test
    void 대댓글이_달린_댓글을_지우면_대댓글_점수까지_빠진다() {
        Long postId = write(author, "댓글 지워질 글");
        Long commentId = commentService.createComment(new CreateCommentRequest("댓글", null), postId, neighbor).id();
        commentService.createComment(new CreateCommentRequest("대댓글", commentId), postId, author);
        postService.likePost(postId, neighbor);

        commentService.deleteComment(commentId, postId, neighbor);

        assertThat(hotPostService.getHotPosts(neighbor, 10).posts())
                .singleElement()
                .extracting(HotPostResponse::score).isEqualTo(1.0);
    }

    @Test
    void 같은_이벤트를_두번_받아도_한번만_반영된다() {
        Long postId = write(author, "중복 수신 대상");
        String eventId = UUID.randomUUID().toString();

        hotPostService.record(eventId, EngagementType.LIKED, postId, GANGNAM, 1, Instant.now());
        hotPostService.record(eventId, EngagementType.LIKED, postId, GANGNAM, 1, Instant.now());

        assertThat(hotPostService.getHotPosts(neighbor, 10).posts())
                .singleElement()
                .extracting(HotPostResponse::score).isEqualTo(1.0);
    }

    @Test
    void 오래된_반응일수록_낮게_반영되고_3일이_지나면_빠진다() {
        Long recent = write(author, "오늘 좋아요 1개");
        Long older = write(author, "그제 좋아요 3개");
        Long expired = write(author, "사흘 전 좋아요 10개");

        likeAt(recent, 1, 0);
        likeAt(older, 3, 2);   // 3 * 0.25 = 0.75
        likeAt(expired, 10, 3); // 집계 기간(오늘~그제) 밖

        HotFeedResponse hot = hotPostService.getHotPosts(neighbor, 10);

        assertThat(hot.posts()).extracting(p -> p.post().id()).containsExactly(recent, older);
        assertThat(hot.posts()).extracting(HotPostResponse::score).containsExactly(1.0, 0.75);
    }

    @Test
    void 다른_동네_글의_반응은_섞이지_않는다() {
        Long mapoPost = write(mapoUser, "마포 글");
        postService.likePost(mapoPost, neighbor);

        assertThat(hotPostService.getHotPosts(neighbor, 10).posts()).isEmpty();
        assertThat(hotPostService.getHotPosts(mapoUser, 10).posts())
                .extracting(p -> p.post().id()).containsExactly(mapoPost);
    }

    @Test
    void 삭제된_글은_인기글에서_빠지고_점수도_정리된다() {
        Long kept = write(author, "남는 글");
        Long deleted = write(author, "지울 글");
        postService.likePost(kept, neighbor);
        postService.likePost(deleted, neighbor);

        postService.deletePost(deleted, author);

        assertThat(hotPostService.getHotPosts(neighbor, 10).posts())
                .extracting(p -> p.post().id()).containsExactly(kept);
        assertThat(hotPostRepository.findTop(GANGNAM, today(), 10))
                .extracting(HotPostRepository.ScoredPost::postId).containsExactly(kept);
    }

    @Test
    void 동네가_설정되지_않은_사용자는_인기글을_볼_수_없다() {
        User noRegion = userRepository.save(User.createUserLocal("미설정", "hot-none@example.com", "encoded-password", "서울"));
        createdUserIds.add(noRegion.getId());

        assertThatThrownBy(() -> hotPostService.getHotPosts(noRegion.getId(), 10))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGION_REQUIRED);
    }

    private void likeAt(Long postId, int count, int daysAgo) {
        Instant occurredAt = Instant.now().minus(Duration.ofDays(daysAgo));
        hotPostService.record(UUID.randomUUID().toString(), EngagementType.LIKED, postId, GANGNAM, count, occurredAt);
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

    private LocalDate today() {
        return LocalDate.now(HotPostService.ZONE);
    }

    private void clearScores() {
        hotPostRepository.deleteRecent(GANGNAM, today());
        hotPostRepository.deleteRecent(MAPO, today());
        //집계 기간 밖(사흘 전)에 쓴 키도 정리
        stringRedisTemplate.delete("hot:region:" + GANGNAM + ":" + today().minusDays(3).format(DateTimeFormatter.BASIC_ISO_DATE));
    }
}
