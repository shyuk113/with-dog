package com.example.withdog.notification.infrastructure;

import com.example.withdog.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//Spring Data JPA 구현 기술. 서비스는 이 인터페이스가 아니라 domain의 NotificationRepository에 의존한다
public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByIdDesc(Long recipientId, Pageable pageable);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    void deleteByCommentId(Long commentId);
}
