package com.example.app.infrastructure.external.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "sendgrid", url = "https://api.sendgrid.com/v3")
public interface SendGridClient {

    @PostMapping("/mail/send")
    void send(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> request);
}
