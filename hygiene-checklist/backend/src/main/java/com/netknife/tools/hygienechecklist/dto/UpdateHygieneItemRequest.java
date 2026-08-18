package com.netknife.tools.hygienechecklist.dto;

import com.netknife.common.dto.CheckStatus;

public record UpdateHygieneItemRequest(CheckStatus status) {
}
