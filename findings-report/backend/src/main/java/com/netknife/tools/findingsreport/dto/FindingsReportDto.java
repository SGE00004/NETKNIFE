package com.netknife.tools.findingsreport.dto;

import com.netknife.common.dto.RiskLevel;

import java.time.Instant;
import java.util.List;

public record FindingsReportDto(
        Instant generatedAt,
        int totalFindings,
        int lowRiskCount,
        int mediumRiskCount,
        int highRiskCount,
        RiskLevel overallRisk,
        List<FindingDto> findings,
        List<ModuleRefDto> modulesWithoutData
) {
}
