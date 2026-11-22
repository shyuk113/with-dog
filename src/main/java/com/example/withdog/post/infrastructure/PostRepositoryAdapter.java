package com.example.withdog.post.infrastructure;

import com.example.withdog.post.domain.Post;
import com.example.withdog.post.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

//도메인 포트 PostRepository의 Spring Data JPA 구현 (저장 기술이 바뀌면 이 어댑터만 교체한다)
@Repository
@RequiredArgsConstructor
public class PostRepositoryAdapter implements PostRepository {

    private final PostJpaRepository postJpaRepository;

    @Override
    public Post save(Post post) {
        return postJpaRepository.save(post);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return postJpaRepository.findById(id);
    }

    @Override
    public void delete(Post post) {
        postJpaRepository.delete(post);
    }

    @Override
    public Page<Post> findAll(Pageable pageable) {
        return postJpaRepository.findAll(pageable);
    }

    @Override
    public List<Post> findAllById(Collection<Long> ids) {
        return postJpaRepository.findAllById(ids);
    }

    @Override
    public Page<Post> findByTitleContaining(String keyword, Pageable pageable) {
        return postJpaRepository.findByTitleContaining(keyword, pageable);
    }

    @Override
    public List<Long> findIdsByRegionCode(String regionCode, Long cursor, Pageable pageable) {
        return postJpaRepository.findIdsByRegionCode(regionCode, cursor, pageable);
    }
}
