package com.bookstore.event;

import java.time.LocalDate;
import java.util.List;

public class BookReturnedEvent {
    private Long loanId;
    private List<ItemDto> items;
    private LocalDate returnDate;

    public BookReturnedEvent() {}

    // All-args constructor to bypass setter dependency completely
    public BookReturnedEvent(Long loanId, List<ItemDto> items, LocalDate returnDate) {
        this.loanId = loanId;
        this.items = items;
        this.returnDate = returnDate;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
}