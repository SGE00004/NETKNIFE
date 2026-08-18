package com.netknife.tools.hygienechecklist.dto;

import com.netknife.common.dto.CheckStatus;

import java.util.List;

public record HygieneChecklistDto(
        List<HygieneItemDto> items,
        int totalItems,
        int itemsInGoodShape,
        CheckStatus overallStatus
) {
}
