package com.example.withdog.post.infrastructure;

import com.example.withdog.post.domain.PostLike;
import com.example.withdog.post.domain.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

//도메인 포트 PostLikeRepository의 Spring Data JPA 구현 (저장 기술이 바뀌면 이 어댑터만 교체한다)
@Repository
@RequiredArgsConstructor
public class PostLikeRepositoryAdapter implements PostLikeRepository {

    private final PostLikeJpaRepository postLikeJpaRepository;

    @Override
    public PostLike save(PostLike postLike) {
        return postLikeJpaRepository.save(postLike);
    }

    @Override
    public void delete(PostLike postLike) {
        postLikeJpaRepository.delete(postLike);
    }

    @Override
    public boolean existsByUserIdAndPostId(Long userId, Long postId) {
        return postLikeJpaRepository.existsByUserIdAndPostId(userId, postId);
    }

    @Override
    public Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId) {
        return postLikeJpaRepository.findByUserIdAndPostId(userId, postId);
    }

    @Override
    public long countByPostId(Long postId) {
        return postLikeJpaRepository.countByPostId(postId);
    }

    @Override
    public Page<PostLike> findByUserIdOrderByIdDesc(Long userId, Pageable pageable) {
        return postLikeJpaRepository.findByUserIdOrderByIdDesc(userId, pageable);
    }

    @Override
    public void deleteByPostId(Long postId) {
        postLikeJpaRepository.deleteByPostId(postId);
    }

    @Override
    public List<PostLikeRepository.LikeCount> countByPostIds(Collection<Long> postIds) {
        return postLikeJpaRepository.countByPostIds(postIds);
    }

    @Override
    public List<Long> findLikedPostIds(Long userId, Collection<Long> postIds) {
        return postLikeJpaRepository.findLikedPostIds(userId, postIds);
    }
}
