package com.example.withdog.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ── Auth ──────────────────────────────────────────
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_OAUTH_STATE(HttpStatus.BAD_REQUEST, "유효하지 않은 로그인 요청입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),

    // ── User ──────────────────────────────────────────
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "본인의 정보만 접근할 수 있습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    //── Dog ──────────────────────────────────────────
    DOG_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 강아지입니다."),

    //── Post ──────────────────────────────────────────
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    POST_FORBIDDEN(HttpStatus.FORBIDDEN, "본인이 작성한 게시글만 수정/삭제할 수 있습니다."),
    POST_ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요를 누른 게시글입니다."),
    POST_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요를 누르지 않은 게시글입니다."),

    //── Comment──────────────────────────────────────────
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "본인이 작성한 댓글만 수정/삭제할 수 있습니다."),
    REPLY_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "대댓글에는 답글을 달 수 없습니다."),

    // AI
    AI_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "추천 서버 응답에 실패했습니다."),
    LOCATION_REQUIRED(HttpStatus.BAD_REQUEST, "등록된 위치 정보가 없습니다. 위치를 직접 입력하거나 프로필에서 위치를 설정해주세요."),

    //walkHistory
    WALKHISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 산책 이력입니다."),
    WALK_ALREADY_ONGOING(HttpStatus.CONFLICT, "이미 산책 중 입니다."),

    //── Medication ──────────────────────────────────────────
    MEDICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 복용 약 기록입니다."),
    INVALID_IMAGE(HttpStatus.BAD_REQUEST, "이미지 파일이 필요합니다."),
    IMAGE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 저장에 실패했습니다."),
    OCR_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "약 봉투 인식에 실패했습니다."),

    //── Notification ──────────────────────────────────────────
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),

    //── Mission ──────────────────────────────────────────
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 미션입니다."),
    MISSION_NOT_ACHIEVED(HttpStatus.CONFLICT, "아직 달성하지 않은 미션입니다."),
    MISSION_ALREADY_CLAIMED(HttpStatus.CONFLICT, "이미 보상을 수령한 미션입니다."),

    //── Diet ──────────────────────────────────────────
    FOOD_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사료/간식 기록입니다."),
    FOOD_OCR_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "사료/간식 상품 인식에 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
