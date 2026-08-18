package com.netknife.tools.digitalfootprint.dto;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.digitalfootprint.model.FileMetadataFinding;

public record MetadataFindingDto(
        String category,
        String value,
        CheckStatus status,
        String explanation
) {
    public static MetadataFindingDto fromEntity(FileMetadataFinding finding) {
        return new MetadataFindingDto(finding.getCategory(), finding.getValue(), finding.getStatus(), finding.getExplanation());
    }
}
