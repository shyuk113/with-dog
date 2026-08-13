package com.example.withdog.post.domain;

import com.example.withdog.global.BaseEntity;
import com.example.withdog.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Builder
    private Post(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public static Post createPost(String title, String content, User author) {
        return Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();
    }

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
