package com.example.withdog.user.domain;

import java.util.Optional;

//User 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 UserRepositoryAdapter가 담당한다
public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    //비관적 쓰기 락(SELECT ... FOR UPDATE)으로 조회: 같은 사용자의 동시 요청을 직렬화할 때 사용
    Optional<User> findByIdForUpdate(Long id);
}
