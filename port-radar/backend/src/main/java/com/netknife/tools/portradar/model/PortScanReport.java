package com.netknife.tools.portradar.model;

import com.netknife.common.dto.CheckStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "port_scan_reports")
public class PortScanReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String target;

    @Column(name = "scanned_at", nullable = false)
    private Instant scannedAt;

    @Column(name = "ports_scanned", nullable = false)
    private int portsScanned;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", nullable = false)
    private CheckStatus overallStatus;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("port ASC")
    private List<PortScanFinding> openPorts = new ArrayList<>();

    protected PortScanReport() {
        // requerido por JPA
    }

    public PortScanReport(String target, Instant scannedAt, int portsScanned, CheckStatus overallStatus) {
        this.target = target;
        this.scannedAt = scannedAt;
        this.portsScanned = portsScanned;
        this.overallStatus = overallStatus;
    }

    public void addOpenPort(PortScanFinding finding) {
        finding.setReport(this);
        openPorts.add(finding);
    }

    public Long getId() {
        return id;
    }

    public String getTarget() {
        return target;
    }

    public Instant getScannedAt() {
        return scannedAt;
    }

    public int getPortsScanned() {
        return portsScanned;
    }

    public CheckStatus getOverallStatus() {
        return overallStatus;
    }

    public List<PortScanFinding> getOpenPorts() {
        return openPorts;
    }
}
