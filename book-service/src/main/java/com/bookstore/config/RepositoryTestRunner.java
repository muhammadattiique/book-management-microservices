package com.bookstore.config;

import com.bookstore.entity.Author;
import com.bookstore.entity.Category;
import com.bookstore.entity.Book;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.UUID;

@Configuration
public class RepositoryTestRunner {

    @Bean
    CommandLineRunner testRepositories(BookRepository bookRepository,
                                       AuthorRepository authorRepository,
                                       CategoryRepository categoryRepository) {
        return args -> {
            System.out.println("--- TESTING REPOSITORY LAYER ---");

            // Generate a unique suffix to prevent duplicate entry errors on restarts
            String uniqueId = UUID.randomUUID().toString().substring(0, 5);

            // 1. Create and save an Author
            Author author = new Author();
            author.setName("J.K. Rowling " + uniqueId);
            Author savedAuthor = authorRepository.save(author);

            // 2. Create and save a Category
            Category category = new Category();
            category.setName("Fantasy " + uniqueId);
            Category savedCategory = categoryRepository.save(category);

            // 3. Create a test book object and satisfy all @Column(nullable = false) requirements
            Book book = new Book();
            book.setTitle("Harry Potter " + uniqueId);
            book.setIsbn("ISBN-" + uniqueId); // Must be unique and <= 20 chars
            book.setPrice(new BigDecimal("29.99")); // Required precision/scale field
            book.setAuthor(savedAuthor);
            book.setCategory(savedCategory);

            // 4. Save the book
            Book savedBook = bookRepository.save(book);
            System.out.println("Successfully saved book with ID: " + savedBook.getId());

            // 5. Fetch count
            long count = bookRepository.count();
            System.out.println("Total books currently in database: " + count);

            System.out.println("--- REPOSITORY TEST PASSED ---");
        };
    }
}