package com.example.withdog.post.infrastructure;

import com.example.withdog.post.domain.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);

    Page<PostLike> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    void deleteByPostId(Long postId);
}
