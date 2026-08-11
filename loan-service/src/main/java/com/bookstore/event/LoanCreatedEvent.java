package com.bookstore.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanCreatedEvent {
    private Long loanId;
    private Long memberId;

    @JsonProperty("items")
    private List<ItemDto> items;
}