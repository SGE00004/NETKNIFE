package com.netknife.tools.digitalfootprint.dto;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.digitalfootprint.model.FileMetadataReport;

import java.time.Instant;
import java.util.List;

public record FileMetadataReportDto(
        Long id,
        Instant analyzedAt,
        String originalFilename,
        String fileType,
        CheckStatus overallStatus,
        List<MetadataFindingDto> findings
) {
    public static FileMetadataReportDto fromEntity(FileMetadataReport report) {
        return new FileMetadataReportDto(
                report.getId(),
                report.getAnalyzedAt(),
                report.getOriginalFilename(),
                report.getFileType(),
                report.getOverallStatus(),
                report.getFindings().stream().map(MetadataFindingDto::fromEntity).toList());
    }
}
