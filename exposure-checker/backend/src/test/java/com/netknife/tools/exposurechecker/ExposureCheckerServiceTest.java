package com.netknife.tools.exposurechecker;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.exposurechecker.check.OpenPortsChecker;
import com.netknife.tools.exposurechecker.check.UpnpChecker;
import com.netknife.common.wifi.WifiEncryptionChecker;
import com.netknife.tools.exposurechecker.dto.ExposureReportDto;
import com.netknife.tools.exposurechecker.model.ExposureReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExposureCheckerServiceTest {

    @Mock
    private WifiEncryptionChecker wifiEncryptionChecker;
    @Mock
    private OpenPortsChecker openPortsChecker;
    @Mock
    private UpnpChecker upnpChecker;
    @Mock
    private ExposureReportRepository repository;

    private ExposureCheckerService service;

    @BeforeEach
    void setUp() {
        service = new ExposureCheckerService(wifiEncryptionChecker, openPortsChecker, upnpChecker, repository);
        lenient().when(repository.save(any(ExposureReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void checkComputesOverallFromTheThreeCategories() {
        when(wifiEncryptionChecker.check()).thenReturn(CheckResult.ok("wifi ok"));
        when(openPortsChecker.check()).thenReturn(
                new CheckResult(CheckStatus.PELIGRO, "puerto abierto", "detalle", "arreglo"));
        when(upnpChecker.check()).thenReturn(CheckResult.ok("upnp ok"));

        ExposureReportDto dto = service.check();

        assertThat(dto.wifiEncryption().status()).isEqualTo(CheckStatus.OK);
        assertThat(dto.openPorts().status()).isEqualTo(CheckStatus.PELIGRO);
        assertThat(dto.services().status()).isEqualTo(CheckStatus.OK);
        assertThat(dto.overallStatus()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void getLastReportReturnsEmptyWhenNeverChecked() {
        when(repository.findTopByOrderByCheckedAtDesc()).thenReturn(Optional.empty());

        assertThat(service.getLastReport()).isEmpty();
    }
}
