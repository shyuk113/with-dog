package com.example.withdog.comment.application;

import com.example.withdog.comment.application.dto.CommentResponse;
import com.example.withdog.comment.application.dto.CreateCommentRequest;
import com.example.withdog.comment.application.dto.UpdateCommentRequest;
import com.example.withdog.comment.domain.Comment;
import com.example.withdog.comment.infrastructure.CommentRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.notification.application.NotificationService;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    //댓글 조회
    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Pageable pageable, Long postId) {
        Page<Comment> comments = commentRepository.findByPostId(pageable, postId);
        return comments.map(CommentResponse::from);
    }

    //댓글/대댓글 생성
    @Transactional
    public CommentResponse createComment(CreateCommentRequest request, Long postId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (request.parentId() == null) {
            Comment comment = Comment.createComment(request.content(), user, post);
            commentRepository.save(comment);
            //댓글 작성 -> 게시물 작성자에게 알림
            notificationService.notifyNewComment(post, comment);
            return CommentResponse.from(comment);
        }

        Comment parent = commentRepository.findByIdAndPostId(request.parentId(), postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        if (parent.isReply()) {
            throw new BusinessException(ErrorCode.REPLY_DEPTH_EXCEEDED);
        }

        Comment reply = Comment.createReply(request.content(), user, post, parent);
        commentRepository.save(reply);
        //대댓글 작성 -> 게시물 작성자가 아니라 원본 댓글 작성자에게 알림
        notificationService.notifyNewReply(parent, reply);
        return CommentResponse.from(reply);
    }

    //댓글 수정
    @Transactional
    public void updateComment(UpdateCommentRequest request, Long commentId, Long postId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if(!userId.equals(comment.getUser().getId())){
            throw new BusinessException(ErrorCode.COMMENT_FORBIDDEN);
        }

        if(!postId.equals(comment.getPost().getId())){
            throw new BusinessException(ErrorCode.COMMENT_FORBIDDEN);
        }

        comment.updateComment(request.content());
    }


    //댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long postId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if(!userId.equals(comment.getUser().getId())){
            throw new BusinessException(ErrorCode.COMMENT_FORBIDDEN);
        }

        if(!postId.equals(comment.getPost().getId())){
            throw new BusinessException(ErrorCode.COMMENT_FORBIDDEN);
        }

        notificationService.deleteNotificationsForComment(comment);
        commentRepository.delete(comment);
    }


}
