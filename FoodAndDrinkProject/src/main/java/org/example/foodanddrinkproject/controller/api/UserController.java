package org.example.foodanddrinkproject.controller.api;

import org.example.foodanddrinkproject.dto.ApiResponse;
import org.example.foodanddrinkproject.dto.ChangePasswordRequest;
import org.example.foodanddrinkproject.dto.UpdateProfileRequest;
import org.example.foodanddrinkproject.dto.UserProfileDto;
import org.example.foodanddrinkproject.security.CurrentUser;
import org.example.foodanddrinkproject.security.UserPrincipal;
import org.example.foodanddrinkproject.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileDto> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        UserProfileDto userProfile = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileDto> updateProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDto updatedProfile = userService.updateUserProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/me/change-password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> changePassword(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable Long userId) {
        UserProfileDto userProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }
}
