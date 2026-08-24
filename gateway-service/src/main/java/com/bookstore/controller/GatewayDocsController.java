package com.example.gatewayservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/gateway")
@Tag(name = "Gateway Management", description = "Endpoints and routing checks managed by API Gateway")
public class GatewayDocsController {

    @Operation(summary = "Check Gateway Health", description = "Verifies that the API Gateway is running and routing requests properly.")
    @ApiResponse(responseCode = "200", description = "Gateway is active")
    @GetMapping("/health-check")
    public Mono<ResponseEntity<String>> gatewayHealth() {
        return Mono.just(ResponseEntity.ok("Gateway is up and routing successfully!"));
    }
}