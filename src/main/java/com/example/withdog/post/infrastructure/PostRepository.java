package com.example.withdog.post.infrastructure;

import com.example.withdog.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    //동네 피드 DB fallback: (region_code, id) 인덱스를 타는 커서 기반 조회 (id < cursor, 최신순)
    @Query("select p.id from Post p where p.regionCode = :regionCode and p.id < :cursor order by p.id desc")
    List<Long> findIdsByRegionCode(@Param("regionCode") String regionCode, @Param("cursor") Long cursor, Pageable pageable);
}
