package com.netknife.tools.cryptojackingdetector.detection;

import com.netknife.tools.cryptojackingdetector.monitor.ProcessCpuSnapshot;
import com.netknife.tools.cryptojackingdetector.monitor.WindowsVisibleWindowChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CryptojackingDetectionServiceTest {

    @Mock
    private WindowsVisibleWindowChecker windowChecker;

    private CryptojackingDetectionService service;

    @BeforeEach
    void setUp() {
        lenient().when(windowChecker.pidsWithVisibleWindow()).thenReturn(Set.of());
        service = new CryptojackingDetectionService(windowChecker, new SuspiciousProcessTracker(), 60.0, 2);
    }

    private static ProcessCpuSnapshot snapshot(long pid, String name, double cpuPercent) {
        return new ProcessCpuSnapshot(pid, name, "C:\\fake\\" + name, name, cpuPercent, Instant.now());
    }

    @Test
    void aKnownMinerNameAlertsImmediatelyWithoutWaitingForSustainedReadings() {
        List<SuspicionResult> results = service.evaluate(List.of(snapshot(100, "xmrig.exe", 5.0)));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).reason()).isEqualTo(SuspicionReason.KNOWN_MINER_NAME);
    }

    @Test
    void highCpuIsNotSuspiciousOnTheFirstReading() {
        List<SuspicionResult> results = service.evaluate(List.of(snapshot(200, "misterioso.exe", 90.0)));

        assertThat(results).isEmpty();
    }

    @Test
    void highCpuSustainedAcrossReadingsWithoutAVisibleWindowIsSuspicious() {
        service.evaluate(List.of(snapshot(200, "misterioso.exe", 90.0)));

        List<SuspicionResult> results = service.evaluate(List.of(snapshot(200, "misterioso.exe", 90.0)));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).reason()).isEqualTo(SuspicionReason.SUSTAINED_HIGH_CPU_NO_WINDOW);
    }

    @Test
    void highCpuSustainedWithAVisibleWindowIsStillReportedButWithLowerConfidenceReason() {
        when(windowChecker.pidsWithVisibleWindow()).thenReturn(Set.of(300));
        service.evaluate(List.of(snapshot(300, "juego.exe", 90.0)));

        List<SuspicionResult> results = service.evaluate(List.of(snapshot(300, "juego.exe", 90.0)));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).reason()).isEqualTo(SuspicionReason.SUSTAINED_HIGH_CPU);
    }

    @Test
    void lowCpuIsNeverSuspicious() {
        service.evaluate(List.of(snapshot(400, "normal.exe", 10.0)));
        List<SuspicionResult> results = service.evaluate(List.of(snapshot(400, "normal.exe", 10.0)));

        assertThat(results).isEmpty();
    }
}
