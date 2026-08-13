package com.example.withdog.dog.application;

import com.example.withdog.dog.application.dto.CreateDogRequest;
import com.example.withdog.dog.application.dto.DogResponse;
import com.example.withdog.dog.application.dto.UpdateDogRequest;
import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class DogService {

    private final DogRepository dogRepository;
    private final UserRepository userRepository;

    //강아지 정보 생성
    @Transactional
    public DogResponse createDog(CreateDogRequest request, Long userId){

        User user = userRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Dog dog = Dog.createDogProfile(request.name(), request.breed(), request.birthDate(), request.weight(), user);
        dogRepository.save(dog);
        return DogResponse.from(dog);
    }

    //강아지 정보 조회
    @Transactional(readOnly = true)
    public DogResponse getDog(Long id, Long userId){
        Dog dog = dogRepository.findByUserIdAndId(userId, id).orElseThrow(()-> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        return DogResponse.from(dog);
    }

    //강아지 정보 수정
    @Transactional
    public void updateDog(Long id, UpdateDogRequest request, Long userId){
        Dog dog =  dogRepository.findByUserIdAndId(userId, id).orElseThrow(()-> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        dog.updateDogProfile(request.name(), request.breed(), request.birthDate(), request.weight());
    }

    //강아지 정보 삭제
    @Transactional
    public void deleteDog(Long id, Long userId){
        Dog dog = dogRepository.findByUserIdAndId(userId, id).orElseThrow(()-> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        dogRepository.delete(dog);
    }
}
