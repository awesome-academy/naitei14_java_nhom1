package org.example.foodanddrinkproject.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {
    private Long id;
    private String recipientName;
    private String phoneNumber;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Boolean isDefault;
}