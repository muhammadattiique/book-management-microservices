package com.bookstore.repository;

import com.bookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    // Custom Query 1: Find books by Author ID
    List<Book> findByAuthorId(Long authorId);

    // Custom Query 2: Find books by Category Name (case-insensitive)
    List<Book> findByCategoryNameIgnoreCase(String categoryName);

    // Custom Query 3: Find books cheaper than or equal to a specific max price using JPQL
    @Query("SELECT b FROM Book b WHERE b.price <= :maxPrice")
    List<Book> findBooksCheaperThan(@Param("maxPrice") BigDecimal maxPrice);
}