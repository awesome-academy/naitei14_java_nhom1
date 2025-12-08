package org.example.foodanddrinkproject.service.impl;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import org.example.foodanddrinkproject.dto.ProductDto;
import org.example.foodanddrinkproject.dto.ProductRequest;
import org.example.foodanddrinkproject.entity.Category;
import org.example.foodanddrinkproject.entity.Product;
import org.example.foodanddrinkproject.enums.ProductType;
import org.example.foodanddrinkproject.exception.BadRequestException;
import org.example.foodanddrinkproject.exception.ResourceNotFoundException;
import org.example.foodanddrinkproject.repository.CategoryRepository;
import org.example.foodanddrinkproject.repository.ProductRepository;
import org.example.foodanddrinkproject.repository.specification.ProductSpecification;
import org.example.foodanddrinkproject.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSpecification productSpecification;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductSpecification productSpecification,
                              CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productSpecification = productSpecification;
        this.categoryRepository = categoryRepository;
    }
    @Override
    public Page<ProductDto> getAllProducts(
            String name, String brand, Integer categoryId, ProductType type,
            BigDecimal minPrice, BigDecimal maxPrice, Double minRating,
            Pageable pageable
    ) {
        Specification<Product> spec =
                productSpecification.isActive()
                        .and(productSpecification.hasName(name))
                        .and(productSpecification.hasBrand(brand))
                        .and(productSpecification.hasCategory(categoryId))
                        .and(productSpecification.hasType(type))
                        .and(productSpecification.priceBetween(minPrice, maxPrice))
                        .and(productSpecification.ratingGreaterThanOrEqual(minRating));

        Page<Product> productsPage = productRepository.findAll(spec, pageable);
        return productsPage.map(this::convertToDto);
    }

    @Override
    public ProductDto getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return convertToDto(product);
    }

    @Override
    @Transactional
    public ProductDto createProduct(@Valid ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (request.getDiscountPrice() != null &&
                request.getDiscountPrice().compareTo(request.getPrice()) >= 0) {
            throw new BadRequestException("Discount price must be less than regular price");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setImageUrl(request.getImageUrl());
        product.setSku(request.getSku());
        product.setBrand(request.getBrand());
        product.setWeight(request.getWeight());
        product.setProductType(request.getProductType());
        product.setCategory(category);
        int stockQty = 0;
        if (request.getStockQuantity() != null) {
            stockQty = request.getStockQuantity();
        }
        product.setStockQuantity(stockQty);
        product.setActive(Boolean.TRUE.equals(request.getIsActive()));

        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (request.getDiscountPrice() != null &&
                request.getDiscountPrice().compareTo(request.getPrice()) >= 0) {
            throw new BadRequestException("Discount price must be less than regular price");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setImageUrl(request.getImageUrl());
        product.setBrand(request.getBrand());
        product.setWeight(request.getWeight());
        product.setProductType(request.getProductType());
        product.setCategory(category);
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getIsActive() != null) {
            product.setActive(request.getIsActive());
        }

        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductDto toggleProductStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.setActive(!product.isActive());
        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setSku(product.getSku());
        dto.setBrand(product.getBrand());
        dto.setWeight(product.getWeight());
        dto.setActive(product.isActive());
        dto.setProductType(product.getProductType().name());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setAvgRating(product.getAvgRating());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        return dto;
    }
}
