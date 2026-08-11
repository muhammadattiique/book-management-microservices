package com.bookstore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
public class InventoryController {

    @GetMapping("/{bookId}/copies/{copyId}/available")
    public boolean checkAvailability(@PathVariable Long bookId, @PathVariable Long copyId) {
        // Add your logic to check database if the book copy is available
        // Return true for testing purposes for now:
        return true;
    }
}