package com.bookstore.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanCreateRequest {
    private Long memberId;
    private String memberName; // <-- Yeh field add karna zaroori tha
    private LocalDate dueDate;

    @NotEmpty(message = "Loan must contain at least one item.")
    private List<LoanItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanItemRequest {
        private Long bookId;
        private Long copyId;
    }
}