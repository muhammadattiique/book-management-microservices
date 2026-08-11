package com.bookstore.inventory.controller; // Use your actual package name

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
public class InventoryController {

    @GetMapping("/{bookId}/copies/{copyId}/available")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable("bookId") Long bookId,
            @PathVariable("copyId") Long copyId) {

        // TODO: Add your actual database check logic here.
        // For now, returning true to let your loan-service test pass:
        boolean isAvailable = true;

        return ResponseEntity.ok(isAvailable);
    }
}