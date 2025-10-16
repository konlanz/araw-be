package com.araw.araw.application.dto.event;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    private String venueName;

    @NotBlank(message = "Address is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State/Province is required")
    private String stateProvince;

    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country;

    private Double latitude;
    private Double longitude;
    private String roomNumber;
    private String buildingName;
    private String parkingInfo;
    private String accessibilityInfo;
    private String virtualMeetingUrl;
    private String virtualMeetingPassword;
    private Boolean isVirtual;
    private Boolean isHybrid;
}
