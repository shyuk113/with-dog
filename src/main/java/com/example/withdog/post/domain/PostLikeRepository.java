package com.example.withdog.post.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

//PostLike 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 PostLikeRepositoryAdapter가 담당한다
public interface PostLikeRepository {

    PostLike save(PostLike postLike);

    void delete(PostLike postLike);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);

    Page<PostLike> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    void deleteByPostId(Long postId);

    //목록 조회용 배치 쿼리: 게시물 N개의 좋아요 수를 한 번에 집계
    List<LikeCount> countByPostIds(Collection<Long> postIds);

    //목록 조회용 배치 쿼리: 게시물 N개 중 내가 좋아요한 게시물 id만 조회
    List<Long> findLikedPostIds(Long userId, Collection<Long> postIds);

    //게시물별 좋아요 수 집계 결과
    interface LikeCount {
        Long getPostId();

        long getLikeCount();
    }
}
