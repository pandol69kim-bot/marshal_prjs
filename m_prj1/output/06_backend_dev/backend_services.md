# 외부 시스템 연동 서비스 레이어

## 1. Feign Client 선언적 외부 연동

### `ExternalDataClient.java` (Feign Interface)
```java
package com.example.app.infrastructure.external.client;

import com.example.app.infrastructure.external.dto.ExternalDataResponse;
import com.example.app.infrastructure.external.fallback.ExternalDataClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "externalDataClient",
    url = "${external.data-api.base-url}",
    fallback = ExternalDataClientFallback.class
)
public interface ExternalDataClient {

    @GetMapping("/data/{id}")
    ExternalDataResponse getData(
            @PathVariable("id") String id,
            @RequestHeader("X-API-KEY") String apiKey);

    @PostMapping("/data/batch")
    ExternalBatchResponse batchProcess(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestBody ExternalBatchRequest request);
}
```

### `ExternalDataClientFallback.java` (Circuit Breaker Fallback)
```java
package com.example.app.infrastructure.external.fallback;

import com.example.app.infrastructure.external.client.ExternalDataClient;
import com.example.app.infrastructure.external.dto.ExternalDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExternalDataClientFallback implements ExternalDataClient {

    @Override
    public ExternalDataResponse getData(String id, String apiKey) {
        log.warn("Circuit breaker activated for getData: id={}", id);
        // 기본 응답 반환 (캐시 or 빈 응답)
        return ExternalDataResponse.empty();
    }

    @Override
    public ExternalBatchResponse batchProcess(String apiKey, ExternalBatchRequest request) {
        log.warn("Circuit breaker activated for batchProcess");
        throw new ServiceUnavailableException("외부 데이터 서비스를 일시적으로 사용할 수 없습니다");
    }
}
```

### `FeignConfig.java`
```java
package com.example.app.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import com.example.app.common.exception.ExternalApiException;
import com.example.app.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.example.app.infrastructure.external")
public class FeignConfig {

    @Value("${external.data-api.api-key}")
    private String dataApiKey;

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;  // 운영: NONE, 개발: BASIC or FULL
    }

    // 공통 헤더 자동 추가
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("X-Correlation-ID", generateCorrelationId());
            template.header("User-Agent", "MyApp/1.0");
        };
    }

    // 외부 API 에러 → 내부 예외 변환
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            if (response.status() == 404) {
                return new ExternalApiException(ErrorCode.RESOURCE_NOT_FOUND,
                    "외부 API에서 리소스를 찾을 수 없습니다");
            }
            if (response.status() == 429) {
                return new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR,
                    "외부 API 요청 한도 초과");
            }
            if (response.status() >= 500) {
                return new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR,
                    "외부 API 서버 오류: " + response.status());
            }
            return new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR,
                "외부 API 호출 실패: " + response.status());
        };
    }

    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }
}
```

---

## 2. WebClient 비동기 외부 연동

### `WebClientConfig.java`
```java
package com.example.app.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
            .responseTimeout(Duration.ofSeconds(5))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS)));

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .filter(logRequest())
            .filter(logResponse())
            .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.debug("WebClient Request: {} {}", req.method(), req.url());
            return Mono.just(req);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(res -> {
            log.debug("WebClient Response: {}", res.statusCode());
            return Mono.just(res);
        });
    }
}
```

### `AsyncExternalService.java` (WebClient 사용 예)
```java
package com.example.app.infrastructure.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncExternalService {

    private final WebClient webClient;

    public Mono<ExternalDataDto> fetchDataAsync(String dataId, String baseUrl, String apiKey) {
        return webClient.get()
            .uri(baseUrl + "/data/{id}", dataId)
            .header("X-API-KEY", apiKey)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response ->
                response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(
                        new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR,
                            "4xx 오류: " + body))))
            .onStatus(HttpStatus::is5xxServerError, response ->
                Mono.error(new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR,
                    "외부 서버 5xx 오류")))
            .bodyToMono(ExternalDataDto.class)
            .timeout(Duration.ofSeconds(5))
            .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                .filter(ex -> ex instanceof java.net.ConnectException))
            .doOnError(ex -> log.error("External API 호출 실패: {}", ex.getMessage()));
    }
}
```

---

## 3. 알림 서비스 (비동기)

### `NotificationService.java`
```java
package com.example.app.domain.notification.service;

import com.example.app.domain.notification.adapter.EmailAdapter;
import com.example.app.domain.notification.adapter.SmsAdapter;
import com.example.app.domain.notification.entity.NotificationHistory;
import com.example.app.domain.notification.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailAdapter emailAdapter;
    private final SmsAdapter smsAdapter;
    private final NotificationHistoryRepository historyRepository;

    @Async("notificationExecutor")
    public void sendEmail(String to, String subject, String content) {
        NotificationHistory history = NotificationHistory.create(to, "EMAIL", subject, content);
        historyRepository.save(history);

        try {
            emailAdapter.send(to, subject, content);
            history.markSent();
        } catch (Exception e) {
            log.error("이메일 발송 실패: to={}, error={}", to, e.getMessage());
            history.markFailed(e.getMessage());
        } finally {
            historyRepository.save(history);
        }
    }

    @Async("notificationExecutor")
    public void sendSms(String phone, String message) {
        NotificationHistory history = NotificationHistory.create(phone, "SMS", "SMS", message);
        historyRepository.save(history);

        try {
            smsAdapter.send(phone, message);
            history.markSent();
        } catch (Exception e) {
            log.error("SMS 발송 실패: phone={}, error={}", phone, e.getMessage());
            history.markFailed(e.getMessage());
        } finally {
            historyRepository.save(history);
        }
    }
}
```

### `SendGridEmailAdapter.java`
```java
package com.example.app.domain.notification.adapter;

import com.example.app.infrastructure.external.client.SendGridClient;
import com.example.app.infrastructure.external.dto.SendGridRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendGridEmailAdapter implements EmailAdapter {

    private final SendGridClient sendGridClient;

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Override
    public void send(String to, String subject, String content) {
        SendGridRequest request = SendGridRequest.builder()
            .personalizations(List.of(
                Personalization.builder()
                    .to(List.of(new Email(to)))
                    .build()))
            .from(new Email(fromEmail))
            .subject(subject)
            .content(List.of(new Content("text/html", content)))
            .build();

        sendGridClient.send("Bearer " + apiKey, request);
    }
}
```

### `SendGridClient.java` (Feign)
```java
package com.example.app.infrastructure.external.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "sendgrid", url = "https://api.sendgrid.com/v3")
public interface SendGridClient {

    @PostMapping("/mail/send")
    void send(
        @RequestHeader("Authorization") String authorization,
        @RequestBody SendGridRequest request);
}
```

---

## 4. 외부 API 로깅 AOP

### `ExternalApiLoggingAspect.java`
```java
package com.example.app.common.aop;

import com.example.app.infrastructure.external.repository.ExternalApiLogRepository;
import com.example.app.infrastructure.external.entity.ExternalApiLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExternalApiLoggingAspect {

    private final ExternalApiLogRepository logRepository;

    @Around("execution(* com.example.app.infrastructure.external.client.*.*(..))")
    public Object logExternalCall(ProceedingJoinPoint pjp) throws Throwable {
        String serviceName = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        long start = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("[External API] {}.{} - SUCCESS ({}ms)", serviceName, methodName, duration);
            saveLog(serviceName, methodName, 200, duration, null);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("[External API] {}.{} - FAILED ({}ms): {}", serviceName, methodName, duration, ex.getMessage());
            saveLog(serviceName, methodName, 500, duration, ex.getMessage());
            throw ex;
        }
    }

    private void saveLog(String service, String method, int status, long durationMs, String error) {
        try {
            ExternalApiLog log = ExternalApiLog.builder()
                .serviceName(service)
                .endpoint(method)
                .responseStatus(status)
                .durationMs(durationMs)
                .responseBody(error)
                .build();
            logRepository.save(log);
        } catch (Exception e) {
            // 로그 저장 실패가 비즈니스 로직에 영향 주지 않도록
        }
    }
}
```

---

## 5. S3 파일 업로드

### `S3StorageService.java`
```java
package com.example.app.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @Value("${s3.cdn-url:}")
    private String cdnUrl;

    // 직접 업로드
    public String upload(MultipartFile file, String directory) throws IOException {
        validateFile(file);
        String key = directory + "/" + UUID.randomUUID() + getExtension(file.getOriginalFilename());

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build(),
            RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        return buildUrl(key);
    }

    // Presigned URL 발급 (프론트엔드 직접 업로드)
    public PresignedUrlResponse generatePresignedUrl(String filename, String contentType) {
        String key = "uploads/" + UUID.randomUUID() + getExtension(filename);

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(15))
            .putObjectRequest(req -> req
                .bucket(bucketName)
                .key(key)
                .contentType(contentType))
            .build();

        String uploadUrl = presigner.presignPutObject(presignRequest).url().toString();
        String fileUrl = buildUrl(key);

        return new PresignedUrlResponse(uploadUrl, fileUrl, key);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("파일이 비어있습니다");
        if (file.getSize() > 50 * 1024 * 1024) throw new IllegalArgumentException("파일 크기는 50MB를 초과할 수 없습니다");

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") &&
                !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

    private String buildUrl(String key) {
        return cdnUrl.isEmpty()
            ? "https://" + bucketName + ".s3.amazonaws.com/" + key
            : cdnUrl + "/" + key;
    }
}
```
