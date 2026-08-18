package com.netknife.tools.networkscanner.blocking.dto;

import com.netknife.tools.networkscanner.blocking.BlockingCapabilityService.Capability;

public record BlockingCapabilityDto(
        boolean available,
        String reasonCode,
        String message
) {
    public static BlockingCapabilityDto fromCapability(Capability capability) {
        return new BlockingCapabilityDto(capability.available(), capability.reason().name(), capability.message());
    }
}
