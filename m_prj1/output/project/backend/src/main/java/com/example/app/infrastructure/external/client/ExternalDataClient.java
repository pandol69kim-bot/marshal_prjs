package com.example.app.infrastructure.external.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "externalDataClient",
        url = "${external.data-api.base-url}",
        fallback = com.example.app.infrastructure.external.fallback.ExternalDataClientFallback.class
)
public interface ExternalDataClient {

    @GetMapping("/data/{id}")
    ExternalDataResponse getData(
            @PathVariable("id") String id,
            @RequestHeader("X-API-KEY") String apiKey);
}
