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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    //게시물 작성 (산책 코스 공유는 선택사항)
    @PostMapping
    public ResponseEntity<PostDetailResponse> createPost(@Valid @RequestBody CreatePostRequest request, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request, userId));
    }

    //게시물 이미지 첨부 (핸드폰에 저장된 이미지 업로드)
    @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
    public ResponseEntity<PostDetailResponse> attachImage(@PathVariable Long id,
                                                            @RequestParam("image") MultipartFile image,
                                                            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(postService.attachImage(id, userId, image));
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
        return ResponseEntity.ok(postService.getPost(id, userId));
    }

    //게시물 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    //게시물 검색
    @GetMapping
    public ResponseEntity<Page<PostSummaryResponse>> getPosts(@PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                               @RequestParam(required = false) String keyword,
                                                               @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(postService.getPosts(keyword, pageable, userId));
    }

    //내가 좋아요 누른 게시물 목록 조회
    @GetMapping("/liked")
    public ResponseEntity<Page<PostSummaryResponse>> getLikedPosts(@PageableDefault(size = 15) Pageable pageable, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(postService.getLikedPosts(userId, pageable));
    }

    //게시물 좋아요
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        postService.likePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    //게시물 좋아요 취소
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        postService.unlikePost(id, userId);
        return ResponseEntity.noContent().build();
    }
}
