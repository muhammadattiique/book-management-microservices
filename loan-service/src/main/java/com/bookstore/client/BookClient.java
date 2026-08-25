package com.bookstore.client;

import com.bookstore.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// CRITICAL FIX: Removed hardcoded "url = ..." so Eureka can automatically resolve the Render URL!
@FeignClient(name = "book-service", configuration = FeignClientConfig.class)
public interface BookClient {

    @GetMapping("/api/v1/books/{id}")
    BookDto getBookById(@PathVariable("id") Long id);

    // CRITICAL FIX: Made this 'public static' so JSON (Jackson) can read and map it properly!
    public static class BookDto {
        private Long id;
        private String title;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}