package org.example.foodanddrinkproject.service;

import org.example.foodanddrinkproject.dto.ChangePasswordRequest;
import org.example.foodanddrinkproject.dto.UpdateProfileRequest;
import org.example.foodanddrinkproject.dto.UserProfileDto;

public interface UserService {
    UserProfileDto getUserProfile(Long userId);
    UserProfileDto updateUserProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
}
