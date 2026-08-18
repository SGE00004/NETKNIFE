package com.netknife.tools.wifirouteraudit;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import com.netknife.common.net.GatewayResolver;
import com.netknife.common.wifi.WifiEncryptionChecker;
import com.netknife.tools.wifirouteraudit.check.DefaultCredentialsCheck;
import com.netknife.tools.wifirouteraudit.check.WpsStatusCheck;
import com.netknife.tools.wifirouteraudit.dto.WifiRouterAuditReportDto;
import com.netknife.tools.wifirouteraudit.model.AuditCategoryResult;
import com.netknife.tools.wifirouteraudit.model.WifiRouterAuditReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class WifiRouterAuditService {

    private final WifiEncryptionChecker wifiEncryptionChecker;
    private final DefaultCredentialsCheck defaultCredentialsCheck;
    private final WpsStatusCheck wpsStatusCheck;
    private final GatewayResolver gatewayResolver;
    private final WifiRouterAuditReportRepository repository;

    public WifiRouterAuditService(
            WifiEncryptionChecker wifiEncryptionChecker,
            DefaultCredentialsCheck defaultCredentialsCheck,
            WpsStatusCheck wpsStatusCheck,
            GatewayResolver gatewayResolver,
            WifiRouterAuditReportRepository repository) {
        this.wifiEncryptionChecker = wifiEncryptionChecker;
        this.defaultCredentialsCheck = defaultCredentialsCheck;
        this.wpsStatusCheck = wpsStatusCheck;
        this.gatewayResolver = gatewayResolver;
        this.repository = repository;
    }

    @Transactional
    public WifiRouterAuditReportDto runAudit(String routerAddressOverride) {
        String routerAddress = resolveRouterAddress(routerAddressOverride);

        CheckResult wifiResult = wifiEncryptionChecker.check();
        CheckResult credentialsResult = defaultCredentialsCheck.check(routerAddress);
        CheckResult wpsResult = wpsStatusCheck.check();

        CheckStatus overall = CheckStatus.worstOf(wifiResult.status(), credentialsResult.status(), wpsResult.status());

        WifiRouterAuditReport report = new WifiRouterAuditReport(
                Instant.now(),
                routerAddress,
                toCategory(wifiResult),
                toCategory(credentialsResult),
                toCategory(wpsResult),
                overall);
        repository.save(report);
        return WifiRouterAuditReportDto.fromEntity(report);
    }

    @Transactional(readOnly = true)
    public Optional<WifiRouterAuditReportDto> getLastReport() {
        return repository.findTopByOrderByCheckedAtDesc().map(WifiRouterAuditReportDto::fromEntity);
    }

    private String resolveRouterAddress(String routerAddressOverride) {
        if (routerAddressOverride != null && !routerAddressOverride.isBlank()) {
            return routerAddressOverride.trim();
        }
        return gatewayResolver.resolveGatewayIp().orElse(null);
    }

    private AuditCategoryResult toCategory(CheckResult result) {
        return new AuditCategoryResult(result.status(), result.summary(), result.detail(), result.howToFix());
    }
}
