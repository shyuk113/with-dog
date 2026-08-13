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

    //── Comment──────────────────────────────────────────
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "본인이 작성한 댓글만 수정/삭제할 수 있습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
