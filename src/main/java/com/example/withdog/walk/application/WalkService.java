package com.example.withdog.walk.application;

import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.mission.application.MissionService;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import com.example.withdog.walk.application.dto.CreateWalkRequest;
import com.example.withdog.walk.application.dto.UpdateWalkRequest;
import com.example.withdog.walk.application.dto.WalkDetailResponse;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalkService {

    private final WalkRepository walkRepository;
    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final RoutePointRepository routePointRepository;
    private final MissionService missionService;

    //히스토리 목록 조회
    @Transactional(readOnly = true)
    public Page<WalkResponse> getWalkHistories(Pageable pageable, Long userId){
        Page<Walk> walkHistories = walkRepository.findByUserId(pageable, userId);
        return walkHistories.map(WalkResponse::from);
    }

    //히스토리 상세 조회 (소요시간, 시작/종료 시간, 거리, 코스, 강아지 프로필)
    @Transactional(readOnly = true)
    public WalkDetailResponse getWalkHistory(Long id, Long userId){
        Walk walkHistory = walkRepository.findByIdAndUserId(id, userId).orElseThrow(()->
                new BusinessException(ErrorCode.WALKHISTORY_NOT_FOUND));
        return WalkDetailResponse.of(walkHistory, routePointRepository.findByWalkId(walkHistory.getId()));
    }

    //산책 시작
    @Transactional
    public WalkResponse createWalk(Long userId, CreateWalkRequest request){
        // user row를 잠궈서 같은 유저의 동시 createWalk 호출을 직렬화 (진행중 산책 체크-후-생성 레이스 방지)
        User user = userRepository.findByIdForUpdate(userId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if(walkRepository.findByUserIdAndEndedAtIsNull(userId).isPresent()){
            throw new BusinessException(ErrorCode.WALK_ALREADY_ONGOING);
        }
        Dog dog = dogRepository.findByUserIdAndId(userId, request.dogId()).orElseThrow(()-> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        Walk walk = Walk.createWalk(user, dog, LocalDateTime.now());
        walkRepository.save(walk);
        return WalkResponse.from(walk);
    }

    //산책 종료 및 저장 -> 종료 직후 화면에 바로 띄울 상세 정보 반환
    @Transactional
    public WalkDetailResponse updateWalk(Long userId, Long id, UpdateWalkRequest request){
        Walk walk = walkRepository.findByIdAndUserId(id, userId).orElseThrow(()->new BusinessException(ErrorCode.WALKHISTORY_NOT_FOUND));
        List<RoutePoint> routePoints = routePointRepository.saveAll(request.routePointRequest().stream()
                .map(r-> RoutePoint.createRoutePoint(r.lat(), r.lon(), r.capturedAt(), walk)).toList());
        walk.end(LocalDateTime.now(), request.distanceKm());

        //산책 종료로 이번 주 산책 관련 미션 달성 여부가 바뀔 수 있어 재평가
        missionService.evaluate(userId);

        return WalkDetailResponse.of(walk, routePoints);
    }

    //진행중인 산책이 있는지 확인
    @Transactional(readOnly = true)
    public Optional<WalkResponse> getOngoingWalk(Long userId){
        return walkRepository.findByUserIdAndEndedAtIsNull(userId).map(WalkResponse::from);
    }
}
