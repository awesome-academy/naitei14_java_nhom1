package org.example.foodanddrinkproject.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        int statusCode = 500;
        String errorMessage = "An unexpected error occurred";
        
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
        }
        
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            errorMessage = "Page Not Found";
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
            errorMessage = "Access Denied";
        } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
            errorMessage = "Unauthorized";
        } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            errorMessage = "Internal Server Error";
        }
        
        if (message != null && !message.toString().isEmpty()) {
            errorMessage = message.toString();
        }
        
        model.addAttribute("status", statusCode);
        model.addAttribute("error", errorMessage);
        model.addAttribute("message", errorMessage);
        model.addAttribute("timestamp", LocalDateTime.now());
        model.addAttribute("path", requestUri);
        
        // Use admin error template for admin paths
        if (requestUri != null && requestUri.startsWith("/admin")) {
            return "admin/error";
        }
        
        return "error";
    }
}
