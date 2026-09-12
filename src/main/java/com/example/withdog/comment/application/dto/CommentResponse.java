package com.example.withdog.comment.application.dto;

import com.example.withdog.comment.domain.Comment;

public record CommentResponse(Long id, String content, Long userId, Long parentId) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getContent(), comment.getUser().getId(), comment.getParentId());
    }
}
