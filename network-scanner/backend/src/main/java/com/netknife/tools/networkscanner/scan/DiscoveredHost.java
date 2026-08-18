package com.netknife.tools.networkscanner.scan;

public record DiscoveredHost(
        String ipAddress,
        String macAddress,
        String hostname
) {
}
