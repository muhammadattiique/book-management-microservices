package com.bookstore.specification;

import com.bookstore.entity.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> filterBooks(String title, String author, String category, String isbn, Integer year) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (title != null && !title.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (isbn != null && !isbn.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("isbn")), "%" + isbn.toLowerCase() + "%"));
            }
            if (author != null && !author.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("author").get("name")), "%" + author.toLowerCase() + "%"));
            }
            if (category != null && !category.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("category").get("name")), "%" + category.toLowerCase() + "%"));
            }
            if (year != null) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.function("YEAR", Integer.class, root.get("publicationDate")), year));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}