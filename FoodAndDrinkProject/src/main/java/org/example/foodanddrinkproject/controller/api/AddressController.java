package org.example.foodanddrinkproject.controller.api;

import org.example.foodanddrinkproject.dto.AddressDto;
import org.example.foodanddrinkproject.dto.ApiResponse;
import org.example.foodanddrinkproject.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressDto>> getAllAddresses() {
        return ResponseEntity.ok(addressService.getAllAddresses());
    }

    @PostMapping
    public ResponseEntity<AddressDto> addAddress(@RequestBody AddressDto addressDto) {
        return new ResponseEntity<>(addressService.addAddress(addressDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(@PathVariable Long id, @RequestBody AddressDto addressDto) {
        return ResponseEntity.ok(addressService.updateAddress(id, addressDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(new ApiResponse(true, "Address deleted successfully"));
    }
}