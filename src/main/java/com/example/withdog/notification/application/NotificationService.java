package com.example.withdog.notification.application;

import com.example.withdog.comment.domain.Comment;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.notification.application.dto.NotificationResponse;
import com.example.withdog.notification.domain.Notification;
import com.example.withdog.notification.domain.NotificationType;
import com.example.withdog.notification.infrastructure.NotificationRepository;
import com.example.withdog.post.domain.Post;
import com.example.withdog.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    //댓글 작성 -> 게시물 작성자에게 알림 (본인 글에 본인이 댓글 단 경우는 생략)
    @Transactional
    public void notifyNewComment(Post post, Comment comment) {
        User postAuthor = post.getAuthor();
        if (postAuthor.getId().equals(comment.getUser().getId())) {
            return;
        }
        String message = comment.getUser().getNickname() + "님이 회원님의 게시글에 댓글을 남겼습니다.";
        notificationRepository.save(Notification.createFor(postAuthor, NotificationType.NEW_COMMENT, message, post, comment));
    }

    //대댓글 작성 -> 게시물 작성자가 아니라 원본 댓글 작성자에게 알림 (본인 댓글에 본인이 대댓글 단 경우는 생략)
    @Transactional
    public void notifyNewReply(Comment parentComment, Comment reply) {
        User parentAuthor = parentComment.getUser();
        if (parentAuthor.getId().equals(reply.getUser().getId())) {
            return;
        }
        String message = reply.getUser().getNickname() + "님이 회원님의 댓글에 답글을 남겼습니다.";
        notificationRepository.save(Notification.createFor(parentAuthor, NotificationType.NEW_REPLY, message, reply.getPost(), reply));
    }

    //내 알림 목록 조회
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByIdDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    //알림 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markAsRead();
    }

    //댓글(및 그 대댓글) 삭제 전, 참조하고 있던 알림을 함께 정리
    @Transactional
    public void deleteNotificationsForComment(Comment comment) {
        notificationRepository.deleteByCommentId(comment.getId());
        for (Comment reply : comment.getReplies()) {
            notificationRepository.deleteByCommentId(reply.getId());
        }
    }
}
