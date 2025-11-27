package org.example.foodanddrinkproject.service.impl;

import org.example.foodanddrinkproject.dto.AddressDto;
import org.example.foodanddrinkproject.entity.Address;
import org.example.foodanddrinkproject.entity.User;
import org.example.foodanddrinkproject.exception.ResourceNotFoundException;
import org.example.foodanddrinkproject.repository.AddressRepository;
import org.example.foodanddrinkproject.repository.UserRepository;
import org.example.foodanddrinkproject.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private AddressDto mapToDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setRecipientName(address.getRecipientName());
        dto.setPhoneNumber(address.getPhoneNumber());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setCountry(address.getCountry());

        dto.setIsDefault(address.isDefault());

        return dto;
    }

    private void handleDefaultAddressLogic(User user, boolean isNewDefault) {
        if (isNewDefault) {
            List<Address> addresses = addressRepository.findByUserId(user.getId());
            for (Address address : addresses) {
                if (address.isDefault()) {
                    address.setDefault(false);
                    addressRepository.save(address);
                }
            }
        }
    }

    @Override
    public List<AddressDto> getAllAddresses() {
        User user = getCurrentUser();
        List<Address> addresses = addressRepository.findByUserId(user.getId());
        return addresses.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressDto addAddress(AddressDto addressDto) {
        User user = getCurrentUser();

        if (Boolean.TRUE.equals(addressDto.getIsDefault())) {
            handleDefaultAddressLogic(user, true);
        } else {
            List<Address> existing = addressRepository.findByUserId(user.getId());
            if (existing.isEmpty()) {
                addressDto.setIsDefault(true);
            }
        }

        Address address = new Address();
        address.setUser(user);
        address.setRecipientName(addressDto.getRecipientName());
        address.setPhoneNumber(addressDto.getPhoneNumber());
        address.setStreet(addressDto.getStreet());
        address.setCity(addressDto.getCity());
        address.setState(addressDto.getState());
        address.setZipCode(addressDto.getZipCode());
        address.setCountry(addressDto.getCountry());
        address.setDefault(addressDto.getIsDefault() != null ? addressDto.getIsDefault() : false);

        Address savedAddress = addressRepository.save(address);
        return mapToDto(savedAddress);
    }

    @Override
    @Transactional
    public AddressDto updateAddress(Long id, AddressDto addressDto) {
        User user = getCurrentUser();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to update this address");
        }

        // SỬA: Dùng .isDefault()
        if (Boolean.TRUE.equals(addressDto.getIsDefault())) {
            handleDefaultAddressLogic(user, true);
            address.setDefault(true);
        } else if (Boolean.FALSE.equals(addressDto.getIsDefault()) && address.isDefault()) {
            address.setDefault(false);
        }

        address.setRecipientName(addressDto.getRecipientName());
        address.setPhoneNumber(addressDto.getPhoneNumber());
        address.setStreet(addressDto.getStreet());
        address.setCity(addressDto.getCity());
        address.setState(addressDto.getState());
        address.setZipCode(addressDto.getZipCode());
        address.setCountry(addressDto.getCountry());

        Address updatedAddress = addressRepository.save(address);
        return mapToDto(updatedAddress);
    }

    @Override
    public void deleteAddress(Long id) {
        User user = getCurrentUser();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to delete this address");
        }

        addressRepository.delete(address);
    }
}