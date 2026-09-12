package com.example.withdog.comment.application;

import com.example.withdog.comment.application.dto.CommentResponse;
import com.example.withdog.comment.application.dto.CreateCommentRequest;
import com.example.withdog.comment.application.dto.UpdateCommentRequest;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.notification.application.NotificationService;
import com.example.withdog.notification.application.dto.NotificationResponse;
import com.example.withdog.notification.domain.NotificationType;
import com.example.withdog.post.domain.Post;
import com.example.withdog.post.infrastructure.PostRepository;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private Long postAuthorId;
    private Long commenterId;
    private Long replierId;
    private Long postId;

    @BeforeEach
    void setUp() {
        User postAuthor = userRepository.save(User.createUserLocal("글쓴이", "author@example.com", "encoded-password", "서울"));
        User commenter = userRepository.save(User.createUserLocal("댓글러", "commenter@example.com", "encoded-password", "서울"));
        User replier = userRepository.save(User.createUserLocal("대댓글러", "replier@example.com", "encoded-password", "서울"));

        Post post = postRepository.save(Post.createPost("오늘 산책 후기", "정말 좋았어요", postAuthor));

        postAuthorId = postAuthor.getId();
        commenterId = commenter.getId();
        replierId = replier.getId();
        postId = post.getId();
    }

    @Test
    void 댓글을_작성할_수_있다() {
        CommentResponse comment = commentService.createComment(new CreateCommentRequest("좋은 코스네요", null), postId, commenterId);

        assertThat(comment.content()).isEqualTo("좋은 코스네요");
        assertThat(comment.userId()).isEqualTo(commenterId);
        assertThat(comment.parentId()).isNull();
    }

    @Test
    void 댓글을_작성하면_게시물_작성자에게_알림이_간다() {
        commentService.createComment(new CreateCommentRequest("좋은 코스네요", null), postId, commenterId);

        Page<NotificationResponse> notifications = notificationService.getNotifications(postAuthorId, PageRequest.of(0, 10));

        assertThat(notifications.getContent()).hasSize(1);
        assertThat(notifications.getContent().get(0).type()).isEqualTo(NotificationType.NEW_COMMENT);
    }

    @Test
    void 자기_글에_자기가_댓글을_달면_알림이_가지_않는다() {
        commentService.createComment(new CreateCommentRequest("셀프 댓글", null), postId, postAuthorId);

        Page<NotificationResponse> notifications = notificationService.getNotifications(postAuthorId, PageRequest.of(0, 10));
        assertThat(notifications.getContent()).isEmpty();
    }

    @Test
    void 대댓글을_작성할_수_있다() {
        CommentResponse parent = commentService.createComment(new CreateCommentRequest("원본 댓글", null), postId, commenterId);

        CommentResponse reply = commentService.createComment(new CreateCommentRequest("답글입니다", parent.id()), postId, replierId);

        assertThat(reply.parentId()).isEqualTo(parent.id());
    }

    @Test
    void 대댓글은_게시물_작성자가_아니라_원본_댓글_작성자에게만_알림이_간다() {
        CommentResponse parent = commentService.createComment(new CreateCommentRequest("원본 댓글", null), postId, commenterId);
        // 게시물 작성자에게 온 "댓글" 알림은 리셋하고 확인하기 위해 그대로 둔 채, 대댓글 알림만 따로 검증

        commentService.createComment(new CreateCommentRequest("답글입니다", parent.id()), postId, replierId);

        Page<NotificationResponse> postAuthorNotifications = notificationService.getNotifications(postAuthorId, PageRequest.of(0, 10));
        Page<NotificationResponse> commenterNotifications = notificationService.getNotifications(commenterId, PageRequest.of(0, 10));

        // 게시물 작성자는 최초 댓글 알림 1개만 있고, 대댓글에 대한 알림은 없어야 한다
        assertThat(postAuthorNotifications.getContent()).hasSize(1);
        assertThat(postAuthorNotifications.getContent().get(0).type()).isEqualTo(NotificationType.NEW_COMMENT);

        // 원본 댓글 작성자에게는 대댓글 알림이 가야 한다
        assertThat(commenterNotifications.getContent()).hasSize(1);
        assertThat(commenterNotifications.getContent().get(0).type()).isEqualTo(NotificationType.NEW_REPLY);
    }

    @Test
    void 자기_댓글에_자기가_대댓글을_달면_알림이_가지_않는다() {
        CommentResponse parent = commentService.createComment(new CreateCommentRequest("원본 댓글", null), postId, commenterId);

        commentService.createComment(new CreateCommentRequest("셀프 답글", parent.id()), postId, commenterId);

        Page<NotificationResponse> commenterNotifications = notificationService.getNotifications(commenterId, PageRequest.of(0, 10));
        assertThat(commenterNotifications.getContent()).isEmpty();
    }

    @Test
    void 대댓글에는_다시_답글을_달_수_없다() {
        CommentResponse parent = commentService.createComment(new CreateCommentRequest("원본 댓글", null), postId, commenterId);
        CommentResponse reply = commentService.createComment(new CreateCommentRequest("답글", parent.id()), postId, replierId);

        assertThatThrownBy(() -> commentService.createComment(new CreateCommentRequest("답글의 답글", reply.id()), postId, postAuthorId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REPLY_DEPTH_EXCEEDED);
    }

    @Test
    void 자신의_댓글을_수정할_수_있다() {
        CommentResponse comment = commentService.createComment(new CreateCommentRequest("원본 내용", null), postId, commenterId);

        commentService.updateComment(new UpdateCommentRequest("수정된 내용"), comment.id(), postId, commenterId);

        Page<CommentResponse> comments = commentService.getComments(PageRequest.of(0, 10), postId);
        assertThat(comments.getContent().get(0).content()).isEqualTo("수정된 내용");
    }

    @Test
    void 타인의_댓글은_수정할_수_없다() {
        CommentResponse comment = commentService.createComment(new CreateCommentRequest("원본 내용", null), postId, commenterId);

        assertThatThrownBy(() -> commentService.updateComment(new UpdateCommentRequest("남의 댓글 수정 시도"), comment.id(), postId, replierId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_FORBIDDEN);
    }

    @Test
    void 자신의_댓글을_삭제할_수_있다() {
        CommentResponse comment = commentService.createComment(new CreateCommentRequest("삭제될 댓글", null), postId, commenterId);

        commentService.deleteComment(comment.id(), postId, commenterId);

        Page<CommentResponse> comments = commentService.getComments(PageRequest.of(0, 10), postId);
        assertThat(comments.getContent()).isEmpty();
    }

    @Test
    void 타인의_댓글은_삭제할_수_없다() {
        CommentResponse comment = commentService.createComment(new CreateCommentRequest("삭제 시도 당할 댓글", null), postId, commenterId);

        assertThatThrownBy(() -> commentService.deleteComment(comment.id(), postId, replierId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_FORBIDDEN);
    }
}
