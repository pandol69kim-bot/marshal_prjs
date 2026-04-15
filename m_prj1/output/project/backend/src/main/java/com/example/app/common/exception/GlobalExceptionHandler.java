package com.example.app.common.exception;

import com.example.app.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("BusinessException: {} at {}", errorCode.getMessage(), request.getRequestURI());
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(toDetail(errorCode, ex.getMessage(), request.getRequestURI())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        var error = new ApiResponse.ErrorDetail(
                "https://api.example.com/errors/validation",
                "Validation Failed", 400, detail, request.getRequestURI());
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleExternalApi(
            ExternalApiException ex, HttpServletRequest request) {
        log.error("External API error: {}", ex.getMessage());
        var error = new ApiResponse.ErrorDetail(
                "https://api.example.com/errors/external-api",
                "External Service Error", 502, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ApiResponse.error(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: ", request.getRequestURI(), ex);
        var error = new ApiResponse.ErrorDetail(
                "https://api.example.com/errors/internal",
                "Internal Server Error", 500, "예기치 않은 오류가 발생했습니다", request.getRequestURI());
        return ResponseEntity.internalServerError().body(ApiResponse.error(error));
    }

    private ApiResponse.ErrorDetail toDetail(ErrorCode code, String detail, String instance) {
        return new ApiResponse.ErrorDetail(
                "https://api.example.com/errors/" + code.name().toLowerCase().replace('_', '-'),
                code.getTitle(), code.getStatus().value(), detail, instance);
    }
}
