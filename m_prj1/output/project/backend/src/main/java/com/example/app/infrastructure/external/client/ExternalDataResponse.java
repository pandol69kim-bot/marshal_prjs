package com.example.app.infrastructure.external.client;

public record ExternalDataResponse(
        String id,
        String data,
        String status
) {
    public static ExternalDataResponse empty() {
        return new ExternalDataResponse(null, null, "UNAVAILABLE");
    }
}
