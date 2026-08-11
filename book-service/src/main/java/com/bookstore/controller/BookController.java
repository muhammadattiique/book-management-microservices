package com.bookstore.controller;

import com.bookstore.dto.BookRequestDTO;
import com.bookstore.dto.BookResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Book Management", description = "Endpoints for managing books in the system")
public class BookController {

    // Simulating a database using an internal list of DTOs for demonstration
    private final List<BookResponseDTO> books = new ArrayList<>(List.of(
            new BookResponseDTO(1L, "Spring Boot in Action", "Craig Walls", 29.99),
            new BookResponseDTO(2L, "Microservices Patterns", "Chris Richardson", 39.99)
    ));

    // Fixed: Added Long type declaration
    private Long idCounter = 3L;

    @Operation(summary = "Get all books", description = "Retrieves a complete list of all available books.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books")
    @GetMapping
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Add a new book", description = "Creates a new book record in the system.")
    @ApiResponse(responseCode = "201", description = "Book successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid input payload")
    @PostMapping
    public ResponseEntity<BookResponseDTO> addBook(@RequestBody BookRequestDTO requestDTO) {
        BookResponseDTO newBook = new BookResponseDTO(
                idCounter++,
                requestDTO.getTitle(),
                requestDTO.getAuthor(),
                requestDTO.getPrice()
        );
        books.add(newBook);
        return ResponseEntity.status(201).body(newBook);
    }

    @Operation(summary = "Update an existing book", description = "Updates book details by its ID.")
    @ApiResponse(responseCode = "200", description = "Book successfully updated")
    @ApiResponse(responseCode = "404", description = "Book ID not found")
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> updateBook(@PathVariable Long id, @RequestBody BookRequestDTO requestDTO) {
        for (BookResponseDTO book : books) {
            if (book.getId().equals(id)) {
                book.setTitle(requestDTO.getTitle());
                book.setAuthor(requestDTO.getAuthor());
                book.setPrice(requestDTO.getPrice());
                return ResponseEntity.ok(book);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Delete a book", description = "Removes a book record by its ID.")
    @ApiResponse(responseCode = "200", description = "Book successfully deleted")
    @ApiResponse(responseCode = "404", description = "Book ID not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        boolean removed = books.removeIf(book -> book.getId().equals(id));
        if (removed) {
            return ResponseEntity.ok("Book deleted successfully with ID: " + id);
        }
        return ResponseEntity.notFound().build();
    }
}