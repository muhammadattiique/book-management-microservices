package com.bookstore.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class LoanCreateRequest {
    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotEmpty(message = "A loan must contain at least one item")
    private List<LoanItemDto> items;

    @Data
    public static class LoanItemDto {
        @NotNull(message = "Book ID is required")
        private Long bookId;

        @NotNull(message = "Copy ID is required")
        private Long copyId;
    }
}