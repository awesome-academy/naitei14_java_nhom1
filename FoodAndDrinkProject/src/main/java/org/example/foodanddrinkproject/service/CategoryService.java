package org.example.foodanddrinkproject.service;


import org.example.foodanddrinkproject.dto.CategoryDto;
import org.example.foodanddrinkproject.dto.CategoryRequest;

import java.util.List;


public interface CategoryService {
    List<CategoryDto> getAllCategories();

    CategoryDto createCategory(CategoryRequest request);
    CategoryDto updateCategory(Integer id, CategoryRequest request);
    void deleteCategory(Integer id);
}
