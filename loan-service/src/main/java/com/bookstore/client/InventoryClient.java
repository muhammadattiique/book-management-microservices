package com.bookstore.client;

import com.bookstore.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

// Yahan configuration add ki gayi hai
@FeignClient(name = "inventory-service", url = "http://inventory-service:8083", configuration = FeignClientConfig.class)
public interface InventoryClient {

    @GetMapping("/api/v1/inventory/books/{bookId}/copies/{copyId}/available")
    Boolean checkAvailability(@PathVariable("bookId") Long bookId, @PathVariable("copyId") Long copyId);

    @PutMapping("/api/v1/inventory/book/{bookId}/borrow")
    void borrowBook(@PathVariable("bookId") Long bookId);

    @PutMapping("/api/v1/inventory/book/{bookId}/return")
    void returnBook(@PathVariable("bookId") Long bookId);
}