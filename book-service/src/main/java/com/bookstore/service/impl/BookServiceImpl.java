package com.bookstore.service.impl;

import com.bookstore.client.InventoryClient;
import com.bookstore.dto.BookCreateRequest;
import com.bookstore.dto.BookResponse;
import com.bookstore.dto.BookUpdateRequest;
import com.bookstore.dto.InventorySummaryDto;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.entity.Category;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;
    private final InventoryClient inventoryClient;

    private BookResponse mapToBookResponseWithInventory(Book book) {
        BookResponse response = bookMapper.toBookResponse(book);
        try {
            InventorySummaryDto summary = inventoryClient.getInventorySummary(book.getId());
            if (summary != null) {
                response.setTotalCopies(summary.getTotalCopies() != null ? summary.getTotalCopies() : 0L);
                response.setAvailableCopies(summary.getAvailableCopies() != null ? summary.getAvailableCopies() : 0L);
            } else {
                response.setTotalCopies(0L);
                response.setAvailableCopies(0L);
            }
        } catch (Exception e) {
            log.warn("Could not fetch inventory summary for book ID: {}. Error: {}", book.getId(), e.getMessage());
            response.setTotalCopies(0L);
            response.setAvailableCopies(0L);
        }
        return response;
    }

    @Override
    public BookResponse createBook(BookCreateRequest request) {
        log.info("Attempting to create a new book with ISBN: {}. Total Copies received: {}",
                request.getIsbn(), request.getTotalCopies());

        String authorName = request.getAuthorName() != null ? request.getAuthorName().trim() : "";
        String categoryName = request.getCategoryName() != null ? request.getCategoryName().trim() : "";

        // Standard object instantiation to prevent missing @Builder errors
        Author author = authorRepository.findByNameIgnoreCase(authorName)
                .orElseGet(() -> {
                    Author newAuthor = new Author();
                    newAuthor.setName(authorName);
                    return authorRepository.save(newAuthor);
                });

        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(categoryName);
                    return categoryRepository.save(newCategory);
                });

        Book book = Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .description(request.getDescription())
                .publicationDate(request.getPublicationDate())
                .language(request.getLanguage())
                .publisher(request.getPublisher())
                .price(request.getPrice())
                .author(author)
                .category(category)
                .build();

        Book savedBook = bookRepository.save(book);

        if (request.getTotalCopies() != null && request.getTotalCopies() > 0) {
            try {
                inventoryClient.initInventory(savedBook.getId(), request.getTotalCopies().longValue());
                log.info("Successfully initialized {} inventory copies for Book ID: {}", request.getTotalCopies(), savedBook.getId());
            } catch (Exception e) {
                log.error("Failed to initialize inventory for book ID: {}", savedBook.getId(), e);
            }
        }

        return mapToBookResponseWithInventory(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        log.info("Fetching book with ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));
        return mapToBookResponseWithInventory(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::mapToBookResponseWithInventory)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(Specification<Book> spec, Pageable pageable) {
        return bookRepository.findAll(spec, pageable)
                .map(this::mapToBookResponseWithInventory);
    }

    @Override
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        log.info("Attempting to update book with ID: {}. Total copies: {}", id, request.getTotalCopies());

        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        if (request.getIsbn() != null) existingBook.setIsbn(request.getIsbn());
        if (request.getTitle() != null) existingBook.setTitle(request.getTitle());
        if (request.getDescription() != null) existingBook.setDescription(request.getDescription());
        if (request.getPublicationDate() != null) existingBook.setPublicationDate(request.getPublicationDate());
        if (request.getLanguage() != null) existingBook.setLanguage(request.getLanguage());
        if (request.getPublisher() != null) existingBook.setPublisher(request.getPublisher());
        if (request.getPrice() != null) existingBook.setPrice(request.getPrice());

        Book updatedBook = bookRepository.save(existingBook);

        if (request.getTotalCopies() != null && request.getTotalCopies() >= 0) {
            try {
                inventoryClient.initInventory(updatedBook.getId(), request.getTotalCopies().longValue());
                log.info("Successfully updated inventory copies for Book ID: {}", updatedBook.getId());
            } catch (Exception e) {
                log.error("Failed to update inventory for book ID: {}", updatedBook.getId(), e);
            }
        }

        return mapToBookResponseWithInventory(updatedBook);
    }

    @Override
    public void deleteBook(Long id) {
        log.info("Attempting to delete book with ID: {}", id);
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with ID: " + id);
        }

        bookRepository.deleteById(id);
        log.info("Successfully deleted book with ID: {} from database", id);

        try {
            inventoryClient.deleteInventoryByBookId(id);
            log.info("Successfully deleted inventory records for Book ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete inventory records for Book ID: {}", id, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByAuthor(Long authorId) {
        return bookRepository.findByAuthorId(authorId).stream()
                .map(this::mapToBookResponseWithInventory)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByCategoryName(String categoryName) {
        return bookRepository.findByCategoryNameIgnoreCase(categoryName).stream()
                .map(this::mapToBookResponseWithInventory)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksCheaperThan(BigDecimal maxPrice) {
        return bookRepository.findBooksCheaperThan(maxPrice).stream()
                .map(this::mapToBookResponseWithInventory)
                .collect(Collectors.toList());
    }
}