package com.example.withdog.post.application;

import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.post.application.dto.CreatePostRequest;
import com.example.withdog.post.application.dto.PostDetailResponse;
import com.example.withdog.post.application.dto.PostSummaryResponse;
import com.example.withdog.post.application.dto.UpdatePostRequest;
import com.example.withdog.post.domain.Post;
import com.example.withdog.post.infrastructure.PostRepository;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    //게시물 작성
    @Transactional
    public PostDetailResponse createPost(CreatePostRequest request, Long userId) {
        User author = userRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Post post = Post.createPost(request.title(), request.content(), author);
        postRepository.save(post);
        return PostDetailResponse.from(post);
    }

    //게시물 수정
    @Transactional
    public void updatePost(UpdatePostRequest request, Long userId, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if(!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.POST_FORBIDDEN);
        }
        post.updatePost(request.title(), request.content());
    }

    //게시물 조회
    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long postId) {

        Post post = postRepository.findById(postId).orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));

        return PostDetailResponse.from(post);
    }

    //게시물 삭제
    @Transactional
    public void deletePost(Long postId, Long userId) {

        Post post = postRepository.findById(postId).orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if(!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.POST_FORBIDDEN);
        }
        postRepository.delete(post);
    }

    //게시물 검색
    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getPosts(String keyword, Pageable pageable) {
        Page<Post> posts = (keyword == null || keyword.isBlank()) ? postRepository.findAll(pageable) : postRepository.findByTitleContaining(keyword, pageable);
        return posts.map(PostSummaryResponse::from);
    }
}
