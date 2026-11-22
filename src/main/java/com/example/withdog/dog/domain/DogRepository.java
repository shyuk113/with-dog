package com.example.withdog.dog.domain;

import java.util.List;
import java.util.Optional;

//Dog 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 DogRepositoryAdapter가 담당한다
public interface DogRepository {

    Dog save(Dog dog);

    void delete(Dog dog);

    //소유권을 조회 조건에 포함 (남의 강아지는 조회되지 않음)
    Optional<Dog> findByUserIdAndId(Long userId, Long id);

    List<Dog> findAllByUserId(Long userId);
}
