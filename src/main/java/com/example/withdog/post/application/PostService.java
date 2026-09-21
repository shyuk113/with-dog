package com.example.withdog.post.application;

import com.example.withdog.comment.domain.Comment;
import com.example.withdog.comment.infrastructure.CommentRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.global.infrastructure.storage.ImageStorageService;
import com.example.withdog.notification.application.NotificationService;
import com.example.withdog.post.application.dto.CreatePostRequest;
import com.example.withdog.post.application.dto.PostDetailResponse;
import com.example.withdog.post.application.dto.PostRouteRequest;
import com.example.withdog.post.application.dto.PostSummaryResponse;
import com.example.withdog.post.application.dto.UpdatePostRequest;
import com.example.withdog.post.domain.Post;
import com.example.withdog.post.domain.PostLike;
import com.example.withdog.post.domain.PostRoutePoint;
import com.example.withdog.post.infrastructure.PostLikeRepository;
import com.example.withdog.post.infrastructure.PostRepository;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final String IMAGE_SUB_DIRECTORY = "posts";

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final ImageStorageService imageStorageService;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    //게시물 작성 (산책 코스 공유는 선택사항)
    @Transactional
    public PostDetailResponse createPost(CreatePostRequest request, Long userId) {
        User author = userRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Post post = request.route() == null
                ? Post.createPost(request.title(), request.content(), author)
                : Post.createPostWithRoute(request.title(), request.content(), author,
                        request.route().courseName(), request.route().distanceKm(), request.route().durationMinutes(),
                        request.route().route().stream().map(r -> new PostRoutePoint(r.lat(), r.lon())).toList());

        postRepository.save(post);
        return PostDetailResponse.of(post, 0, false);
    }

    //게시물 이미지 첨부 (핸드폰에 저장된 이미지 업로드)
    @Transactional
    public PostDetailResponse attachImage(Long postId, Long userId, MultipartFile image) {
        Post post = postRepository.findById(postId).orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if(!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.POST_FORBIDDEN);
        }

        String imageUrl = imageStorageService.store(image, IMAGE_SUB_DIRECTORY);
        post.attachImage(imageUrl);

        long likeCount = postLikeRepository.countByPostId(postId);
        boolean likedByMe = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        return PostDetailResponse.of(post, likeCount, likedByMe);
    }

    //게시물 수정
    @Transactional
    public void updatePost(UpdatePostRequest request, Long userId, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if(!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.POST_FORBIDDEN);
        }
        post.updatePost(request.title(), request.content());
    }

    //게시물 조회
    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));

        long likeCount = postLikeRepository.countByPostId(postId);
        boolean likedByMe = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        return PostDetailResponse.of(post, likeCount, likedByMe);
    }

    //게시물 삭제 -> 댓글/좋아요/알림이 post_id를 참조(FK not-null)하고 있으므로, 게시물을 지우기 전에 먼저 정리해야 한다
    @Transactional
    public void deletePost(Long postId, Long userId) {

        Post post = postRepository.findById(postId).orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if(!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.POST_FORBIDDEN);
        }

        List<Comment> topLevelComments = commentRepository.findByPostIdAndParentIsNull(postId);
        topLevelComments.forEach(notificationService::deleteNotificationsForComment);

        postLikeRepository.deleteByPostId(postId);
        commentRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }

    //게시물 검색
    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getPosts(String keyword, Pageable pageable, Long userId) {
        Page<Post> posts = (keyword == null || keyword.isBlank()) ? postRepository.findAll(pageable) : postRepository.findByTitleContaining(keyword, pageable);
        return toSummaries(posts, userId);
    }

    //게시물 좋아요
    @Transactional
    public void likePost(Long postId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new BusinessException(ErrorCode.POST_ALREADY_LIKED);
        }
        postLikeRepository.save(PostLike.createLike(user, post));
    }

    //게시물 좋아요 취소
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        PostLike postLike = postLikeRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_LIKE_NOT_FOUND));
        postLikeRepository.delete(postLike);
    }

    //내가 좋아요 누른 게시물 목록 조회
    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getLikedPosts(Long userId, Pageable pageable) {
        Page<Post> likedPosts = postLikeRepository.findByUserIdOrderByIdDesc(userId, pageable).map(PostLike::getPost);
        return toSummaries(likedPosts, userId);
    }

    //N+1 방지: 페이지 내 게시물 전체의 좋아요 수/내 좋아요 여부를 IN 쿼리 2번으로 한 번에 조회
    private Page<PostSummaryResponse> toSummaries(Page<Post> posts, Long userId) {
        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        if (postIds.isEmpty()) {
            return posts.map(post -> PostSummaryResponse.of(post, 0, false));
        }

        Map<Long, Long> likeCounts = postLikeRepository.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(PostLikeRepository.LikeCount::getPostId, PostLikeRepository.LikeCount::getLikeCount));
        Set<Long> likedPostIds = new HashSet<>(postLikeRepository.findLikedPostIds(userId, postIds));

        return posts.map(post -> PostSummaryResponse.of(
                post, likeCounts.getOrDefault(post.getId(), 0L), likedPostIds.contains(post.getId())));
    }
}
