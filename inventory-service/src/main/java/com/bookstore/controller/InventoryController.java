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

    // UPDATED: Now handles both INCREASING and DECREASING copies dynamically
    @PostMapping("/init")
    public ResponseEntity<Void> initInventory(@RequestParam Long bookId, @RequestParam Long totalCopies) {
        List<Inventory> existingCopies = inventoryRepository.findByBookId(bookId);
        int currentCopies = existingCopies.size();

        // Condition 1: Agar admin copies barha de (e.g., 20 se 30)
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
        // Condition 2: Agar admin copies kam kar de (e.g., 20 se 10)
        else if (currentCopies > totalCopies) {
            long difference = currentCopies - totalCopies;
            existingCopies.stream()
                    .filter(Inventory::isAvailable) // Sirf available (un-borrowed) copies delete karega
                    .sorted((c1, c2) -> Long.compare(c2.getCopyId(), c1.getCopyId())) // Highest ID se shuru karega
                    .limit(difference)
                    .forEach(inventoryRepository::delete);
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

    @Transactional
    @DeleteMapping("/book/{bookId}")
    public ResponseEntity<Void> deleteInventoryByBookId(@PathVariable Long bookId) {
        inventoryRepository.deleteByBookId(bookId);
        return ResponseEntity.ok().build();
    }
}