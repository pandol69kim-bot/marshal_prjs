package com.example.app.domain.notification.adapter;

import com.example.app.infrastructure.external.client.SendGridClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
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
        Map<String, Object> body = Map.of(
                "personalizations", List.of(
                        Map.of("to", List.of(Map.of("email", to)))),
                "from", Map.of("email", fromEmail),
                "subject", subject,
                "content", List.of(Map.of("type", "text/html", "value", content))
        );

        sendGridClient.send("Bearer " + apiKey, body);
        log.info("이메일 발송 완료: to={}, subject={}", to, subject);
    }
}
