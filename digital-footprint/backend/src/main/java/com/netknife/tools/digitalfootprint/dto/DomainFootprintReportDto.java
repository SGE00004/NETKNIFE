package com.netknife.tools.digitalfootprint.dto;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.digitalfootprint.model.DomainFootprintReport;

import java.time.Instant;
import java.util.List;

public record DomainFootprintReportDto(
        Long id,
        Instant analyzedAt,
        String domain,
        List<SubdomainDto> subdomains,
        WhoisDto whois,
        GeoDto geo,
        CheckStatus overallStatus
) {
    public static DomainFootprintReportDto fromEntity(DomainFootprintReport report) {
        WhoisDto whois = report.getWhoisRegistrar() == null && report.getWhoisCreatedDate() == null && report.getWhoisExpiresDate() == null
                ? null
                : new WhoisDto(report.getWhoisRegistrar(), report.getWhoisCreatedDate(), report.getWhoisExpiresDate());
        GeoDto geo = report.getGeoIp() == null
                ? null
                : new GeoDto(report.getGeoIp(), report.getGeoCountry(), report.getGeoCity(), report.getGeoIsp(),
                        report.getGeoLat(), report.getGeoLon());
        return new DomainFootprintReportDto(
                report.getId(),
                report.getAnalyzedAt(),
                report.getDomain(),
                report.getSubdomains().stream().map(SubdomainDto::fromEntity).toList(),
                whois,
                geo,
                report.getOverallStatus());
    }
}
