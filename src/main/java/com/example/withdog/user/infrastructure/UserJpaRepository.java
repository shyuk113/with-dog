package com.example.withdog.user.infrastructure;

import com.example.withdog.user.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

//Spring Data JPA 구현 기술. 서비스는 이 인터페이스가 아니라 domain의 UserRepository에 의존한다
public interface UserJpaRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(Long id);
}
