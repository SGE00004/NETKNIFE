package com.netknife.tools.exposurechecker.dto;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.exposurechecker.model.ExposureReport;

import java.time.Instant;

public record ExposureReportDto(
        Long id,
        Instant checkedAt,
        CategoryResultDto wifiEncryption,
        CategoryResultDto openPorts,
        CategoryResultDto services,
        CheckStatus overallStatus
) {
    public static ExposureReportDto fromEntity(ExposureReport report) {
        return new ExposureReportDto(
                report.getId(),
                report.getCheckedAt(),
                CategoryResultDto.fromEntity(report.getWifiEncryption()),
                CategoryResultDto.fromEntity(report.getOpenPorts()),
                CategoryResultDto.fromEntity(report.getServices()),
                report.getOverallStatus()
        );
    }
}
