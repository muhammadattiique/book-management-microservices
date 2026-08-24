package com.bookstore.event;

import com.bookstore.dto.ItemDto; // Updated import
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanCreatedEvent {
    private Long loanId;
    private Long memberId;
    private List<ItemDto> items;
}