package com.example.app.domain.notification.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NcpSmsAdapter implements SmsAdapter {

    private final WebClient webClient;

    @Value("${ncp.sms.access-key:}")
    private String accessKey;

    @Value("${ncp.sms.secret-key:}")
    private String secretKey;

    @Value("${ncp.sms.service-id:}")
    private String serviceId;

    @Value("${ncp.sms.sender-phone:}")
    private String senderPhone;

    private static final String NCP_SMS_URL = "https://sens.apigw.ntruss.com";

    @Override
    public void send(String phone, String message) {
        if (accessKey.isBlank()) {
            log.warn("NCP SMS 설정 미완료 — 발송 생략: phone={}", phone);
            return;
        }

        long timestamp = System.currentTimeMillis();
        String path = "/sms/v2/services/" + serviceId + "/messages";
        String signature = makeSignature(timestamp, path);

        Map<String, Object> body = Map.of(
                "type", "SMS",
                "contentType", "COMM",
                "countryCode", "82",
                "from", senderPhone,
                "content", message,
                "messages", List.of(Map.of("to", phone))
        );

        webClient.post()
                .uri(NCP_SMS_URL + path)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-ncp-apigw-timestamp", String.valueOf(timestamp))
                .header("x-ncp-iam-access-key", accessKey)
                .header("x-ncp-apigw-signature-v2", signature)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("SMS 발송 완료: phone={}", phone);
    }

    private String makeSignature(long timestamp, String path) {
        try {
            String message = "POST " + path + "\n" + timestamp + "\n" + accessKey;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("NCP SMS 서명 생성 실패", e);
        }
    }
}
