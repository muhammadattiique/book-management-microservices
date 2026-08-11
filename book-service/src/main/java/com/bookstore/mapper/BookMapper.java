package com.bookstore.mapper;

import com.bookstore.dto.AuthorResponse;
import com.bookstore.dto.CategoryResponse;
import com.bookstore.dto.BookResponse;
import com.bookstore.entity.Author;
import com.bookstore.entity.Category;
import com.bookstore.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public AuthorResponse toAuthorResponse(Author author) {
        if (author == null) return null;
        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .bio(author.getBio())
                .createdAt(author.getCreatedAt())
                .updatedAt(author.getUpdatedAt())
                .build();
    }

    public CategoryResponse toCategoryResponse(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public BookResponse toBookResponse(Book book) {
        if (book == null) return null;
        return BookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .description(book.getDescription())
                .publicationDate(book.getPublicationDate())
                .language(book.getLanguage())
                .publisher(book.getPublisher())
                .price(book.getPrice())
                .author(toAuthorResponse(book.getAuthor()))
                .category(toCategoryResponse(book.getCategory()))
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}