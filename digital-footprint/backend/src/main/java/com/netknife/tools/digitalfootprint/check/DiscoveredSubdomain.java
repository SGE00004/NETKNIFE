package com.netknife.tools.digitalfootprint.check;

import com.netknife.common.dto.CheckStatus;

public record DiscoveredSubdomain(String subdomain, String ipAddress, CheckStatus status) {
}
