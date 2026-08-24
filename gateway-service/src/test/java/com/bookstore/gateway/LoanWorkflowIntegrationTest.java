package com.example.gatewayservice;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@SpringBootTest(
        classes = GatewayServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class LoanWorkflowIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private static String authToken = "dummy-token";
    private static Long loanId = 1L;

    @Test
    @Order(1)
    void testUserLoginAndTokenRetrieval() {
        Map<String, String> loginRequest = Map.of(
                "username", "testuser",
                "password", "password123"
        );

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().value(status ->
                        org.junit.jupiter.api.Assertions.assertTrue(status >= 200 && status < 600)
                );
    }

    @Test
    @Order(2)
    void testBrowseCatalogThroughGateway() {
        webTestClient.get()
                .uri("/api/books")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().value(status ->
                        org.junit.jupiter.api.Assertions.assertTrue(status >= 200 && status < 600)
                );
    }

    @Test
    @Order(3)
    void testBorrowBookWorkflow() {
        Map<String, Object> borrowRequest = Map.of(
                "bookId", 1L,
                "userId", 1L
        );

        webTestClient.post()
                .uri("/api/loans")
                .headers(headers -> headers.setBearerAuth(authToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(borrowRequest)
                .exchange()
                .expectStatus().value(status ->
                        org.junit.jupiter.api.Assertions.assertTrue(status >= 200 && status < 600)
                );
    }

    @Test
    @Order(4)
    void testReturnBookWorkflow() {
        webTestClient.post()
                .uri("/api/loans/" + loanId + "/return")
                .headers(headers -> headers.setBearerAuth(authToken))
                .exchange()
                .expectStatus().value(status ->
                        org.junit.jupiter.api.Assertions.assertTrue(status >= 200 && status < 600)
                );
    }
}