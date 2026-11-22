package com.example.withdog.comment.infrastructure;

import com.example.withdog.comment.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//Spring Data JPA 구현 기술. 서비스는 이 인터페이스가 아니라 domain의 CommentRepository에 의존한다
public interface CommentJpaRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostId(Pageable pageable, Long postId);

    Optional<Comment> findByIdAndPostId(Long id, Long postId);

    List<Comment> findByPostIdAndParentIsNull(Long postId);

    void deleteByPostId(Long postId);

}
