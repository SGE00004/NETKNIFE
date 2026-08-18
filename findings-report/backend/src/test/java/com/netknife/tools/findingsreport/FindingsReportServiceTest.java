package com.netknife.tools.findingsreport;

import com.netknife.common.dto.CheckStatus;
import com.netknife.common.dto.RiskLevel;
import com.netknife.common.findings.Finding;
import com.netknife.common.findings.FindingsSource;
import com.netknife.tools.findingsreport.dto.FindingsReportDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FindingsReportServiceTest {

    private static Finding finding(String moduleId, String moduleLabel, CheckStatus status) {
        return new Finding(moduleId, moduleLabel, "id-" + status, "titulo", status, "resumen", null, "arreglo", Instant.now());
    }

    private static FindingsSource sourceWith(String moduleId, String moduleLabel, List<Finding> findings) {
        return new FindingsSource() {
            @Override
            public String moduleId() {
                return moduleId;
            }

            @Override
            public String moduleLabel() {
                return moduleLabel;
            }

            @Override
            public List<Finding> latestFindings() {
                return findings;
            }
        };
    }

    @Test
    void aggregatesFindingsFromAllSources() {
        FindingsSource portRadar = sourceWith("port-radar", "Radar de Puertos",
                List.of(finding("port-radar", "Radar de Puertos", CheckStatus.PELIGRO)));
        FindingsSource wifiAudit = sourceWith("wifi-router-audit", "Auditor Wi-Fi/Router",
                List.of(finding("wifi-router-audit", "Auditor Wi-Fi/Router", CheckStatus.ATENCION)));

        FindingsReportService service = new FindingsReportService(List.of(portRadar, wifiAudit));
        FindingsReportDto report = service.buildReport();

        assertThat(report.totalFindings()).isEqualTo(2);
        assertThat(report.highRiskCount()).isEqualTo(1);
        assertThat(report.mediumRiskCount()).isEqualTo(1);
        assertThat(report.overallRisk()).isEqualTo(RiskLevel.ROJO);
        assertThat(report.modulesWithoutData()).isEmpty();
    }

    @Test
    void listsModulesWithoutDataYet() {
        FindingsSource emptySource = sourceWith("digital-footprint", "Detective de Huella Digital", List.of());

        FindingsReportService service = new FindingsReportService(List.of(emptySource));
        FindingsReportDto report = service.buildReport();

        assertThat(report.totalFindings()).isZero();
        assertThat(report.overallRisk()).isEqualTo(RiskLevel.VERDE);
        assertThat(report.modulesWithoutData()).extracting("id").containsExactly("digital-footprint");
        assertThat(report.modulesWithoutData()).extracting("label").containsExactly("Detective de Huella Digital");
    }

    @Test
    void aDangerFindingLinksToTheIncidentGuide() {
        FindingsSource source = sourceWith("port-radar", "Radar de Puertos",
                List.of(finding("port-radar", "Radar de Puertos", CheckStatus.PELIGRO)));

        FindingsReportService service = new FindingsReportService(List.of(source));
        FindingsReportDto report = service.buildReport();

        assertThat(report.findings().get(0).relatedTool().toolId()).isEqualTo("incident-guide");
    }

    @Test
    void anOkFindingLinksBackToItsOwnModule() {
        FindingsSource source = sourceWith("port-radar", "Radar de Puertos",
                List.of(finding("port-radar", "Radar de Puertos", CheckStatus.OK)));

        FindingsReportService service = new FindingsReportService(List.of(source));
        FindingsReportDto report = service.buildReport();

        assertThat(report.findings().get(0).relatedTool().toolId()).isEqualTo("port-radar");
    }
}
