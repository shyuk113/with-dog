package com.example.withdog.post.presentation;

import com.example.withdog.post.application.PostService;
import com.example.withdog.post.application.dto.CreatePostRequest;
import com.example.withdog.post.application.dto.PostDetailResponse;
import com.example.withdog.post.application.dto.PostSummaryResponse;
import com.example.withdog.post.application.dto.UpdatePostRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    //게시물 작성
    @PostMapping
    public ResponseEntity<PostDetailResponse> createPost(@Valid @RequestBody CreatePostRequest request, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request, userId));
    }

    //게시물 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePost(@PathVariable Long id, @Valid @RequestBody UpdatePostRequest request, @AuthenticationPrincipal Long userId) {
        postService.updatePost(request, userId, id);
        return ResponseEntity.noContent().build();
    }

    //게시물 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> getPost(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    //게시물 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    //게시물 검색
    @GetMapping
    public ResponseEntity<Page<PostSummaryResponse>> getPosts(@PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(postService.getPosts(keyword, pageable));
    }
}
