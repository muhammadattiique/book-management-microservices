package com.bookstore.client;

import com.bookstore.dto.InventorySummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/api/v1/inventory/book/{bookId}")
    InventorySummaryDto getInventorySummary(@PathVariable("bookId") Long bookId);

    @PostMapping("/api/v1/inventory/init")
    void initInventory(@RequestParam("bookId") Long bookId, @RequestParam("totalCopies") Long totalCopies);

    @DeleteMapping("/api/v1/inventory/book/{bookId}")
    void deleteInventoryByBookId(@PathVariable("bookId") Long bookId);
}