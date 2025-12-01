package org.example.foodanddrinkproject.service.impl;

import org.example.foodanddrinkproject.dto.ChangePasswordRequest;
import org.example.foodanddrinkproject.dto.UpdateProfileRequest;
import org.example.foodanddrinkproject.dto.UserProfileDto;
import org.example.foodanddrinkproject.entity.User;
import org.example.foodanddrinkproject.enums.AuthProvider;
import org.example.foodanddrinkproject.exception.BadRequestException;
import org.example.foodanddrinkproject.exception.ResourceNotFoundException;
import org.example.foodanddrinkproject.repository.UserRepository;
import org.example.foodanddrinkproject.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return mapToUserProfileDto(user);
    }

    @Override
    @Transactional
    public UserProfileDto updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());

        User updatedUser = userRepository.save(user);
        return mapToUserProfileDto(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));


        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException("Cannot change password for OAuth2 users");
        }


        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }


        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }


        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }


        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public Page<UserProfileDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public void banUser(Long userId, boolean isEnabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Prevent banning yourself (Admin safety)
        // You might want to get current user ID here to check, but for now simple logic:
        // Don't ban the main admin if you have a specific ID, etc.

        user.setEnabled(isEnabled);
        userRepository.save(user);
    }

    private UserProfileDto convertToDto(User user) {
        UserProfileDto dto = new UserProfileDto();

        // --- MAP FIELDS MANUALLY ---
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());

        if (user.getAuthProvider() != null) {
            dto.setAuthProvider(user.getAuthProvider().name());
        }

        // Optional: If you added 'isEnabled' to UserProfileDto, map it here too
        // dto.setEnabled(user.isEnabled());

        return dto;
    }

    private UserProfileDto mapToUserProfileDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAuthProvider(user.getAuthProvider().name());

        return dto;
    }
}
