package com.bookstore.controller;

import com.bookstore.dto.InventorySummaryDto;
import com.bookstore.model.Inventory;
import com.bookstore.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    @GetMapping({"/books/{bookId}/copies/{copyId}/available", "/book/{bookId}/copy/{copyId}/available"})
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable("bookId") Long bookId,
            @PathVariable("copyId") Long copyId) {

        // FIX: Strict copyId check replaced.
        // Ab system check karega ke is book ki KUI BHI copy available hai ya nahi.
        List<Inventory> copies = inventoryRepository.findByBookId(bookId);
        boolean isAvailable = copies.stream().anyMatch(Inventory::isAvailable);

        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping({"/book/{bookId}", "/books/{bookId}"})
    public ResponseEntity<InventorySummaryDto> getInventorySummary(@PathVariable Long bookId) {
        List<Inventory> copies = inventoryRepository.findByBookId(bookId);
        long total = copies.size();
        long available = copies.stream().filter(Inventory::isAvailable).count();

        InventorySummaryDto summary = new InventorySummaryDto(bookId, total, available);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/book/{bookId}/init")
    public ResponseEntity<Void> initInventory(@PathVariable Long bookId, @RequestParam int totalCopies) {
        List<Inventory> existingCopies = inventoryRepository.findByBookId(bookId);
        int currentCopies = existingCopies.size();

        if (currentCopies < totalCopies) {
            long maxCopyId = existingCopies.stream().mapToLong(Inventory::getCopyId).max().orElse(0L);
            for (int i = 1; i <= (totalCopies - currentCopies); i++) {
                Inventory copy = new Inventory();
                copy.setBookId(bookId);
                copy.setCopyId(maxCopyId + i);
                copy.setAvailable(true);
                inventoryRepository.save(copy);
            }
        }
        return ResponseEntity.ok().build();
    }

    @Transactional
    @PutMapping("/book/{bookId}/borrow")
    public ResponseEntity<Void> borrowBook(@PathVariable Long bookId) {
        List<Inventory> copies = inventoryRepository.findByBookId(bookId);
        Optional<Inventory> availableCopy = copies.stream().filter(Inventory::isAvailable).findFirst();
        if (availableCopy.isPresent()) {
            Inventory inv = availableCopy.get();
            inv.setAvailable(false);
            inventoryRepository.save(inv);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @Transactional
    @PutMapping("/book/{bookId}/return")
    public ResponseEntity<Void> returnBook(@PathVariable Long bookId) {
        List<Inventory> copies = inventoryRepository.findByBookId(bookId);
        Optional<Inventory> unavailableCopy = copies.stream().filter(inv -> !inv.isAvailable()).findFirst();
        if (unavailableCopy.isPresent()) {
            Inventory inv = unavailableCopy.get();
            inv.setAvailable(true);
            inventoryRepository.save(inv);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    // Direct synchronous delete endpoint for inventory records when book is deleted
    @Transactional
    @DeleteMapping("/book/{bookId}")
    public ResponseEntity<Void> deleteInventoryByBookId(@PathVariable Long bookId) {
        inventoryRepository.deleteByBookId(bookId);
        return ResponseEntity.ok().build();
    }
}