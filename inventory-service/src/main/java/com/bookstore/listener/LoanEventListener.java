package com.bookstore.listener;

import com.bookstore.event.LoanCreatedEvent;
import com.bookstore.event.BookReturnedEvent;
import com.bookstore.model.Inventory;
import com.bookstore.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanEventListener {

    private final InventoryRepository inventoryRepository;

    @KafkaListener(topics = "loan-created-topic", groupId = "inventory-group")
    public void handleLoanCreated(LoanCreatedEvent event) {
        log.info("Received LoanCreatedEvent: loanId={}, memberId={}, items={}",
                event.getLoanId(), event.getMemberId(), event.getItems());

        if (event.getItems() == null || event.getItems().isEmpty()) {
            log.warn("Skipping inventory update because items list is null or empty!");
            return;
        }

        for (var item : event.getItems()) {
            if (item.getBookId() == null || item.getCopyId() == null) {
                log.warn("Skipping item because bookId or copyId is null");
                continue;
            }

            Inventory inventory = inventoryRepository.findByBookIdAndCopyId(item.getBookId(), item.getCopyId())
                    .orElse(new Inventory());

            inventory.setBookId(item.getBookId());
            inventory.setCopyId(item.getCopyId());
            inventory.setAvailable(false); // Mark as unavailable

            inventoryRepository.save(inventory);
            log.info("Successfully saved inventory: Book ID {} (Copy ID {}) -> UNAVAILABLE", item.getBookId(), item.getCopyId());
        }
    }

    @KafkaListener(topics = "book-returned-topic", groupId = "inventory-group")
    public void handleBookReturned(BookReturnedEvent event) {
        log.info("Received BookReturnedEvent: loanId={}, items={}", event.getLoanId(), event.getItems());

        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        for (var item : event.getItems()) {
            Inventory inventory = inventoryRepository.findByBookIdAndCopyId(item.getBookId(), item.getCopyId())
                    .orElse(new Inventory());

            inventory.setBookId(item.getBookId());
            inventory.setCopyId(item.getCopyId());
            inventory.setAvailable(true); // Mark as available

            inventoryRepository.save(inventory);
            log.info("Successfully saved inventory: Book ID {} (Copy ID {}) -> AVAILABLE", item.getBookId(), item.getCopyId());
        }
    }
}