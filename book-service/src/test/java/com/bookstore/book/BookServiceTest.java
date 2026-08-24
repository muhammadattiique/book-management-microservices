package com.bookstore.service;

import com.bookstore.dto.BookResponse;
import com.bookstore.entity.Book;
import com.bookstore.mapper.BookMapper;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.service.impl.BookServiceImpl;
import com.bookstore.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book sampleBook;
    private BookResponse sampleBookResponse;

    @BeforeEach
    void setUp() {
        sampleBook = new Book();
        sampleBook.setId(1L);
        sampleBook.setTitle("Spring Boot Microservices");

        sampleBookResponse = new BookResponse();
        sampleBookResponse.setId(1L);
        sampleBookResponse.setTitle("Spring Boot Microservices");
    }

    @Test
    void testGetBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookMapper.toBookResponse(sampleBook)).thenReturn(sampleBookResponse);

        BookResponse response = bookService.getBookById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Spring Boot Microservices");
        verify(bookRepository, times(1)).findById(1L);
    }
}