package com.netknife.tools.exposurechecker.model;

import com.netknife.common.dto.CheckStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "exposure_reports")
public class ExposureReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "status", column = @Column(name = "wifi_status", nullable = false)),
            @AttributeOverride(name = "summary", column = @Column(name = "wifi_summary", length = 500)),
            @AttributeOverride(name = "detail", column = @Column(name = "wifi_detail", length = 500)),
            @AttributeOverride(name = "howToFix", column = @Column(name = "wifi_how_to_fix", length = 500))
    })
    private CategoryResult wifiEncryption;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "status", column = @Column(name = "ports_status", nullable = false)),
            @AttributeOverride(name = "summary", column = @Column(name = "ports_summary", length = 500)),
            @AttributeOverride(name = "detail", column = @Column(name = "ports_detail", length = 500)),
            @AttributeOverride(name = "howToFix", column = @Column(name = "ports_how_to_fix", length = 500))
    })
    private CategoryResult openPorts;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "status", column = @Column(name = "services_status", nullable = false)),
            @AttributeOverride(name = "summary", column = @Column(name = "services_summary", length = 500)),
            @AttributeOverride(name = "detail", column = @Column(name = "services_detail", length = 500)),
            @AttributeOverride(name = "howToFix", column = @Column(name = "services_how_to_fix", length = 500))
    })
    private CategoryResult services;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", nullable = false)
    private CheckStatus overallStatus;

    protected ExposureReport() {
        // requerido por JPA
    }

    public ExposureReport(
            Instant checkedAt,
            CategoryResult wifiEncryption,
            CategoryResult openPorts,
            CategoryResult services,
            CheckStatus overallStatus) {
        this.checkedAt = checkedAt;
        this.wifiEncryption = wifiEncryption;
        this.openPorts = openPorts;
        this.services = services;
        this.overallStatus = overallStatus;
    }

    public Long getId() {
        return id;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public CategoryResult getWifiEncryption() {
        return wifiEncryption;
    }

    public CategoryResult getOpenPorts() {
        return openPorts;
    }

    public CategoryResult getServices() {
        return services;
    }

    public CheckStatus getOverallStatus() {
        return overallStatus;
    }
}
