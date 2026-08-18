package com.netknife.tools.digitalfootprint.dto;

public record GeoDto(String ipAddress, String country, String city, String isp, Double lat, Double lon) {
}
