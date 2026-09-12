package com.example.withdog.notification.domain;

import com.example.withdog.comment.domain.Comment;
import com.example.withdog.global.BaseEntity;
import com.example.withdog.post.domain.Post;
import com.example.withdog.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림을 받는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    private boolean read;

    @Builder
    private Notification(User recipient, NotificationType type, String message, Post post, Comment comment) {
        this.recipient = recipient;
        this.type = type;
        this.message = message;
        this.post = post;
        this.comment = comment;
        this.read = false;
    }

    public static Notification createFor(User recipient, NotificationType type, String message, Post post, Comment comment) {
        return Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .post(post)
                .comment(comment)
                .build();
    }

    public void markAsRead() {
        this.read = true;
    }
}
