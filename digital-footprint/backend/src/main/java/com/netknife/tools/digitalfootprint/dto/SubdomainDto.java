package com.netknife.tools.digitalfootprint.dto;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.digitalfootprint.model.DiscoveredSubdomainEntity;

public record SubdomainDto(String subdomain, String ipAddress, CheckStatus status) {
    public static SubdomainDto fromEntity(DiscoveredSubdomainEntity entity) {
        return new SubdomainDto(entity.getSubdomain(), entity.getIpAddress(), entity.getStatus());
    }
}
