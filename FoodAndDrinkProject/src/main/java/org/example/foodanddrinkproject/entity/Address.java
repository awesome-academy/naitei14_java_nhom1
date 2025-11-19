package org.example.foodanddrinkproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_addresses")
@Getter
@Setter
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    private String state;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(nullable = false)
    private String country;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}