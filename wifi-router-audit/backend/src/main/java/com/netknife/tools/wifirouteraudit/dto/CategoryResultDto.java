package com.netknife.tools.wifirouteraudit.dto;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.wifirouteraudit.model.AuditCategoryResult;

public record CategoryResultDto(
        CheckStatus status,
        String summary,
        String detail,
        String howToFix
) {
    public static CategoryResultDto fromEntity(AuditCategoryResult result) {
        return new CategoryResultDto(result.getStatus(), result.getSummary(), result.getDetail(), result.getHowToFix());
    }
}
