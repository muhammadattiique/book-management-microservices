package com.bookstore.event;

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
public class BookReturnedEvent {
    private Long loanId;
    private List<Long> copyIds;
    private LocalDate returnDate;
}