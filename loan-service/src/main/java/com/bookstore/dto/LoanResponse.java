package com.bookstore.dto;

import com.bookstore.enums.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LoanResponse {
    private Long id;
    private Long memberId;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;
    private List<LoanItemResponse> items;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class LoanItemResponse {
        private Long id;
        private Long bookId;
        private Long copyId;
    }
}