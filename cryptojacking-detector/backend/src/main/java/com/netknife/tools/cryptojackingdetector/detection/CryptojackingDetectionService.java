package com.netknife.tools.cryptojackingdetector.detection;

import com.netknife.tools.cryptojackingdetector.monitor.KnownMinerProcessNames;
import com.netknife.tools.cryptojackingdetector.monitor.ProcessCpuSnapshot;
import com.netknife.tools.cryptojackingdetector.monitor.WindowsVisibleWindowChecker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Combina tres señales para decidir si un proceso es sospechoso de minar
 * criptomonedas oculto: nombre de ejecutable conocido (alta confianza, alerta
 * inmediata), o CPU alta sostenida durante varias lecturas seguidas combinada con
 * si el proceso tiene o no una ventana visible (sin ventana es la señal mas tipica
 * de mineria oculta; con ventana se sigue reportando pero con menos confianza,
 * puede ser una app que el usuario esta usando activamente).
 */
@Component
public class CryptojackingDetectionService {

    private final WindowsVisibleWindowChecker windowChecker;
    private final SuspiciousProcessTracker tracker;
    private final double cpuThresholdPercent;
    private final int sustainedReadingsRequired;

    public CryptojackingDetectionService(
            WindowsVisibleWindowChecker windowChecker,
            SuspiciousProcessTracker tracker,
            @Value("${netknife.cryptojacking.cpu-threshold-percent:60.0}") double cpuThresholdPercent,
            @Value("${netknife.cryptojacking.sustained-readings-required:3}") int sustainedReadingsRequired) {
        this.windowChecker = windowChecker;
        this.tracker = tracker;
        this.cpuThresholdPercent = cpuThresholdPercent;
        this.sustainedReadingsRequired = sustainedReadingsRequired;
    }

    public List<SuspicionResult> evaluate(List<ProcessCpuSnapshot> snapshots) {
        tracker.retainOnly(snapshots.stream().map(ProcessCpuSnapshot::pid).collect(Collectors.toSet()));
        Set<Integer> visibleWindowPids = windowChecker.pidsWithVisibleWindow();

        List<SuspicionResult> results = new ArrayList<>();
        for (ProcessCpuSnapshot snapshot : snapshots) {
            if (KnownMinerProcessNames.isKnownMinerName(snapshot.name())) {
                results.add(new SuspicionResult(snapshot.pid(), snapshot.name(), snapshot.path(),
                        SuspicionReason.KNOWN_MINER_NAME, snapshot.cpuPercent()));
                continue;
            }

            boolean aboveThreshold = snapshot.cpuPercent() >= cpuThresholdPercent;
            boolean sustained = tracker.recordReadingAndCheckSustained(
                    snapshot.pid(), aboveThreshold, sustainedReadingsRequired);
            if (!sustained) {
                continue;
            }
            boolean hasVisibleWindow = visibleWindowPids.contains((int) snapshot.pid());
            SuspicionReason reason = hasVisibleWindow
                    ? SuspicionReason.SUSTAINED_HIGH_CPU
                    : SuspicionReason.SUSTAINED_HIGH_CPU_NO_WINDOW;
            results.add(new SuspicionResult(snapshot.pid(), snapshot.name(), snapshot.path(), reason, snapshot.cpuPercent()));
        }
        return results;
    }
}
