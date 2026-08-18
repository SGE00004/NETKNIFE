package com.netknife.tools.digitalfootprint.findings;

import com.netknife.common.dto.CheckStatus;
import com.netknife.common.findings.Finding;
import com.netknife.common.findings.FindingsSource;
import com.netknife.tools.digitalfootprint.DomainFootprintReportRepository;
import com.netknife.tools.digitalfootprint.FileMetadataReportRepository;
import com.netknife.tools.digitalfootprint.model.DiscoveredSubdomainEntity;
import com.netknife.tools.digitalfootprint.model.DomainFootprintReport;
import com.netknife.tools.digitalfootprint.model.FileMetadataFinding;
import com.netknife.tools.digitalfootprint.model.FileMetadataReport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DigitalFootprintFindingsSource implements FindingsSource {

    private final FileMetadataReportRepository fileMetadataReportRepository;
    private final DomainFootprintReportRepository domainFootprintReportRepository;

    public DigitalFootprintFindingsSource(
            FileMetadataReportRepository fileMetadataReportRepository,
            DomainFootprintReportRepository domainFootprintReportRepository) {
        this.fileMetadataReportRepository = fileMetadataReportRepository;
        this.domainFootprintReportRepository = domainFootprintReportRepository;
    }

    @Override
    public String moduleId() {
        return "digital-footprint";
    }

    @Override
    public String moduleLabel() {
        return "Detective de Huella Digital";
    }

    @Override
    public List<Finding> latestFindings() {
        List<Finding> findings = new ArrayList<>();

        fileMetadataReportRepository.findTopByOrderByAnalyzedAtDesc().ifPresent(report ->
                report.getFindings().stream()
                        .filter(finding -> finding.getStatus() != CheckStatus.OK)
                        .forEach(finding -> findings.add(toFileFinding(report, finding))));

        domainFootprintReportRepository.findTopByOrderByAnalyzedAtDesc().ifPresent(report ->
                report.getSubdomains().stream()
                        .filter(subdomain -> subdomain.getStatus() != CheckStatus.OK)
                        .forEach(subdomain -> findings.add(toSubdomainFinding(report, subdomain))));

        return findings;
    }

    private Finding toFileFinding(FileMetadataReport report, FileMetadataFinding finding) {
        return new Finding(
                moduleId(),
                moduleLabel(),
                "file-" + finding.getId(),
                finding.getCategory() + " expuesto en " + report.getOriginalFilename(),
                finding.getStatus(),
                finding.getExplanation(),
                finding.getValue(),
                null,
                report.getAnalyzedAt());
    }

    private Finding toSubdomainFinding(DomainFootprintReport report, DiscoveredSubdomainEntity subdomain) {
        return new Finding(
                moduleId(),
                moduleLabel(),
                "subdomain-" + subdomain.getId(),
                "Subdominio sensible encontrado: " + subdomain.getSubdomain(),
                subdomain.getStatus(),
                "Este subdominio existe y responde. Si no deberia ser publico, revisa su configuracion.",
                subdomain.getIpAddress(),
                null,
                report.getAnalyzedAt());
    }
}
