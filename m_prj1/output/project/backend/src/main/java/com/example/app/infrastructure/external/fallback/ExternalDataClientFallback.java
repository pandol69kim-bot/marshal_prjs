package com.example.app.infrastructure.external.fallback;

import com.example.app.infrastructure.external.client.ExternalDataClient;
import com.example.app.infrastructure.external.client.ExternalDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExternalDataClientFallback implements ExternalDataClient {

    @Override
    public ExternalDataResponse getData(String id, String apiKey) {
        log.warn("Circuit breaker activated for getData: id={}", id);
        return ExternalDataResponse.empty();
    }
}
