package com.example.withdog.walk.application;

import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import com.example.withdog.walk.application.dto.CreateWalkRequest;
import com.example.withdog.walk.application.dto.UpdateWalkRequest;
import com.example.withdog.walk.application.dto.WalkResponse;
import com.example.withdog.walk.domain.RoutePoint;
import com.example.withdog.walk.domain.Walk;
import com.example.withdog.walk.infrastructure.RoutePointRepository;
import com.example.withdog.walk.infrastructure.WalkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalkService {

    private final WalkRepository walkRepository;
    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final RoutePointRepository routePointRepository;

    //히스토리 목록 조회
    @Transactional(readOnly = true)
    public Page<WalkResponse> getWalkHistories(Pageable pageable, Long userId){
        Page<Walk> walkHistories = walkRepository.findByUserId(pageable, userId);
        return walkHistories.map(WalkResponse::from);
    }

    //히스토리 상세 조회
    @Transactional(readOnly = true)
    public WalkResponse getWalkHistory(Long id, Long userId){
        Walk walkHistory = walkRepository.findByIdAndUserId(id, userId).orElseThrow(()->
                new BusinessException(ErrorCode.WALKHISTORY_NOT_FOUND));
        return WalkResponse.from(walkHistory);
    }

    //산책 시작
    @Transactional
    public WalkResponse createWalk(Long userId, CreateWalkRequest request){
        User user = userRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Dog dog = dogRepository.findByUserIdAndId(userId, request.dogId()).orElseThrow(()-> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        Walk walk = Walk.createWalk(user, dog, LocalDateTime.now());
        walkRepository.save(walk);
        return WalkResponse.from(walk);
    }

    //산책 종료 및 저장
    @Transactional
    public void updateWalk(Long userId, Long id, UpdateWalkRequest request){
        Walk walk = walkRepository.findByIdAndUserId(id, userId).orElseThrow(()->new BusinessException(ErrorCode.WALKHISTORY_NOT_FOUND));
        routePointRepository.saveAll(request.routePointRequest().stream()
                .map(r-> RoutePoint.createRoutePoint(r.lat(), r.lon(), r.capturedAt(), walk)).toList());
        walk.end(LocalDateTime.now(), request.distanceKm());
    }
}
