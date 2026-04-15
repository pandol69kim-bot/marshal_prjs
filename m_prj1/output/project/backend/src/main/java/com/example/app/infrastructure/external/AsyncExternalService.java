package com.example.app.infrastructure.external;

import com.example.app.common.exception.ErrorCode;
import com.example.app.common.exception.ExternalApiException;
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

    public Mono<String> fetchDataAsync(String dataId, String baseUrl, String apiKey) {
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
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                        .filter(ex -> ex instanceof java.net.ConnectException))
                .doOnError(ex -> log.error("External API 호출 실패: {}", ex.getMessage()));
    }
}
