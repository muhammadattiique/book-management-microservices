package com.bookstore.service.impl;

import com.bookstore.dto.BookCreateRequest;
import com.bookstore.dto.BookResponse;
import com.bookstore.dto.BookUpdateRequest;
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

    @Override
    public BookResponse createBook(BookCreateRequest request) {
        log.info("Attempting to create a new book with ISBN: {}", request.getIsbn());

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> {
                    log.warn("Author not found with ID: {}", request.getAuthorId());
                    return new ResourceNotFoundException("Author not found with ID: " + request.getAuthorId());
                });

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Category not found with ID: {}", request.getCategoryId());
                    return new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId());
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
        log.info("Successfully created book with ID: {}", savedBook.getId());

        return bookMapper.toBookResponse(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        log.info("Fetching book with ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book not found with ID: {}", id);
                    return new ResourceNotFoundException("Book not found with ID: " + id);
                });
        return bookMapper.toBookResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        log.info("Fetching all books from the database");
        return bookRepository.findAll().stream()
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(Specification<Book> spec, Pageable pageable) {
        log.info("Searching books with filters, page number: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return bookRepository.findAll(spec, pageable)
                .map(bookMapper::toBookResponse);
    }

    @Override
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        log.info("Attempting to update book with ID: {}", id);

        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book update failed. Book not found with ID: {}", id);
                    return new ResourceNotFoundException("Book not found with ID: " + id);
                });

        if (request.getIsbn() != null) existingBook.setIsbn(request.getIsbn());
        if (request.getTitle() != null) existingBook.setTitle(request.getTitle());
        if (request.getDescription() != null) existingBook.setDescription(request.getDescription());
        if (request.getPublicationDate() != null) existingBook.setPublicationDate(request.getPublicationDate());
        if (request.getLanguage() != null) existingBook.setLanguage(request.getLanguage());
        if (request.getPublisher() != null) existingBook.setPublisher(request.getPublisher());
        if (request.getPrice() != null) existingBook.setPrice(request.getPrice());

        if (request.getAuthorId() != null) {
            Author author = authorRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Author not found with ID: " + request.getAuthorId()));
            existingBook.setAuthor(author);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
            existingBook.setCategory(category);
        }

        Book updatedBook = bookRepository.save(existingBook);
        log.info("Successfully updated book with ID: {}", updatedBook.getId());

        return bookMapper.toBookResponse(updatedBook);
    }

    @Override
    public void deleteBook(Long id) {
        log.info("Attempting to delete book with ID: {}", id);
        if (!bookRepository.existsById(id)) {
            log.warn("Book deletion failed. Book not found with ID: {}", id);
            throw new ResourceNotFoundException("Book not found with ID: " + id);
        }
        bookRepository.deleteById(id);
        log.info("Successfully deleted book with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByAuthor(Long authorId) {
        log.info("Fetching books for author ID: {}", authorId);
        return bookRepository.findByAuthorId(authorId).stream()
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByCategoryName(String categoryName) {
        log.info("Fetching books for category name: {}", categoryName);
        return bookRepository.findByCategoryNameIgnoreCase(categoryName).stream()
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksCheaperThan(BigDecimal maxPrice) {
        log.info("Fetching books with max price: {}", maxPrice);
        return bookRepository.findBooksCheaperThan(maxPrice).stream()
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
    }
}