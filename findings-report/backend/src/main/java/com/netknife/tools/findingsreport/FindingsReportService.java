package com.netknife.tools.findingsreport;

import com.netknife.common.dto.CheckStatus;
import com.netknife.common.dto.RiskLevel;
import com.netknife.common.findings.Finding;
import com.netknife.common.findings.FindingsSource;
import com.netknife.tools.findingsreport.dto.FindingDto;
import com.netknife.tools.findingsreport.dto.FindingsReportDto;
import com.netknife.tools.findingsreport.dto.ModuleRefDto;
import com.netknife.tools.findingsreport.dto.RelatedToolDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class FindingsReportService {

    private final List<FindingsSource> sources;

    /**
     * Spring inyecta aqui automaticamente todos los beans que implementan
     * FindingsSource (uno por modulo del equipo rojo): anadir un modulo rojo
     * nuevo en el futuro es tan simple como implementar la interfaz y
     * anotarla @Component, sin tocar este servicio.
     */
    public FindingsReportService(List<FindingsSource> sources) {
        this.sources = sources;
    }

    public FindingsReportDto buildReport() {
        List<Finding> allFindings = sources.stream()
                .flatMap(source -> source.latestFindings().stream())
                .toList();

        List<ModuleRefDto> modulesWithoutData = sources.stream()
                .filter(source -> source.latestFindings().isEmpty())
                .map(source -> new ModuleRefDto(source.moduleId(), source.moduleLabel()))
                .toList();

        List<FindingDto> findingDtos = allFindings.stream().map(this::toDto).toList();

        int low = countByRisk(findingDtos, RiskLevel.VERDE);
        int medium = countByRisk(findingDtos, RiskLevel.AMARILLO);
        int high = countByRisk(findingDtos, RiskLevel.ROJO);
        RiskLevel overallRisk = high > 0 ? RiskLevel.ROJO : medium > 0 ? RiskLevel.AMARILLO : RiskLevel.VERDE;

        return new FindingsReportDto(
                Instant.now(), findingDtos.size(), low, medium, high, overallRisk, findingDtos, modulesWithoutData);
    }

    private FindingDto toDto(Finding finding) {
        RiskLevel riskLevel = RiskLevel.fromCheckStatus(finding.status());
        RelatedToolDto relatedTool = finding.status() == CheckStatus.PELIGRO
                ? new RelatedToolDto("incident-guide", "Ver que hacer en la Guia de Respuesta a Incidentes")
                : new RelatedToolDto(finding.sourceModuleId(), "Ver detalle en " + finding.sourceModuleLabel());

        return new FindingDto(
                finding.sourceModuleId(),
                finding.sourceModuleLabel(),
                finding.id(),
                finding.title(),
                finding.status(),
                riskLevel,
                finding.summary(),
                finding.detail(),
                finding.howToFix(),
                finding.detectedAt(),
                relatedTool);
    }

    private int countByRisk(List<FindingDto> findings, RiskLevel level) {
        return (int) findings.stream().filter(f -> f.riskLevel() == level).count();
    }
}
