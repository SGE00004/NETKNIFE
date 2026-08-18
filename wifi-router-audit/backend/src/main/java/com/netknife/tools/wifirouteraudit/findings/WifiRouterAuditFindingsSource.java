package com.netknife.tools.wifirouteraudit.findings;

import com.netknife.common.findings.Finding;
import com.netknife.common.findings.FindingsSource;
import com.netknife.tools.wifirouteraudit.WifiRouterAuditReportRepository;
import com.netknife.tools.wifirouteraudit.model.AuditCategoryResult;
import com.netknife.tools.wifirouteraudit.model.WifiRouterAuditReport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class WifiRouterAuditFindingsSource implements FindingsSource {

    private final WifiRouterAuditReportRepository repository;

    public WifiRouterAuditFindingsSource(WifiRouterAuditReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public String moduleId() {
        return "wifi-router-audit";
    }

    @Override
    public String moduleLabel() {
        return "Auditor Wi-Fi/Router";
    }

    @Override
    public List<Finding> latestFindings() {
        Optional<WifiRouterAuditReport> lastReport = repository.findTopByOrderByCheckedAtDesc();
        if (lastReport.isEmpty()) {
            return List.of();
        }
        WifiRouterAuditReport report = lastReport.get();
        return List.of(
                toFinding(report, "wifi-encryption", "Cifrado de tu red WiFi", report.getWifiEncryption()),
                toFinding(report, "default-credentials", "Credenciales del panel del router", report.getDefaultCredentials()),
                toFinding(report, "wps", "Estado de WPS", report.getWps()));
    }

    private Finding toFinding(WifiRouterAuditReport report, String id, String title, AuditCategoryResult result) {
        return new Finding(
                moduleId(),
                moduleLabel(),
                id,
                title,
                result.getStatus(),
                result.getSummary(),
                result.getDetail(),
                result.getHowToFix(),
                report.getCheckedAt());
    }
}
