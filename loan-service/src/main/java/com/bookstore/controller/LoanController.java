package com.bookstore.controller;

import com.bookstore.dto.LoanCreateRequest;
import com.bookstore.dto.LoanResponse;
import com.bookstore.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@Tag(name = "Loan Management", description = "Endpoints for managing book loans and copies")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @Operation(summary = "Create a new loan")
    @ApiResponse(responseCode = "201", description = "Loan successfully created")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.createLoan(request));
    }

    @Operation(summary = "Get loan by ID")
    @ApiResponse(responseCode = "200", description = "Loan retrieved successfully")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @Operation(summary = "Get loans by member ID")
    @ApiResponse(responseCode = "200", description = "List of loans retrieved")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<LoanResponse>> getLoansByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(loanService.getLoansByMemberId(memberId));
    }

    @Operation(summary = "Return a loan")
    @ApiResponse(responseCode = "200", description = "Loan marked as returned")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponse> returnLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.returnLoan(id));
    }
}