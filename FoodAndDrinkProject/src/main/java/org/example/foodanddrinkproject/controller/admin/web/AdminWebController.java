package org.example.foodanddrinkproject.controller.admin.web;

import org.example.foodanddrinkproject.dto.CategoryRequest;
import org.example.foodanddrinkproject.dto.OrderDto;
import org.example.foodanddrinkproject.dto.ProductRequest;
import org.example.foodanddrinkproject.enums.OrderStatus;
import org.example.foodanddrinkproject.enums.PaymentMethod;
import org.example.foodanddrinkproject.service.*;

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

    public AdminWebController(ProductService productService, CategoryService categoryService,
                              OrderService orderService, UserService userService,
                              DashboardService dashboardService,
                              ProductSuggestionService suggestionService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.userService = userService;
        this.dashboardService = dashboardService;
        this.suggestionService = suggestionService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.getStats());
        model.addAttribute("recentOrders", orderService.getAllOrders(
                null, null, org.springframework.data.domain.PageRequest.of(0, 5)).getContent());
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
    public String listProducts(Model model,
                               @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        model.addAttribute("products", productService.getAllProducts(
                null, null, null, null, null, null, null, pageable));
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
            return "admin/product-form";
        }

        if (id != null) {
            productService.updateProduct(id, request);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        } else {
            productService.createProduct(request);
            redirectAttributes.addFlashAttribute("success", "Product created successfully!");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("success", "Product deactivated successfully!");
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    public String listOrders(Model model, Pageable pageable) {
        model.addAttribute("orders", orderService.getAllOrders(null, null, pageable));
        return "admin/orders";
    }

    @GetMapping("/users")
    public String listUsers(Model model, Pageable pageable) {
        model.addAttribute("users", userService.getAllUsers(pageable));
        return "admin/users";
    }

    @GetMapping("/suggestions")
    public String listSuggestions(Model model, Pageable pageable) {
        model.addAttribute("suggestions", suggestionService.getAllSuggestions(pageable));
        return "admin/suggestions";
    }



    @Autowired
    private ChatworkService chatworkService;

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
}
