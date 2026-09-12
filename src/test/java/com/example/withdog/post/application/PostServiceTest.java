package com.example.withdog.post.application;

import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.post.application.dto.CreatePostRequest;
import com.example.withdog.post.application.dto.PostDetailResponse;
import com.example.withdog.post.application.dto.PostRouteRequest;
import com.example.withdog.post.application.dto.PostSummaryResponse;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    private Long authorId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        User author = userRepository.save(User.createUserLocal("작성자", "author@example.com", "encoded-password", "서울"));
        User other = userRepository.save(User.createUserLocal("다른유저", "other@example.com", "encoded-password", "부산"));
        authorId = author.getId();
        otherUserId = other.getId();
    }

    private CreatePostRequest requestWithRoute() {
        PostRouteRequest route = new PostRouteRequest(
                "한강 공원 코스", 3.2, 40,
                List.of(new PostRouteRequest.RouteStepRequest(37.5665, 126.9780), new PostRouteRequest.RouteStepRequest(37.5700, 126.9800))
        );
        return new CreatePostRequest("오늘 산책 코스 공유해요", "정말 좋았어요", route);
    }

    @Test
    void 추천받은_산책코스를_첨부해서_게시글을_작성할_수_있다() {
        PostDetailResponse response = postService.createPost(requestWithRoute(), authorId);

        assertThat(response.courseName()).isEqualTo("한강 공원 코스");
        assertThat(response.distanceKm()).isEqualTo(3.2);
        assertThat(response.durationMinutes()).isEqualTo(40);
        assertThat(response.route()).hasSize(2);
        assertThat(response.likeCount()).isEqualTo(0);
        assertThat(response.likedByMe()).isFalse();
    }

    @Test
    void 코스없이_일반_게시글도_작성할_수_있다() {
        CreatePostRequest request = new CreatePostRequest("일반 글", "코스 없이 그냥 씁니다", null);

        PostDetailResponse response = postService.createPost(request, authorId);

        assertThat(response.courseName()).isNull();
        assertThat(response.route()).isEmpty();
    }

    @Test
    void 핸드폰_이미지를_게시글에_첨부할_수_있다() {
        PostDetailResponse created = postService.createPost(requestWithRoute(), authorId);
        MockMultipartFile image = new MockMultipartFile("image", "walk.jpg", "image/jpeg", "fake-bytes".getBytes());

        PostDetailResponse withImage = postService.attachImage(created.id(), authorId, image);

        assertThat(withImage.imageUrl()).startsWith("/uploads/posts/");
    }

    @Test
    void 작성자가_아니면_이미지를_첨부할_수_없다() {
        PostDetailResponse created = postService.createPost(requestWithRoute(), authorId);
        MockMultipartFile image = new MockMultipartFile("image", "walk.jpg", "image/jpeg", "fake-bytes".getBytes());

        assertThatThrownBy(() -> postService.attachImage(created.id(), otherUserId, image))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_FORBIDDEN);
    }

    @Test
    void 게시글에_좋아요를_누르고_취소할_수_있다() {
        PostDetailResponse created = postService.createPost(requestWithRoute(), authorId);

        postService.likePost(created.id(), otherUserId);
        PostDetailResponse afterLike = postService.getPost(created.id(), otherUserId);
        assertThat(afterLike.likeCount()).isEqualTo(1);
        assertThat(afterLike.likedByMe()).isTrue();

        postService.unlikePost(created.id(), otherUserId);
        PostDetailResponse afterUnlike = postService.getPost(created.id(), otherUserId);
        assertThat(afterUnlike.likeCount()).isEqualTo(0);
        assertThat(afterUnlike.likedByMe()).isFalse();
    }

    @Test
    void 같은_게시글에_중복으로_좋아요를_누를_수_없다() {
        PostDetailResponse created = postService.createPost(requestWithRoute(), authorId);
        postService.likePost(created.id(), otherUserId);

        assertThatThrownBy(() -> postService.likePost(created.id(), otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_ALREADY_LIKED);
    }

    @Test
    void 좋아요_누르지_않은_게시글은_취소할_수_없다() {
        PostDetailResponse created = postService.createPost(requestWithRoute(), authorId);

        assertThatThrownBy(() -> postService.unlikePost(created.id(), otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_LIKE_NOT_FOUND);
    }

    @Test
    void 내가_좋아요_누른_게시물만_따로_조회할_수_있다() {
        PostDetailResponse liked = postService.createPost(requestWithRoute(), authorId);
        PostDetailResponse notLiked = postService.createPost(new CreatePostRequest("좋아요 안누른 글", "내용", null), authorId);

        postService.likePost(liked.id(), otherUserId);

        Page<PostSummaryResponse> likedPosts = postService.getLikedPosts(otherUserId, PageRequest.of(0, 10));

        assertThat(likedPosts.getContent()).hasSize(1);
        assertThat(likedPosts.getContent().get(0).id()).isEqualTo(liked.id());
        assertThat(likedPosts.getContent()).noneMatch(p -> p.id().equals(notLiked.id()));
    }
}
