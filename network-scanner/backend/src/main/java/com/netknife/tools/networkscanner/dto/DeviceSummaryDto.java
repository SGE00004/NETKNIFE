package com.netknife.tools.networkscanner.dto;

import com.netknife.common.dto.RiskLevel;

public record DeviceSummaryDto(
        int totalDevices,
        int unrecognizedDevices,
        int newSinceLastScan,
        RiskLevel riskLevel
) {
}
