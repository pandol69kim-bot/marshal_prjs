package com.example.app.config;

import com.example.app.common.exception.ErrorCode;
import com.example.app.common.exception.ExternalApiException;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("X-Correlation-ID", UUID.randomUUID().toString());
            template.header("User-Agent", "ApiService/1.0");
        };
    }

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
}
