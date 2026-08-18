package com.netknife.tools.cryptojackingdetector.alert.model;

import com.netknife.tools.cryptojackingdetector.alert.AlertResolution;
import com.netknife.tools.cryptojackingdetector.detection.SuspicionReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "cryptojacking_alerts")
public class CryptojackingAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long pid;

    @Column(name = "process_name", nullable = false)
    private String processName;

    @Column(name = "process_path")
    private String processPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuspicionReason reason;

    @Column(name = "peak_cpu_percent", nullable = false)
    private double peakCpuPercent;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    /** null mientras la alerta sigue activa. */
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution")
    private AlertResolution resolution;

    protected CryptojackingAlert() {
        // requerido por JPA
    }

    public CryptojackingAlert(long pid, String processName, String processPath, SuspicionReason reason,
                               double peakCpuPercent, Instant detectedAt) {
        this.pid = pid;
        this.processName = processName;
        this.processPath = processPath;
        this.reason = reason;
        this.peakCpuPercent = peakCpuPercent;
        this.detectedAt = detectedAt;
    }

    public void updatePeakCpu(double cpuPercent) {
        if (cpuPercent > this.peakCpuPercent) {
            this.peakCpuPercent = cpuPercent;
        }
    }

    public void resolve(AlertResolution resolution, Instant resolvedAt) {
        this.resolution = resolution;
        this.resolvedAt = resolvedAt;
    }

    public Long getId() {
        return id;
    }

    public long getPid() {
        return pid;
    }

    public String getProcessName() {
        return processName;
    }

    public String getProcessPath() {
        return processPath;
    }

    public SuspicionReason getReason() {
        return reason;
    }

    public double getPeakCpuPercent() {
        return peakCpuPercent;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public AlertResolution getResolution() {
        return resolution;
    }
}
