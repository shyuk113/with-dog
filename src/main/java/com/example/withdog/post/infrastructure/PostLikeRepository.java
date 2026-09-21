package com.example.withdog.post.infrastructure;

import com.example.withdog.post.domain.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);

    Page<PostLike> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    void deleteByPostId(Long postId);

    //목록 조회용 배치 쿼리: 게시물 N개의 좋아요 수를 한 번에 집계
    @Query("select pl.post.id as postId, count(pl) as likeCount from PostLike pl where pl.post.id in :postIds group by pl.post.id")
    List<LikeCount> countByPostIds(@Param("postIds") Collection<Long> postIds);

    //목록 조회용 배치 쿼리: 게시물 N개 중 내가 좋아요한 게시물 id만 조회
    @Query("select pl.post.id from PostLike pl where pl.user.id = :userId and pl.post.id in :postIds")
    List<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);

    interface LikeCount {
        Long getPostId();
        long getLikeCount();
    }
}
