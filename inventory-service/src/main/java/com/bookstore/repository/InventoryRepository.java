package com.bookstore.repository;

import com.bookstore.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByBookIdAndCopyId(Long bookId, Long copyId);
    List<Inventory> findByBookId(Long bookId);
    void deleteByBookId(Long bookId);

    // Added to count exact available copies for a book
    long countByBookIdAndAvailable(Long bookId, boolean available);
}