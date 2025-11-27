package org.example.foodanddrinkproject.service;

import org.example.foodanddrinkproject.dto.AddressDto;

import java.util.List;

public interface AddressService {
    List<AddressDto> getAllAddresses();
    AddressDto addAddress(AddressDto addressDto);
    AddressDto updateAddress(Long id, AddressDto addressDto);
    void deleteAddress(Long id);
}
