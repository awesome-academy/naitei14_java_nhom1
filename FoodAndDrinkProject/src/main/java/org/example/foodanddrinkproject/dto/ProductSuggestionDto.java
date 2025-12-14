package org.example.foodanddrinkproject.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProductSuggestionDto {
    private Long id;
    private String userName;
    private String productName;
    private String content;
    private LocalDateTime createdAt;
}