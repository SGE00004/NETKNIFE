package com.netknife.tools.digitalfootprint;

import com.netknife.common.dto.CheckStatus;
import com.netknife.common.geo.GeoLocation;
import com.netknife.common.geo.IpGeolocationLookup;
import com.netknife.tools.digitalfootprint.check.DiscoveredSubdomain;
import com.netknife.tools.digitalfootprint.check.DomainWhoisLookup;
import com.netknife.tools.digitalfootprint.check.SubdomainDiscoveryService;
import com.netknife.tools.digitalfootprint.check.WhoisSummary;
import com.netknife.tools.digitalfootprint.dto.DomainFootprintReportDto;
import com.netknife.tools.digitalfootprint.model.DiscoveredSubdomainEntity;
import com.netknife.tools.digitalfootprint.model.DomainFootprintReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class DomainFootprintService {

    private final SubdomainDiscoveryService subdomainDiscoveryService;
    private final DomainWhoisLookup domainWhoisLookup;
    private final IpGeolocationLookup ipGeolocationLookup;
    private final DomainFootprintReportRepository repository;

    public DomainFootprintService(
            SubdomainDiscoveryService subdomainDiscoveryService,
            DomainWhoisLookup domainWhoisLookup,
            IpGeolocationLookup ipGeolocationLookup,
            DomainFootprintReportRepository repository) {
        this.subdomainDiscoveryService = subdomainDiscoveryService;
        this.domainWhoisLookup = domainWhoisLookup;
        this.ipGeolocationLookup = ipGeolocationLookup;
        this.repository = repository;
    }

    @Transactional
    public DomainFootprintReportDto analyze(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("Indica un dominio para analizar (ej. ejemplo.com).");
        }
        String trimmedDomain = domain.trim();

        List<DiscoveredSubdomain> subdomains = subdomainDiscoveryService.discover(trimmedDomain);
        Optional<WhoisSummary> whois = domainWhoisLookup.lookup(trimmedDomain);
        Optional<GeoLocation> geo = ipGeolocationLookup.lookup(trimmedDomain);

        CheckStatus overall = CheckStatus.worstOf(subdomains.stream().map(DiscoveredSubdomain::status).toList());

        DomainFootprintReport report = new DomainFootprintReport(
                Instant.now(),
                trimmedDomain,
                whois.map(WhoisSummary::registrar).orElse(null),
                whois.map(WhoisSummary::createdDate).orElse(null),
                whois.map(WhoisSummary::expiresDate).orElse(null),
                geo.map(GeoLocation::ipAddress).orElse(null),
                geo.map(GeoLocation::country).orElse(null),
                geo.map(GeoLocation::city).orElse(null),
                geo.map(GeoLocation::isp).orElse(null),
                geo.map(GeoLocation::lat).orElse(null),
                geo.map(GeoLocation::lon).orElse(null),
                overall);
        subdomains.forEach(subdomain -> report.addSubdomain(
                new DiscoveredSubdomainEntity(subdomain.subdomain(), subdomain.ipAddress(), subdomain.status())));

        repository.save(report);
        return DomainFootprintReportDto.fromEntity(report);
    }

    @Transactional(readOnly = true)
    public Optional<DomainFootprintReportDto> getLastReport() {
        return repository.findTopByOrderByAnalyzedAtDesc().map(DomainFootprintReportDto::fromEntity);
    }
}
