package com.bookstore.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    @JsonProperty("bookId")
    private Long bookId;

    @JsonProperty("copyId")
    private Long copyId;
}