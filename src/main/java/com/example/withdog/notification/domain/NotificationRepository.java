package com.example.withdog.notification.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

//Notification 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 NotificationRepositoryAdapter가 담당한다
public interface NotificationRepository {

    Notification save(Notification notification);

    Page<Notification> findByRecipientIdOrderByIdDesc(Long recipientId, Pageable pageable);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    void deleteByCommentId(Long commentId);
}
