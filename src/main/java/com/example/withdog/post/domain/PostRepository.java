package com.example.withdog.post.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

//Post 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 PostRepositoryAdapter가 담당한다
public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(Long id);

    void delete(Post post);

    Page<Post> findAll(Pageable pageable);

    List<Post> findAllById(Collection<Long> ids);

    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    //동네 피드 DB fallback: 커서(id < cursor) 기반 최신순 id 조회
    List<Long> findIdsByRegionCode(String regionCode, Long cursor, Pageable pageable);
}
