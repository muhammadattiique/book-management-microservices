package com.bookstore.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "INVENTORY-SERVICE") // Match the exact app name in Eureka
public interface InventoryClient {

    @GetMapping("/api/v1/books/{bookId}/copies/{copyId}/available")
    boolean checkAvailability(@PathVariable("bookId") Long bookId, @PathVariable("copyId") Long copyId);
}