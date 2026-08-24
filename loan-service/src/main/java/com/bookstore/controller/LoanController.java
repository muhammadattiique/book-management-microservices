package com.bookstore.controller;

import com.bookstore.dto.LoanCreateRequest;
import com.bookstore.dto.LoanResponse;
import com.bookstore.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/loans")
@Tag(name = "Loan Management", description = "Endpoints for managing book loans and copies")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    // Helper method to robustly resolve numeric ID or string username to a unique Long memberId
    private Long resolveMemberId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return 1L;
        }
        String identity = authentication.getName();
        try {
            return Long.parseLong(identity);
        } catch (NumberFormatException e) {
            // Generates a consistent deterministic positive Long ID from the username string (e.g., "Abdullah")
            return Math.abs((long) identity.hashCode());
        }
    }

    @Operation(summary = "Create a new loan")
    @ApiResponse(responseCode = "201", description = "Loan successfully created")
    @PreAuthorize("hasRole('MEMBER') or hasRole('STUDENT') or hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(
            @Valid @RequestBody LoanCreateRequest request,
            Authentication authentication) {

        Long memberId = resolveMemberId(authentication);
        request.setMemberId(memberId);
        log.info("Creating loan for resolved member ID: {}", memberId);

        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.createLoan(request));
    }

    @Operation(summary = "Get current user's loans")
    @ApiResponse(responseCode = "200", description = "List of current user's loans retrieved")
    @PreAuthorize("hasRole('MEMBER') or hasRole('STUDENT') or hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @GetMapping("/my")
    public ResponseEntity<List<LoanResponse>> getMyLoans(Authentication authentication) {
        String identity = authentication != null ? authentication.getName() : "1";
        log.info("Fetching loans for authenticated user identity: {}", identity);

        return ResponseEntity.ok(loanService.getLoansForUser(identity));
    }

    @Operation(summary = "Get loan by ID")
    @ApiResponse(responseCode = "200", description = "Loan retrieved successfully")
    @PreAuthorize("hasRole('MEMBER') or hasRole('STUDENT') or hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @Operation(summary = "Get loans by member ID")
    @ApiResponse(responseCode = "200", description = "List of loans retrieved")
    @PreAuthorize("hasRole('MEMBER') or hasRole('STUDENT') or hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<LoanResponse>> getLoansByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(loanService.getLoansByMemberId(memberId));
    }

    @Operation(summary = "Return a loan")
    @ApiResponse(responseCode = "200", description = "Loan marked as returned")
    @PreAuthorize("hasRole('MEMBER') or hasRole('STUDENT') or hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponse> returnLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.returnLoan(id));
    }

    @Operation(summary = "Renew a loan")
    @ApiResponse(responseCode = "200", description = "Loan successfully renewed")
    @PreAuthorize("hasRole('MEMBER') or hasRole('STUDENT') or hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @PatchMapping("/{id}/renew")
    public ResponseEntity<LoanResponse> renewLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.renewLoan(id));
    }
}