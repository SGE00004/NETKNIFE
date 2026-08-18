package com.netknife.tools.exposurechecker.dto;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.exposurechecker.model.CategoryResult;

public record CategoryResultDto(
        CheckStatus status,
        String summary,
        String detail,
        String howToFix
) {
    public static CategoryResultDto fromEntity(CategoryResult result) {
        return new CategoryResultDto(result.getStatus(), result.getSummary(), result.getDetail(), result.getHowToFix());
    }
}
