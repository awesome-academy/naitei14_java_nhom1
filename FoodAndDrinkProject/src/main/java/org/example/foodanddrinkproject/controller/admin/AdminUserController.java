package org.example.foodanddrinkproject.controller.admin;

import org.example.foodanddrinkproject.dto.ApiResponse;
import org.example.foodanddrinkproject.dto.UserProfileDto;
import org.example.foodanddrinkproject.service.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users.
     * GET /api/admin/users
     */
    @GetMapping
    public ResponseEntity<Page<UserProfileDto>> getAllUsers(
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    /**
     * Ban or Unban a user.
     * PUT /api/admin/users/{id}/ban?enable=false
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> changeUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enable) {

        userService.banUser(id, enable);
        String status = enable ? "unbanned" : "banned";
        return ResponseEntity.ok(new ApiResponse(true, "User has been " + status));
    }
}