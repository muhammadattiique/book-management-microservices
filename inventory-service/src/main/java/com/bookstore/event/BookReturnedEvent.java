package com.bookstore.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookReturnedEvent {
    private Long loanId;
    private List<ItemDto> items; // Add this to know which books were returned
}