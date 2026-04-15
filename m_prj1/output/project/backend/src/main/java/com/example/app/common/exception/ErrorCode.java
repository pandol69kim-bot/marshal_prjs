package com.example.app.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Auth
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid Credentials",
            "이메일 또는 비밀번호가 올바르지 않습니다", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token Expired",
            "토큰이 만료되었습니다", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("TOKEN_INVALID", "Invalid Token",
            "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("ACCESS_DENIED", "Access Denied",
            "접근 권한이 없습니다", HttpStatus.FORBIDDEN),

    // Resource
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Resource Not Found",
            "리소스를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "Duplicate Resource",
            "이미 존재하는 리소스입니다", HttpStatus.CONFLICT),

    // External
    EXTERNAL_API_ERROR("EXTERNAL_API_ERROR", "External API Error",
            "외부 서비스 연동 중 오류가 발생했습니다", HttpStatus.BAD_GATEWAY),
    EXTERNAL_API_TIMEOUT("EXTERNAL_API_TIMEOUT", "External API Timeout",
            "외부 서비스 응답 시간이 초과되었습니다", HttpStatus.GATEWAY_TIMEOUT);

    private final String code;
    private final String title;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String title, String message, HttpStatus status) {
        this.code = code;
        this.title = title;
        this.message = message;
        this.status = status;
    }
}
