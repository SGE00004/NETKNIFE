package com.netknife.tools.portradar.model;

import com.netknife.common.dto.CheckStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "port_scan_findings")
public class PortScanFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private PortScanReport report;

    @Column(nullable = false)
    private int port;

    @Column(name = "protocol_label", nullable = false)
    private String protocolLabel;

    @Column(length = 500)
    private String banner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckStatus status;

    @Column(length = 500)
    private String summary;

    @Column(name = "how_to_fix", length = 500)
    private String howToFix;

    protected PortScanFinding() {
        // requerido por JPA
    }

    public PortScanFinding(int port, String protocolLabel, String banner, CheckStatus status, String summary, String howToFix) {
        this.port = port;
        this.protocolLabel = protocolLabel;
        this.banner = banner;
        this.status = status;
        this.summary = summary;
        this.howToFix = howToFix;
    }

    void setReport(PortScanReport report) {
        this.report = report;
    }

    public Long getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public String getProtocolLabel() {
        return protocolLabel;
    }

    public String getBanner() {
        return banner;
    }

    public CheckStatus getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public String getHowToFix() {
        return howToFix;
    }
}
