package org.example.foodanddrinkproject.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import java.util.StringJoiner;

@Getter
@Setter
public class AddressDto {
    private Long id;

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    private String state;
    private String zipCode;

    @NotBlank(message = "Country is required")
    private String country;

    private boolean isDefault;

    /**
     * Returns a formatted full address string combining all address fields.
     * Example: "123 Main St, New York, NY 10001, USA"
     */
    public String getFullAddress() {
        StringJoiner joiner = new StringJoiner(", ");
        
        if (street != null && !street.isBlank()) {
            joiner.add(street);
        }
        if (city != null && !city.isBlank()) {
            joiner.add(city);
        }
        if (state != null && !state.isBlank()) {
            joiner.add(state);
        }
        if (zipCode != null && !zipCode.isBlank()) {
            joiner.add(zipCode);
        }
        if (country != null && !country.isBlank()) {
            joiner.add(country);
        }
        
        return joiner.toString();
    }
}
