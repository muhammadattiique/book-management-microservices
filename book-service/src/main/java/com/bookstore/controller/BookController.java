package com.bookstore.controller;

import com.bookstore.dto.BookCreateRequest;
import com.bookstore.dto.BookResponse;
import com.bookstore.dto.BookUpdateRequest;
import com.bookstore.entity.Book;
import com.bookstore.service.BookService;
import com.bookstore.specification.BookSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Book Management", description = "Endpoints for managing books in the system")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Get all books", description = "Retrieves a complete list of all available books.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books")
    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @Operation(summary = "Search and paginate books", description = "Retrieves books with optional filters, pagination, and sorting.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list of books")
    @GetMapping("/search")
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer year,
            Pageable pageable) {

        Specification<Book> spec = BookSpecification.filterBooks(title, author, category, isbn, year);
        Page<BookResponse> books = bookService.searchBooks(spec, pageable);
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Get books by Author ID", description = "Custom query to retrieve books written by a specific author.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved books by author")
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<BookResponse>> getBooksByAuthor(@PathVariable Long authorId) {
        return ResponseEntity.ok(bookService.getBooksByAuthor(authorId));
    }

    @Operation(summary = "Get books by Category Name", description = "Custom query to retrieve books belonging to a specific category.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved books by category")
    @GetMapping("/category/name/{categoryName}")
    public ResponseEntity<List<BookResponse>> getBooksByCategoryName(@PathVariable String categoryName) {
        return ResponseEntity.ok(bookService.getBooksByCategoryName(categoryName));
    }

    @Operation(summary = "Get budget-friendly books", description = "Custom JPQL query to retrieve books under or equal to a specific price.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved budget books")
    @GetMapping("/filter/max-price")
    public ResponseEntity<List<BookResponse>> getBooksCheaperThan(@RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(bookService.getBooksCheaperThan(maxPrice));
    }

    @Operation(summary = "Get book by ID", description = "Retrieves a book record by its unique ID.")
    @ApiResponse(responseCode = "200", description = "Book successfully retrieved")
    @ApiResponse(responseCode = "404", description = "Book ID not found")
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @Operation(summary = "Add a new book", description = "Creates a new book record in the system.")
    @ApiResponse(responseCode = "201", description = "Book successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid input payload")
    @PostMapping
    public ResponseEntity<BookResponse> addBook(@Valid @RequestBody BookCreateRequest requestDTO) {
        BookResponse newBook = bookService.createBook(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBook);
    }

    @Operation(summary = "Update an existing book", description = "Updates book details by its ID.")
    @ApiResponse(responseCode = "200", description = "Book successfully updated")
    @ApiResponse(responseCode = "404", description = "Book ID not found")
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookUpdateRequest requestDTO) {
        BookResponse updatedBook = bookService.updateBook(id, requestDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @Operation(summary = "Delete a book", description = "Removes a book record by its ID.")
    @ApiResponse(responseCode = "200", description = "Book successfully deleted")
    @ApiResponse(responseCode = "404", description = "Book ID not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}