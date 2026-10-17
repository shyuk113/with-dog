package com.example.withdog.feed.application;

import com.example.withdog.feed.application.dto.RegionFeedResponse;
import com.example.withdog.feed.infrastructure.RegionFeedRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.post.application.PostService;
import com.example.withdog.post.application.dto.CreatePostRequest;
import com.example.withdog.post.application.dto.PostSummaryResponse;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

//게시글 작성 커밋 이후(AFTER_COMMIT)에 피드가 반영되는지 검증해야 하므로 테스트 트랜잭션(롤백)을 쓰지 않고, 만든 데이터를 직접 정리한다
@SpringBootTest
@ActiveProfiles("local")
class FeedServiceTest {

    //MockRegionClient 기준 좌표 -> 시군구
    private static final String GANGNAM = "11680";
    private static final String MAPO = "11440";

    @Autowired private FeedService feedService;
    @Autowired private PostService postService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private RegionFeedRepository regionFeedRepository;

    private final List<Long> createdUserIds = new ArrayList<>();
    private final List<long[]> createdPosts = new ArrayList<>(); // {postId, authorId}

    private Long gangnamUser;
    private Long gangnamNeighbor;
    private Long mapoUser;

    @BeforeEach
    void setUp() {
        clearFeeds();
        gangnamUser = createUser("강남주민", "feed-gangnam@example.com", 37.4979, 127.0276);
        gangnamNeighbor = createUser("강남이웃", "feed-gangnam2@example.com", 37.5000, 127.0300);
        mapoUser = createUser("마포주민", "feed-mapo@example.com", 37.5665, 126.9780);
    }

    @AfterEach
    void tearDown() {
        for (long[] post : createdPosts) {
            if (postRepository.existsById(post[0])) {
                postService.deletePost(post[0], post[1]);
            }
        }
        userRepository.deleteAllById(createdUserIds);
        clearFeeds();
    }

    @Test
    void 같은_동네_글만_최신순으로_보인다() {
        Long first = write(gangnamUser, "강남 첫 글");
        write(mapoUser, "마포 글");
        Long second = write(gangnamNeighbor, "강남 두번째 글");

        RegionFeedResponse feed = feedService.getRegionFeed(gangnamUser, null, 20);

        assertThat(feed.regionCode()).isEqualTo(GANGNAM);
        assertThat(feed.regionName()).isEqualTo("서울특별시 강남구");
        assertThat(feed.posts()).extracting(PostSummaryResponse::id).containsExactly(second, first);
        assertThat(feed.hasNext()).isFalse();
        assertThat(feed.nextCursor()).isNull();
    }

    @Test
    void 글을_작성하면_커밋_이후_동네_피드_캐시에_반영된다() {
        Long postId = write(gangnamUser, "캐시 반영 확인");

        assertThat(regionFeedRepository.findPostIds(GANGNAM, null, 10)).containsExactly(postId);
        assertThat(regionFeedRepository.findPostIds(MAPO, null, 10)).isEmpty();
    }

    @Test
    void 커서로_다음_페이지를_이어서_조회한다() {
        Long p1 = write(gangnamUser, "1");
        Long p2 = write(gangnamUser, "2");
        Long p3 = write(gangnamUser, "3");

        RegionFeedResponse firstPage = feedService.getRegionFeed(gangnamUser, null, 2);
        assertThat(firstPage.posts()).extracting(PostSummaryResponse::id).containsExactly(p3, p2);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.nextCursor()).isEqualTo(p2);

        RegionFeedResponse secondPage = feedService.getRegionFeed(gangnamUser, firstPage.nextCursor(), 2);
        assertThat(secondPage.posts()).extracting(PostSummaryResponse::id).containsExactly(p1);
        assertThat(secondPage.hasNext()).isFalse();
    }

    @Test
    void 캐시가_비어있으면_DB에서_조회하고_캐시를_다시_채운다() {
        Long p1 = write(gangnamUser, "1");
        Long p2 = write(gangnamUser, "2");
        regionFeedRepository.deleteFeed(GANGNAM); // Redis 재시작/유실 상황

        RegionFeedResponse feed = feedService.getRegionFeed(gangnamUser, null, 20);

        assertThat(feed.posts()).extracting(PostSummaryResponse::id).containsExactly(p2, p1);
        assertThat(regionFeedRepository.findPostIds(GANGNAM, null, 10)).containsExactly(p2, p1);
    }

    @Test
    void 캐시_보관범위를_넘어선_오래된_글은_DB에서_이어서_채운다() {
        Long old = write(gangnamUser, "캐시에서 밀려난 오래된 글");
        Long recent = write(gangnamUser, "최근 글");
        regionFeedRepository.remove(GANGNAM, List.of(old)); // trim으로 잘려나간 상황

        RegionFeedResponse feed = feedService.getRegionFeed(gangnamUser, null, 20);

        assertThat(feed.posts()).extracting(PostSummaryResponse::id).containsExactly(recent, old);
    }

    @Test
    void 삭제한_글은_피드와_캐시에서_빠진다() {
        Long kept = write(gangnamUser, "남는 글");
        Long deleted = write(gangnamUser, "지울 글");

        postService.deletePost(deleted, gangnamUser);

        assertThat(feedService.getRegionFeed(gangnamUser, null, 20).posts())
                .extracting(PostSummaryResponse::id).containsExactly(kept);
        assertThat(regionFeedRepository.findPostIds(GANGNAM, null, 10)).containsExactly(kept);
    }

    @Test
    void 캐시에만_남은_삭제된_글은_조회_시점에_정리된다() {
        Long kept = write(gangnamUser, "남는 글");
        long ghostId = kept + 1_000_000; // 삭제 이벤트가 유실돼 캐시에만 남은 글
        regionFeedRepository.add(GANGNAM, ghostId);

        RegionFeedResponse feed = feedService.getRegionFeed(gangnamUser, null, 20);

        assertThat(feed.posts()).extracting(PostSummaryResponse::id).containsExactly(kept);
        assertThat(regionFeedRepository.findPostIds(GANGNAM, null, 10)).doesNotContain(ghostId);
    }

    @Test
    void 피드에도_좋아요_수와_내_좋아요_여부가_붙는다() {
        Long postId = write(gangnamUser, "좋아요 받을 글");
        postService.likePost(postId, gangnamNeighbor);

        PostSummaryResponse mine = feedService.getRegionFeed(gangnamNeighbor, null, 20).posts().getFirst();

        assertThat(mine.likeCount()).isEqualTo(1);
        assertThat(mine.likedByMe()).isTrue();
    }

    @Test
    void 동네가_설정되지_않은_사용자는_피드를_볼_수_없다() {
        User noRegion = userRepository.save(User.createUserLocal("미설정", "feed-none@example.com", "encoded-password", "서울"));
        createdUserIds.add(noRegion.getId());

        assertThatThrownBy(() -> feedService.getRegionFeed(noRegion.getId(), null, 20))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGION_REQUIRED);
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

    private void clearFeeds() {
        regionFeedRepository.deleteFeed(GANGNAM);
        regionFeedRepository.deleteFeed(MAPO);
    }
}
