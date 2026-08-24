package com.bookstore.client;

import com.bookstore.dto.InventorySummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service", url = "http://inventory-service:8083")
public interface InventoryClient {

    // --- Methods for Book Service ---

    @GetMapping("/api/v1/inventory/book/{bookId}")
    InventorySummaryDto getInventorySummary(@PathVariable("bookId") Long bookId);

    @PostMapping("/api/v1/inventory/book/{bookId}/init")
    void initInventory(@PathVariable("bookId") Long bookId, @RequestParam("totalCopies") Integer totalCopies);

    @DeleteMapping("/api/v1/inventory/book/{bookId}")
    void deleteInventoryByBookId(@PathVariable("bookId") Long bookId);


    // --- Methods for Loan Service ---

    @GetMapping("/api/v1/inventory/books/{bookId}/copies/{copyId}/available")
    Boolean checkAvailability(@PathVariable("bookId") Long bookId, @PathVariable("copyId") Long copyId);

    @PutMapping("/api/v1/inventory/book/{bookId}/borrow")
    void borrowBook(@PathVariable("bookId") Long bookId);

    @PutMapping("/api/v1/inventory/book/{bookId}/return")
    void returnBook(@PathVariable("bookId") Long bookId);
}