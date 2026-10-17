package com.example.withdog.post.application;

import com.example.withdog.post.application.dto.PostSummaryResponse;
import com.example.withdog.post.domain.Post;
import com.example.withdog.post.infrastructure.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

//게시글 목록 응답 조립 (게시글 목록 / 좋아요한 글 / 동네 피드 공통)
@Component
@RequiredArgsConstructor
public class PostSummaryAssembler {

    private final PostLikeRepository postLikeRepository;

    //N+1 방지: 게시글 전체의 좋아요 수/내 좋아요 여부를 IN 쿼리 2번으로 한 번에 조회 (입력 순서 유지)
    public List<PostSummaryResponse> assemble(List<Post> posts, Long userId) {
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        if (postIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> likeCounts = postLikeRepository.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(PostLikeRepository.LikeCount::getPostId, PostLikeRepository.LikeCount::getLikeCount));
        Set<Long> likedPostIds = new HashSet<>(postLikeRepository.findLikedPostIds(userId, postIds));

        return posts.stream()
                .map(post -> PostSummaryResponse.of(
                        post, likeCounts.getOrDefault(post.getId(), 0L), likedPostIds.contains(post.getId())))
                .toList();
    }

    public Page<PostSummaryResponse> assemble(Page<Post> posts, Long userId) {
        return new PageImpl<>(assemble(posts.getContent(), userId), posts.getPageable(), posts.getTotalElements());
    }
}
