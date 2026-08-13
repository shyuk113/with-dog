package com.example.withdog.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String code;
    private final String message;

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorcode){
        return ResponseEntity.status(errorcode.getStatus())
                .body(ErrorResponse.builder()
                        .status(errorcode.getStatus().value())
                        .error(errorcode.getStatus().name())
                        .code(errorcode.name())
                        .message(errorcode.getMessage())
                .build());
    }
}
