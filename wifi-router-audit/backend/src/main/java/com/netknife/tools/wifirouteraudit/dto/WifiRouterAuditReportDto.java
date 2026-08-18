package com.netknife.tools.wifirouteraudit.dto;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.wifirouteraudit.model.WifiRouterAuditReport;

import java.time.Instant;

public record WifiRouterAuditReportDto(
        Long id,
        Instant checkedAt,
        String routerAddress,
        CategoryResultDto wifiEncryption,
        CategoryResultDto defaultCredentials,
        CategoryResultDto wps,
        CheckStatus overallStatus
) {
    public static WifiRouterAuditReportDto fromEntity(WifiRouterAuditReport report) {
        return new WifiRouterAuditReportDto(
                report.getId(),
                report.getCheckedAt(),
                report.getRouterAddress(),
                CategoryResultDto.fromEntity(report.getWifiEncryption()),
                CategoryResultDto.fromEntity(report.getDefaultCredentials()),
                CategoryResultDto.fromEntity(report.getWps()),
                report.getOverallStatus());
    }
}
