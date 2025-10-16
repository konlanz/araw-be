package com.araw.araw.domain.event.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Column(name = "venue_name")
    private String venueName;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state_province")
    private String stateProvince;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "building_name")
    private String buildingName;

    @Column(name = "parking_info", columnDefinition = "TEXT")
    private String parkingInfo;

    @Column(name = "accessibility_info", columnDefinition = "TEXT")
    private String accessibilityInfo;

    @Column(name = "virtual_meeting_url")
    private String virtualMeetingUrl;

    @Column(name = "virtual_meeting_password")
    private String virtualMeetingPassword;

    @Column(name = "is_virtual")
    private Boolean isVirtual = false;

    @Column(name = "is_hybrid")
    private Boolean isHybrid = false;

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) sb.append(addressLine1).append(", ");
        if (addressLine2 != null) sb.append(addressLine2).append(", ");
        if (city != null) sb.append(city).append(", ");
        if (stateProvince != null) sb.append(stateProvince).append(" ");
        if (postalCode != null) sb.append(postalCode).append(", ");
        if (country != null) sb.append(country);
        return sb.toString().replaceAll(", $", "");
    }
}
