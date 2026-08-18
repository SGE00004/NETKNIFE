package com.netknife.tools.exposurechecker;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import com.netknife.common.wifi.WifiEncryptionChecker;
import com.netknife.tools.exposurechecker.check.OpenPortsChecker;
import com.netknife.tools.exposurechecker.check.UpnpChecker;
import com.netknife.tools.exposurechecker.dto.ExposureReportDto;
import com.netknife.tools.exposurechecker.model.CategoryResult;
import com.netknife.tools.exposurechecker.model.ExposureReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ExposureCheckerService {

    private final WifiEncryptionChecker wifiEncryptionChecker;
    private final OpenPortsChecker openPortsChecker;
    private final UpnpChecker upnpChecker;
    private final ExposureReportRepository repository;

    public ExposureCheckerService(
            WifiEncryptionChecker wifiEncryptionChecker,
            OpenPortsChecker openPortsChecker,
            UpnpChecker upnpChecker,
            ExposureReportRepository repository) {
        this.wifiEncryptionChecker = wifiEncryptionChecker;
        this.openPortsChecker = openPortsChecker;
        this.upnpChecker = upnpChecker;
        this.repository = repository;
    }

    @Transactional
    public ExposureReportDto check() {
        CheckResult wifiResult = wifiEncryptionChecker.check();
        CheckResult portsResult = openPortsChecker.check();
        CheckResult servicesResult = upnpChecker.check();

        CheckStatus overall = CheckStatus.worstOf(wifiResult.status(), portsResult.status(), servicesResult.status());

        ExposureReport report = new ExposureReport(
                Instant.now(),
                toCategory(wifiResult),
                toCategory(portsResult),
                toCategory(servicesResult),
                overall);
        repository.save(report);
        return ExposureReportDto.fromEntity(report);
    }

    @Transactional(readOnly = true)
    public Optional<ExposureReportDto> getLastReport() {
        return repository.findTopByOrderByCheckedAtDesc().map(ExposureReportDto::fromEntity);
    }

    private CategoryResult toCategory(CheckResult result) {
        return new CategoryResult(result.status(), result.summary(), result.detail(), result.howToFix());
    }
}
