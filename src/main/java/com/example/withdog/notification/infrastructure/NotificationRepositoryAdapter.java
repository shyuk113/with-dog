package com.example.withdog.notification.infrastructure;

import com.example.withdog.notification.domain.Notification;
import com.example.withdog.notification.domain.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//도메인 포트 NotificationRepository의 Spring Data JPA 구현 (저장 기술이 바뀌면 이 어댑터만 교체한다)
@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public Notification save(Notification notification) {
        return notificationJpaRepository.save(notification);
    }

    @Override
    public Page<Notification> findByRecipientIdOrderByIdDesc(Long recipientId, Pageable pageable) {
        return notificationJpaRepository.findByRecipientIdOrderByIdDesc(recipientId, pageable);
    }

    @Override
    public Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId) {
        return notificationJpaRepository.findByIdAndRecipientId(id, recipientId);
    }

    @Override
    public void deleteByCommentId(Long commentId) {
        notificationJpaRepository.deleteByCommentId(commentId);
    }
}
