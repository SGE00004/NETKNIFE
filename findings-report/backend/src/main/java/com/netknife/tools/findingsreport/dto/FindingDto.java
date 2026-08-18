package com.netknife.tools.findingsreport.dto;

import com.netknife.common.dto.CheckStatus;
import com.netknife.common.dto.RiskLevel;

import java.time.Instant;

public record FindingDto(
        String sourceModuleId,
        String sourceModuleLabel,
        String id,
        String title,
        CheckStatus status,
        RiskLevel riskLevel,
        String summary,
        String detail,
        String howToFix,
        Instant detectedAt,
        RelatedToolDto relatedTool
) {
}
