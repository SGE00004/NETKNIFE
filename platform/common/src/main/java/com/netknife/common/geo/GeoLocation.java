package com.netknife.common.geo;

public record GeoLocation(String ipAddress, String country, String city, String isp, String org, Double lat, Double lon) {
}
