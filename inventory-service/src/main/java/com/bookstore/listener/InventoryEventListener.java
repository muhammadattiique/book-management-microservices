package com.bookstore.listener;

import com.bookstore.event.BookCreatedEvent;
import com.bookstore.event.BookDeletedEvent;
import com.bookstore.event.BookReturnedEvent;
import com.bookstore.event.LoanCreatedEvent;
import com.bookstore.model.Inventory;
import com.bookstore.repository.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryRepository inventoryRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = "book-created-topic", groupId = "inventory-group")
    public void handleBookCreated(String eventJson) {
        try {
            BookCreatedEvent event = objectMapper.readValue(eventJson, BookCreatedEvent.class);
            for (int i = 1; i <= event.getTotalCopies(); i++) {
                long copyId = (long) i;
                boolean exists = inventoryRepository.findByBookIdAndCopyId(event.getBookId(), copyId).isPresent();
                if (!exists) {
                    Inventory copy = new Inventory();
                    copy.setBookId(event.getBookId());
                    copy.setCopyId(copyId);
                    copy.setAvailable(true);
                    inventoryRepository.save(copy);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process BookCreatedEvent", e);
        }
    }

    @Transactional
    @KafkaListener(topics = "book-updated-topic", groupId = "inventory-group")
    public void handleBookUpdated(String eventJson) {
        try {
            BookCreatedEvent event = objectMapper.readValue(eventJson, BookCreatedEvent.class);
            Long bookId = event.getBookId();
            int targetCopies = event.getTotalCopies();

            List<Inventory> existingCopies = inventoryRepository.findByBookId(bookId);
            int currentCopies = existingCopies.size();

            if (targetCopies > currentCopies) {
                long maxCopyId = existingCopies.stream().mapToLong(Inventory::getCopyId).max().orElse(0L);
                for (int i = 1; i <= (targetCopies - currentCopies); i++) {
                    Inventory copy = new Inventory();
                    copy.setBookId(bookId);
                    copy.setCopyId(maxCopyId + i);
                    copy.setAvailable(true);
                    inventoryRepository.save(copy);
                }
            } else if (targetCopies < currentCopies) {
                existingCopies.stream()
                        .sorted(Comparator.comparing(Inventory::getCopyId).reversed())
                        .limit(currentCopies - targetCopies)
                        .forEach(inventoryRepository::delete);
            }
        } catch (Exception e) {
            log.error("Failed to process BookUpdatedEvent", e);
        }
    }

    @Transactional
    @KafkaListener(topics = "book-deleted-topic", groupId = "inventory-group")
    public void handleBookDeleted(String eventJson) {
        try {
            BookDeletedEvent event = objectMapper.readValue(eventJson, BookDeletedEvent.class);
            inventoryRepository.deleteByBookId(event.getBookId());
        } catch (Exception e) {
            log.error("Failed to process BookDeletedEvent", e);
        }
    }

    @Transactional
    @KafkaListener(topics = "loan-created-topic", groupId = "inventory-group")
    public void handleLoanCreated(String eventJson) {
        try {
            LoanCreatedEvent event = objectMapper.readValue(eventJson, LoanCreatedEvent.class);
            if (event.getItems() != null) {
                event.getItems().forEach(item -> {
                    Long bookId = item.getBookId();
                    Long copyId = item.getCopyId() != null ? item.getCopyId() : 1L;

                    // 1. Try finding by exact bookId and copyId
                    Optional<Inventory> target = inventoryRepository.findByBookIdAndCopyId(bookId, copyId);

                    // 2. Fallback: If exact copy not found, pick ANY available copy for this book
                    if (target.isEmpty()) {
                        target = inventoryRepository.findByBookId(bookId).stream()
                                .filter(Inventory::isAvailable)
                                .findFirst();
                    }

                    target.ifPresentOrElse(inventory -> {
                        inventory.setAvailable(false);
                        inventoryRepository.save(inventory);
                        log.info("Inventory MINUS SUCCESS: Book ID {} Copy ID {} marked UNAVAILABLE", inventory.getBookId(), inventory.getCopyId());
                    }, () -> log.warn("Inventory MINUS FAILED: No available copies found for Book ID {}", bookId));
                });
            }
        } catch (Exception e) {
            log.error("Failed to process LoanCreatedEvent", e);
        }
    }

    @Transactional
    @KafkaListener(topics = "book-returned-topic", groupId = "inventory-group")
    public void handleBookReturned(String eventJson) {
        try {
            BookReturnedEvent event = objectMapper.readValue(eventJson, BookReturnedEvent.class);
            if (event.getItems() != null) {
                event.getItems().forEach(item -> {
                    Long bookId = item.getBookId();
                    Long copyId = item.getCopyId() != null ? item.getCopyId() : 1L;

                    // 1. Try finding by exact bookId and copyId
                    Optional<Inventory> target = inventoryRepository.findByBookIdAndCopyId(bookId, copyId);

                    // 2. Fallback: If exact copy not found, pick ANY borrowed (unavailable) copy to return
                    if (target.isEmpty()) {
                        target = inventoryRepository.findByBookId(bookId).stream()
                                .filter(inv -> !inv.isAvailable())
                                .findFirst();
                    }

                    target.ifPresentOrElse(inventory -> {
                        inventory.setAvailable(true);
                        inventoryRepository.save(inventory);
                        log.info("Inventory PLUS SUCCESS: Book ID {} Copy ID {} marked AVAILABLE", inventory.getBookId(), inventory.getCopyId());
                    }, () -> log.warn("Inventory PLUS FAILED: No borrowed copies found for Book ID {}", bookId));
                });
            }
        } catch (Exception e) {
            log.error("Failed to process BookReturnedEvent", e);
        }
    }
}