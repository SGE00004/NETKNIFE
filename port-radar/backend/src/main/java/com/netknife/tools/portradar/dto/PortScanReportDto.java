package com.netknife.tools.portradar.dto;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.portradar.model.PortScanReport;

import java.time.Instant;
import java.util.List;

public record PortScanReportDto(
        Long id,
        String target,
        Instant scannedAt,
        int portsScanned,
        int openPortsCount,
        CheckStatus overallStatus,
        List<PortFindingDto> openPorts
) {
    public static PortScanReportDto fromEntity(PortScanReport report) {
        List<PortFindingDto> openPorts = report.getOpenPorts().stream()
                .map(PortFindingDto::fromEntity)
                .toList();
        return new PortScanReportDto(
                report.getId(),
                report.getTarget(),
                report.getScannedAt(),
                report.getPortsScanned(),
                openPorts.size(),
                report.getOverallStatus(),
                openPorts);
    }
}
