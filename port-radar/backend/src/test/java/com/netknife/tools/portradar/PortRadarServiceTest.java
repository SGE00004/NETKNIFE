package com.netknife.tools.portradar;

import com.netknife.tools.portradar.check.PortScanner;
import com.netknife.tools.portradar.model.PortScanReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortRadarServiceTest {

    @Mock
    private PortScanner portScanner;
    @Mock
    private PortScanReportRepository repository;

    private PortRadarService service;

    @BeforeEach
    void setUp() {
        service = new PortRadarService(portScanner, repository);
        lenient().when(repository.save(any(PortScanReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void rejectsABlankTarget() {
        assertThatThrownBy(() -> service.scan("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void noOpenPortsMeansOverallOk() {
        when(portScanner.scan(anyString())).thenReturn(List.of());

        var dto = service.scan("127.0.0.1");

        assertThat(dto.openPorts()).isEmpty();
        assertThat(dto.overallStatus().name()).isEqualTo("OK");
    }

    @Test
    void getLastReportReturnsEmptyWhenNeverScanned() {
        when(repository.findTopByOrderByScannedAtDesc()).thenReturn(Optional.empty());

        assertThat(service.getLastReport()).isEmpty();
    }
}
