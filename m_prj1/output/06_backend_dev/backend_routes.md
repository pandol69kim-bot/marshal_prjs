# API 라우트 구현 (Spring Boot REST)

## 1. 공통 응답 포맷

### `ApiResponse.java`
```java
package com.example.app.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorDetail error;
    private final PageMeta meta;

    private ApiResponse(boolean success, T data, ErrorDetail error, PageMeta meta) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.meta = meta;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, PageMeta meta) {
        return new ApiResponse<>(true, data, null, meta);
    }

    public static <T> ApiResponse<T> error(ErrorDetail error) {
        return new ApiResponse<>(false, null, error, null);
    }

    @Getter
    public static class PageMeta {
        private final long totalElements;
        private final int totalPages;
        private final int page;
        private final int size;

        public PageMeta(org.springframework.data.domain.Page<?> page) {
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
            this.page = page.getNumber();
            this.size = page.getSize();
        }
    }

    @Getter
    public static class ErrorDetail {
        private final String type;
        private final String title;
        private final int status;
        private final String detail;
        private final String instance;

        public ErrorDetail(String type, String title, int status, String detail, String instance) {
            this.type = type;
            this.title = title;
            this.status = status;
            this.detail = detail;
            this.instance = instance;
        }
    }
}
```

---

## 2. 전역 예외 처리

### `GlobalExceptionHandler.java`
```java
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
                .body(ApiResponse.error(toDetail(errorCode, request.getRequestURI())));
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

    private ApiResponse.ErrorDetail toDetail(ErrorCode code, String instance) {
        return new ApiResponse.ErrorDetail(
                "https://api.example.com/errors/" + code.name().toLowerCase().replace('_', '-'),
                code.getTitle(), code.getStatus().value(), code.getMessage(), instance);
    }
}
```

### `ErrorCode.java`
```java
package com.example.app.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Auth
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid Credentials", "이메일 또는 비밀번호가 올바르지 않습니다", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token Expired", "토큰이 만료되었습니다", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("TOKEN_INVALID", "Invalid Token", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("ACCESS_DENIED", "Access Denied", "접근 권한이 없습니다", HttpStatus.FORBIDDEN),

    // Resource
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Resource Not Found", "리소스를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "Duplicate Resource", "이미 존재하는 리소스입니다", HttpStatus.CONFLICT),

    // External
    EXTERNAL_API_ERROR("EXTERNAL_API_ERROR", "External API Error", "외부 서비스 연동 중 오류가 발생했습니다", HttpStatus.BAD_GATEWAY),
    EXTERNAL_API_TIMEOUT("EXTERNAL_API_TIMEOUT", "External API Timeout", "외부 서비스 응답 시간이 초과되었습니다", HttpStatus.GATEWAY_TIMEOUT);

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
```

---

## 3. 인증 API

### `AuthController.java`
```java
package com.example.app.domain.auth.controller;

import com.example.app.common.response.ApiResponse;
import com.example.app.domain.auth.dto.*;
import com.example.app.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증/인가 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "이메일 로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResponse result = authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "토큰 갱신")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @CookieValue(name = "refreshToken") String refreshToken,
            HttpServletResponse response) {
        TokenResponse result = authService.refresh(refreshToken, response);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken, response);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

### `LoginRequest.java` (DTO - record)
```java
package com.example.app.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        String password
) {}
```

### `LoginResponse.java`
```java
public record LoginResponse(
        String accessToken,
        String tokenType,    // "Bearer"
        long expiresIn,      // 초
        UserDto user
) {}

public record UserDto(
        String id,
        String email,
        String name,
        String role
) {}
```

---

## 4. 리소스 API (페이지네이션/필터링)

### `ResourceController.java`
```java
package com.example.app.domain.resource.controller;

import com.example.app.common.response.ApiResponse;
import com.example.app.domain.resource.dto.*;
import com.example.app.domain.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Resources", description = "리소스 CRUD API")
@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @Operation(summary = "리소스 목록 조회 (페이지네이션/필터링)")
    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<ResourceDto>>> getList(
            @Parameter(description = "상태 필터") @RequestParam(required = false) String status,
            @Parameter(description = "검색어") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<ResourceDto> page = resourceService.findAll(status, keyword, pageable);
        return ResponseEntity.ok(
            ApiResponse.success(page.getContent(), new ApiResponse.PageMeta(page)));
    }

    @Operation(summary = "리소스 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResourceDto>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(resourceService.findById(id)));
    }

    @Operation(summary = "리소스 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<ResourceDto>> create(
            @Valid @RequestBody CreateResourceRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        ResourceDto created = resourceService.create(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @Operation(summary = "리소스 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ResourceDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateResourceRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(resourceService.update(id, request, user.getId())));
    }

    @Operation(summary = "리소스 삭제 (관리자 전용)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

---

## 5. Security 설정

### `SecurityConfig.java`
```java
package com.example.app.config;

import com.example.app.domain.auth.service.OAuth2UserService;
import com.example.app.domain.auth.filter.JwtAuthenticationFilter;
import com.example.app.domain.auth.handler.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/resources/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(ui -> ui.userService(oAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```
