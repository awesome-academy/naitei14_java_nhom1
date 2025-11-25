package org.example.foodanddrinkproject.service.impl;


import org.example.foodanddrinkproject.dto.CategoryDto;
import org.example.foodanddrinkproject.entity.Category;
import org.example.foodanddrinkproject.repository.CategoryRepository;
import org.example.foodanddrinkproject.service.CategoryService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;


    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }


    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}
