package org.example.foodanddrinkproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSuggestionRequest {
    @NotBlank(message = "Content cannot be empty")
    private String content;
}