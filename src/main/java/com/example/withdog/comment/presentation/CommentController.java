package com.example.withdog.comment.presentation;

import com.example.withdog.comment.application.CommentService;
import com.example.withdog.comment.application.dto.CommentResponse;
import com.example.withdog.comment.application.dto.CreateCommentRequest;
import com.example.withdog.comment.application.dto.UpdateCommentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    //댓글 조회
    @GetMapping
    public ResponseEntity<Page<CommentResponse>> getComments(@PathVariable Long postId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok().body(commentService.getComments(pageable, postId));
    }

    //댓글 생성
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long postId, @Valid @RequestBody CreateCommentRequest request, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(request, postId, userId));
    }

    //댓글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, @PathVariable Long postId, @AuthenticationPrincipal Long userId) {
        commentService.deleteComment(id, postId, userId);
        return ResponseEntity.noContent().build();
    }

    //댓글 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateComment(@PathVariable Long id, @PathVariable Long postId
            ,@Valid @RequestBody UpdateCommentRequest request, @AuthenticationPrincipal Long userId) {
        commentService.updateComment(request, id, postId, userId);
        return  ResponseEntity.noContent().build();
    }

}
