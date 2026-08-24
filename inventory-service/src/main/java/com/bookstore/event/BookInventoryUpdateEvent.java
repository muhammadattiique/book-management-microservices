package com.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookInventoryUpdateEvent {
    private Long bookId;
    private Integer totalCopies;
}