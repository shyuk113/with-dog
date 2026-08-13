package com.example.withdog.post.infrastructure;

import com.example.withdog.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByUserIdAndId(Long userId, Long id);
}
