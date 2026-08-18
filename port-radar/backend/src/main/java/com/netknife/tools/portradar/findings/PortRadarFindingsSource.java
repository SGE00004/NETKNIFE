package com.netknife.tools.portradar.findings;

import com.netknife.common.findings.Finding;
import com.netknife.common.findings.FindingsSource;
import com.netknife.tools.portradar.PortScanReportRepository;
import com.netknife.tools.portradar.model.PortScanFinding;
import com.netknife.tools.portradar.model.PortScanReport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PortRadarFindingsSource implements FindingsSource {

    private final PortScanReportRepository repository;

    public PortRadarFindingsSource(PortScanReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public String moduleId() {
        return "port-radar";
    }

    @Override
    public String moduleLabel() {
        return "Radar de Puertos";
    }

    @Override
    public List<Finding> latestFindings() {
        Optional<PortScanReport> lastReport = repository.findTopByOrderByScannedAtDesc();
        if (lastReport.isEmpty()) {
            return List.of();
        }
        PortScanReport report = lastReport.get();
        return report.getOpenPorts().stream()
                .map(finding -> toFinding(report, finding))
                .toList();
    }

    private Finding toFinding(PortScanReport report, PortScanFinding finding) {
        return new Finding(
                moduleId(),
                moduleLabel(),
                "port-" + finding.getPort(),
                "Puerto " + finding.getPort() + " abierto (" + finding.getProtocolLabel() + ") en " + report.getTarget(),
                finding.getStatus(),
                finding.getSummary(),
                finding.getBanner(),
                finding.getHowToFix(),
                report.getScannedAt());
    }
}
