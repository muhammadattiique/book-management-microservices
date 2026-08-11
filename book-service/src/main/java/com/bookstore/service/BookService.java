package com.bookstore.service;

import com.bookstore.dto.BookCreateRequest;
import com.bookstore.dto.BookResponse;
import com.bookstore.dto.BookUpdateRequest;
import com.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface BookService {
    BookResponse createBook(BookCreateRequest request);
    BookResponse getBookById(Long id);
    List<BookResponse> getAllBooks();
    Page<BookResponse> searchBooks(Specification<Book> spec, Pageable pageable);
    BookResponse updateBook(Long id, BookUpdateRequest request);
    void deleteBook(Long id);
}