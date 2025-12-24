package org.example.foodanddrinkproject.controller.admin.web;

import org.example.foodanddrinkproject.dto.CategoryRequest;
import org.example.foodanddrinkproject.dto.OrderDto;
import org.example.foodanddrinkproject.dto.ProductRequest;
import org.example.foodanddrinkproject.enums.OrderStatus;
import org.example.foodanddrinkproject.enums.PaymentMethod;
import org.example.foodanddrinkproject.enums.ProductType;
import org.example.foodanddrinkproject.scheduler.MonthlyStatisticsScheduler;
import org.example.foodanddrinkproject.service.*;

import java.math.BigDecimal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;
    private final DashboardService dashboardService;
    private final ProductSuggestionService suggestionService;
    private final RatingService ratingService;
    private final FileStorageService fileStorageService;

    public AdminWebController(ProductService productService, CategoryService categoryService,
                              OrderService orderService, UserService userService,
                              DashboardService dashboardService,
                              ProductSuggestionService suggestionService,
                              RatingService ratingService,
                              FileStorageService fileStorageService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.userService = userService;
        this.dashboardService = dashboardService;
        this.suggestionService = suggestionService;
        this.ratingService = ratingService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.getStats());
        model.addAttribute("recentOrders", orderService.getAllOrders(
                null, null, org.springframework.data.domain.PageRequest.of(0, 5, 
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))).getContent());
        return "admin/dashboard";
    }


    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("categoryRequest", new CategoryRequest());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute("categoryRequest") CategoryRequest request,
                               RedirectAttributes redirectAttributes) {
        categoryService.createCategory(request);
        redirectAttributes.addFlashAttribute("success", "Category saved successfully!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete category (it may contain products).");
        }
        return "redirect:/admin/categories";
    }


    @GetMapping("/products")
    public String listProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) ProductType type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Model model,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        
        model.addAttribute("products", productService.getAllProductsAdmin(
                name, categoryId, type, minPrice, maxPrice, pageable));
        
        // For filter form
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("filterName", name);
        model.addAttribute("filterCategoryId", categoryId);
        model.addAttribute("filterType", type);
        model.addAttribute("filterMinPrice", minPrice);
        model.addAttribute("filterMaxPrice", maxPrice);
        
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String showCreateProductForm(Model model) {
        model.addAttribute("productRequest", new ProductRequest());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        var product = productService.getProductById(id);

        ProductRequest request = new ProductRequest();
        request.setName(product.getName());
        request.setPrice(product.getPrice());
        request.setStockQuantity(product.getStockQuantity());
        request.setCategoryId(product.getCategoryId());
        request.setDescription(product.getDescription());
        request.setImageUrl(product.getImageUrl());
        request.setSku(product.getSku());
        request.setBrand(product.getBrand());
        request.setProductType(ProductType.valueOf(product.getProductType()));
        request.setIsActive(product.isActive());

        model.addAttribute("productRequest", request);
        model.addAttribute("productId", id).addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute("productRequest") @Valid ProductRequest request,
                              BindingResult result,
                              @RequestParam(required = false) Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            if (id != null) {
                model.addAttribute("productId", id);
            }
            return "admin/product-form";
        }

        try {
            // Handle file upload
            if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
                String imagePath = fileStorageService.storeFile(request.getImageFile(), "products");
                request.setImageUrl(imagePath);
            }

            if (id != null) {
                productService.updateProduct(id, request);
                redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
            } else {
                productService.createProduct(request);
                redirectAttributes.addFlashAttribute("success", "Product created successfully!");
            }
            return "redirect:/admin/products";
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Handle duplicate SKU or other database constraints
            model.addAttribute("error", "SKU already exists. Please use a unique SKU.");
            model.addAttribute("categories", categoryService.getAllCategories());
            if (id != null) {
                model.addAttribute("productId", id);
            }
            return "admin/product-form";
        } catch (org.example.foodanddrinkproject.exception.BadRequestException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            if (id != null) {
                model.addAttribute("productId", id);
            }
            return "admin/product-form";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            if (id != null) {
                model.addAttribute("productId", id);
            }
            return "admin/product-form";
        }
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("success", "Product deactivated successfully!");
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long userId,
            Model model, 
            Pageable pageable) {
        model.addAttribute("orders", orderService.getAllOrders(status, userId, pageable));
        
        // For filter form
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterUserId", userId);
        
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        OrderDto order = orderService.getOrderByIdForAdmin(id);
        model.addAttribute("order", order);
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("paymentStatuses", org.example.foodanddrinkproject.enums.PaymentStatus.values());
        
        // Initialize DTO for form
        org.example.foodanddrinkproject.dto.AdminUpdateOrderRequest request = new org.example.foodanddrinkproject.dto.AdminUpdateOrderRequest();
        request.setOrderStatus(order.getOrderStatus());
        request.setPaymentStatus(order.getPaymentStatus());
        model.addAttribute("orderRequest", request);
        
        return "admin/order-detail";
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam Long id, 
                                    @ModelAttribute("orderRequest") org.example.foodanddrinkproject.dto.AdminUpdateOrderRequest request,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrder(id, request);
            redirectAttributes.addFlashAttribute("success", "Order status updated successfully!");
        } catch (org.example.foodanddrinkproject.exception.BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    @GetMapping("/users")
    public String listUsers(Model model, Pageable pageable) {
        model.addAttribute("users", userService.getAllUsers(pageable));
        return "admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        var user = userService.getUserById(id);
        
        org.example.foodanddrinkproject.dto.AdminUpdateUserRequest request = new org.example.foodanddrinkproject.dto.AdminUpdateUserRequest();
        request.setFullName(user.getFullName());
        request.setPhoneNumber(user.getPhoneNumber());
        request.setEnabled(user.isEnabled());
        request.setRoles(user.getRoles());
        
        model.addAttribute("userRequest", request);
        model.addAttribute("userId", id);
        model.addAttribute("allRoles", userService.getAllRoles());
        return "admin/user-form";
    }
    
    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute("userRequest") org.example.foodanddrinkproject.dto.AdminUpdateUserRequest request,
                           @RequestParam Long id,
                           RedirectAttributes redirectAttributes) {
         userService.updateUser(id, request);
         redirectAttributes.addFlashAttribute("success", "User updated successfully!");
         return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.banUser(id, false);
        redirectAttributes.addFlashAttribute("success", "User has been disabled.");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/enable/{id}")
    public String enableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.banUser(id, true);
        redirectAttributes.addFlashAttribute("success", "User has been enabled.");
        return "redirect:/admin/users";
    }

    @GetMapping("/suggestions")
    public String listSuggestions(Model model, Pageable pageable) {
        model.addAttribute("suggestions", suggestionService.getAllSuggestions(pageable));
        return "admin/suggestions";
    }

    @GetMapping("/suggestions/delete/{id}")
    public String deleteSuggestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        suggestionService.deleteSuggestion(id);
        redirectAttributes.addFlashAttribute("success", "Suggestion deleted.");
        return "redirect:/admin/suggestions";
    }

    @GetMapping("/reviews")
    public String listReviews(
            @RequestParam(required = false) Long productId,
            Model model, 
            Pageable pageable) {
        model.addAttribute("reviews", ratingService.getAllRatingsFiltered(productId, pageable));
        
        var products = productService.getAllProductsAdmin(null, null, null, null, null, 
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();
        model.addAttribute("products", products);
        model.addAttribute("filterProductId", productId);
        
        // Find selected product name for display
        if (productId != null) {
            products.stream()
                    .filter(p -> p.getId().equals(productId))
                    .findFirst()
                    .ifPresent(p -> model.addAttribute("selectedProductName", p.getName()));
        }
        
        return "admin/reviews";
    }

    @GetMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ratingService.deleteRating(id);
        redirectAttributes.addFlashAttribute("success", "Review deleted.");
        return "redirect:/admin/reviews";
    }



    @Autowired
    private ChatworkService chatworkService;

    @Autowired
    private MonthlyStatisticsScheduler monthlyStatisticsScheduler;

    @GetMapping("/test-chatwork")
    @ResponseBody
    public String testChatwork() {
        OrderDto dummyOrder = new OrderDto();
        dummyOrder.setId(9999L);
        dummyOrder.setTotalAmount(new java.math.BigDecimal("150.00"));
        dummyOrder.setPaymentMethod(PaymentMethod.COD);
        dummyOrder.setOrderStatus(OrderStatus.PENDING);

        chatworkService.sendOrderNotification(dummyOrder);

        return "Check your Chatwork room!";
    }

    @GetMapping("/test-monthly-stats")
    @ResponseBody
    public String testMonthlyStats() {
        monthlyStatisticsScheduler.sendStatisticsManually();
        return "Monthly statistics sent to Chatwork! Check your room.";
    }
}
