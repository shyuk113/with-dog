package com.example.withdog.comment.infrastructure;

import com.example.withdog.comment.domain.Comment;
import com.example.withdog.comment.domain.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//도메인 포트 CommentRepository의 Spring Data JPA 구현 (저장 기술이 바뀌면 이 어댑터만 교체한다)
@Repository
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepository {

    private final CommentJpaRepository commentJpaRepository;

    @Override
    public Comment save(Comment comment) {
        return commentJpaRepository.save(comment);
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return commentJpaRepository.findById(id);
    }

    @Override
    public void delete(Comment comment) {
        commentJpaRepository.delete(comment);
    }

    @Override
    public Page<Comment> findByPostId(Pageable pageable, Long postId) {
        return commentJpaRepository.findByPostId(pageable, postId);
    }

    @Override
    public Optional<Comment> findByIdAndPostId(Long id, Long postId) {
        return commentJpaRepository.findByIdAndPostId(id, postId);
    }

    @Override
    public List<Comment> findByPostIdAndParentIsNull(Long postId) {
        return commentJpaRepository.findByPostIdAndParentIsNull(postId);
    }

    @Override
    public void deleteByPostId(Long postId) {
        commentJpaRepository.deleteByPostId(postId);
    }
}
