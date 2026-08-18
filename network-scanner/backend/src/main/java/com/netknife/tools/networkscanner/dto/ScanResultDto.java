package com.netknife.tools.networkscanner.dto;

import com.netknife.common.dto.RiskLevel;

import java.time.Instant;
import java.util.List;

public record ScanResultDto(
        Instant scannedAt,
        String subnetScanned,
        String networkInterfaceName,
        int totalDevices,
        int unrecognizedDevices,
        int newDevicesThisScan,
        RiskLevel riskLevel,
        List<NetworkDeviceDto> devices
) {
}
