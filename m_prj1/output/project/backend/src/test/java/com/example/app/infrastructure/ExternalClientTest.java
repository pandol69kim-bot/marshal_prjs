package com.example.app.infrastructure;

import com.example.app.infrastructure.external.AsyncExternalService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExternalClientTest {

    private WireMockServer wireMockServer;

    private AsyncExternalService asyncExternalService;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        WebClient webClient = WebClient.builder().build();
        asyncExternalService = new AsyncExternalService(webClient);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("외부 API 정상 응답 시 데이터 반환")
    void fetchDataAsync_success() {
        // Arrange
        stubFor(get(urlEqualTo("/data/test-id"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"test-id\",\"data\":\"value\"}")));

        String baseUrl = "http://localhost:" + wireMockServer.port();

        // Act
        String result = asyncExternalService
                .fetchDataAsync("test-id", baseUrl, "api-key")
                .block();

        // Assert
        assertThat(result).contains("test-id");
    }
}
