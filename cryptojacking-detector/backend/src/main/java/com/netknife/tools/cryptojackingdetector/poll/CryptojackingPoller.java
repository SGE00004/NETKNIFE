package com.netknife.tools.cryptojackingdetector.poll;

import com.netknife.common.lifecycle.ShutdownHook;
import com.netknife.tools.cryptojackingdetector.alert.CryptojackingAlertService;
import com.netknife.tools.cryptojackingdetector.detection.CryptojackingDetectionService;
import com.netknife.tools.cryptojackingdetector.detection.SuspicionResult;
import com.netknife.tools.cryptojackingdetector.monitor.CpuUsageMonitor;
import com.netknife.tools.cryptojackingdetector.monitor.ProcessCpuSnapshot;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Bucle periodico propio en vez de Spring @Scheduled, para mantener el mismo patron
 * ya usado por DeviceBlockingManager (network-scanner) en todo el repo: un
 * ScheduledExecutorService con hilo daemon dedicado. Corre continuo desde el
 * arranque (OSHI y "Get-Process" no requieren privilegios de administrador ni
 * tienen coste alto), a diferencia de la captura de trafico que si necesita
 * activacion explicita del usuario.
 */
@Component
public class CryptojackingPoller implements ShutdownHook {

    private static final Logger log = LoggerFactory.getLogger(CryptojackingPoller.class);

    private final CpuUsageMonitor cpuUsageMonitor;
    private final CryptojackingDetectionService detectionService;
    private final CryptojackingAlertService alertService;
    private final long pollIntervalMs;
    private final ScheduledExecutorService scheduler;

    public CryptojackingPoller(
            CpuUsageMonitor cpuUsageMonitor,
            CryptojackingDetectionService detectionService,
            CryptojackingAlertService alertService,
            @Value("${netknife.cryptojacking.poll-interval-ms:5000}") long pollIntervalMs) {
        this.cpuUsageMonitor = cpuUsageMonitor;
        this.detectionService = detectionService;
        this.alertService = alertService;
        this.pollIntervalMs = pollIntervalMs;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(this::newDaemonThread);
    }

    private Thread newDaemonThread(Runnable runnable) {
        Thread thread = new Thread(runnable, "netknife-cryptojacking-poller");
        thread.setDaemon(true);
        return thread;
    }

    @PostConstruct
    void start() {
        scheduler.scheduleAtFixedRate(this::pollOnce, 0, pollIntervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Envuelta en try/catch: si una ejecucion de scheduleAtFixedRate lanza una
     * excepcion sin capturar, el ScheduledExecutorService cancela silenciosamente
     * todas las ejecuciones futuras de esa tarea.
     */
    private void pollOnce() {
        try {
            List<ProcessCpuSnapshot> snapshots = cpuUsageMonitor.poll();
            List<SuspicionResult> suspicions = detectionService.evaluate(snapshots);
            Set<Long> allLivePids = snapshots.stream().map(ProcessCpuSnapshot::pid).collect(Collectors.toSet());
            alertService.reconcile(suspicions, allLivePids);
        } catch (Exception e) {
            log.warn("Fallo en un ciclo de deteccion de cryptojacking: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onShutdown() {
        scheduler.shutdownNow();
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdownNow();
    }
}
