package com.netknife.tools.wifirouteraudit;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import com.netknife.common.net.GatewayResolver;
import com.netknife.common.wifi.WifiEncryptionChecker;
import com.netknife.tools.wifirouteraudit.check.DefaultCredentialsCheck;
import com.netknife.tools.wifirouteraudit.check.WpsStatusCheck;
import com.netknife.tools.wifirouteraudit.dto.WifiRouterAuditReportDto;
import com.netknife.tools.wifirouteraudit.model.WifiRouterAuditReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WifiRouterAuditServiceTest {

    @Mock
    private WifiEncryptionChecker wifiEncryptionChecker;
    @Mock
    private DefaultCredentialsCheck defaultCredentialsCheck;
    @Mock
    private WpsStatusCheck wpsStatusCheck;
    @Mock
    private GatewayResolver gatewayResolver;
    @Mock
    private WifiRouterAuditReportRepository repository;

    private WifiRouterAuditService service;

    @BeforeEach
    void setUp() {
        service = new WifiRouterAuditService(
                wifiEncryptionChecker, defaultCredentialsCheck, wpsStatusCheck, gatewayResolver, repository);
        lenient().when(repository.save(any(WifiRouterAuditReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(wifiEncryptionChecker.check()).thenReturn(CheckResult.ok("wifi ok"));
        lenient().when(wpsStatusCheck.check()).thenReturn(
                new CheckResult(CheckStatus.NO_VERIFICABLE, "no verificable", null, "guia"));
    }

    @Test
    void usesTheExplicitRouterAddressWithoutAutodetecting() {
        when(defaultCredentialsCheck.check("192.168.50.1")).thenReturn(CheckResult.ok("credenciales ok"));

        WifiRouterAuditReportDto dto = service.runAudit("192.168.50.1");

        assertThat(dto.routerAddress()).isEqualTo("192.168.50.1");
        verify(gatewayResolver, org.mockito.Mockito.never()).resolveGatewayIp();
    }

    @Test
    void autodetectsTheGatewayWhenNoAddressIsGiven() {
        when(gatewayResolver.resolveGatewayIp()).thenReturn(Optional.of("192.168.1.1"));
        when(defaultCredentialsCheck.check(anyString())).thenReturn(CheckResult.ok("credenciales ok"));

        WifiRouterAuditReportDto dto = service.runAudit(null);

        assertThat(dto.routerAddress()).isEqualTo("192.168.1.1");
    }

    @Test
    void overallStatusIsTheWorstOfTheThreeCategories() {
        when(defaultCredentialsCheck.check(anyString())).thenReturn(
                new CheckResult(CheckStatus.PELIGRO, "credencial de fabrica valida", "detalle", "arreglo"));

        WifiRouterAuditReportDto dto = service.runAudit("192.168.1.1");

        assertThat(dto.defaultCredentials().status()).isEqualTo(CheckStatus.PELIGRO);
        assertThat(dto.overallStatus()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void getLastReportReturnsEmptyWhenNeverAudited() {
        when(repository.findTopByOrderByCheckedAtDesc()).thenReturn(Optional.empty());

        assertThat(service.getLastReport()).isEmpty();
    }
}
