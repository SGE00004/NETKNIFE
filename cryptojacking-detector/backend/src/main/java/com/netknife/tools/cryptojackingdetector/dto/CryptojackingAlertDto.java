package com.netknife.tools.cryptojackingdetector.dto;

import com.netknife.tools.cryptojackingdetector.alert.model.CryptojackingAlert;

import java.time.Instant;

public record CryptojackingAlertDto(
        Long id,
        long pid,
        String processName,
        String processPath,
        String reason,
        double peakCpuPercent,
        Instant detectedAt,
        Instant resolvedAt,
        String resolution
) {
    public static CryptojackingAlertDto fromEntity(CryptojackingAlert alert) {
        return new CryptojackingAlertDto(
                alert.getId(),
                alert.getPid(),
                alert.getProcessName(),
                alert.getProcessPath(),
                alert.getReason().name(),
                alert.getPeakCpuPercent(),
                alert.getDetectedAt(),
                alert.getResolvedAt(),
                alert.getResolution() == null ? null : alert.getResolution().name());
    }
}
