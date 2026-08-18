package com.netknife.tools.portradar;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.portradar.check.CuratedPortCatalog;
import com.netknife.tools.portradar.check.PortScanOutcome;
import com.netknife.tools.portradar.check.PortScanner;
import com.netknife.tools.portradar.dto.PortScanReportDto;
import com.netknife.tools.portradar.model.PortScanFinding;
import com.netknife.tools.portradar.model.PortScanReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PortRadarService {

    private final PortScanner portScanner;
    private final PortScanReportRepository repository;

    public PortRadarService(PortScanner portScanner, PortScanReportRepository repository) {
        this.portScanner = portScanner;
        this.repository = repository;
    }

    @Transactional
    public PortScanReportDto scan(String target) {
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("Indica un host o direccion IP para escanear.");
        }
        String trimmedTarget = target.trim();

        List<PortScanOutcome> outcomes = portScanner.scan(trimmedTarget);
        CheckStatus overall = outcomes.isEmpty()
                ? CheckStatus.OK
                : CheckStatus.worstOf(outcomes.stream().map(PortScanOutcome::status).toList());

        PortScanReport report = new PortScanReport(
                trimmedTarget, Instant.now(), CuratedPortCatalog.all().size(), overall);
        outcomes.forEach(outcome -> report.addOpenPort(new PortScanFinding(
                outcome.port(), outcome.protocolLabel(), outcome.banner(),
                outcome.status(), outcome.summary(), outcome.howToFix())));

        repository.save(report);
        return PortScanReportDto.fromEntity(report);
    }

    @Transactional(readOnly = true)
    public Optional<PortScanReportDto> getLastReport() {
        return repository.findTopByOrderByScannedAtDesc().map(PortScanReportDto::fromEntity);
    }
}
