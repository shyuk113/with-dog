package com.example.withdog.comment.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

//Comment 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 CommentRepositoryAdapter가 담당한다
public interface CommentRepository {

    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    void delete(Comment comment);

    Page<Comment> findByPostId(Pageable pageable, Long postId);

    Optional<Comment> findByIdAndPostId(Long id, Long postId);

    List<Comment> findByPostIdAndParentIsNull(Long postId);

    void deleteByPostId(Long postId);
}
