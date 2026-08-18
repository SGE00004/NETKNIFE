package com.netknife.tools.hygienechecklist.dto;

import com.netknife.common.dto.CheckStatus;

import java.time.Instant;

public record HygieneItemDto(
        String id,
        String title,
        String whyItMatters,
        boolean automatic,
        CheckStatus status,
        String detail,
        String howToFix,
        Instant lastUpdated
) {
}
