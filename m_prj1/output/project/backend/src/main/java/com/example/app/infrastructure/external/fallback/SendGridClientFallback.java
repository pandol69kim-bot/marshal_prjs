package com.example.app.infrastructure.external.fallback;

import com.example.app.infrastructure.external.client.SendGridClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SendGridClientFallback implements SendGridClient {

    @Override
    public void send(String authorization, Map<String, Object> request) {
        log.warn("SendGrid circuit breaker activated — 이메일 발송 생략");
    }
}
