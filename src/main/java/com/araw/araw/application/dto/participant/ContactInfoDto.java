package com.araw.araw.application.dto.participant;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfoDto {

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "\\+?[1-9]\\d{1,14}", message = "Invalid phone number")
    private String phoneNumber;

    private String alternativePhone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String country;
    private String preferredContactMethod;
    private String bestTimeToContact;
}
