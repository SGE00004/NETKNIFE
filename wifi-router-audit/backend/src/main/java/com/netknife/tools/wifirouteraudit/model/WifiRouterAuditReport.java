package com.netknife.tools.wifirouteraudit.model;

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
@Table(name = "wifi_router_audit_reports")
public class WifiRouterAuditReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @Column(name = "router_address")
    private String routerAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "status", column = @Column(name = "wifi_status", nullable = false)),
            @AttributeOverride(name = "summary", column = @Column(name = "wifi_summary", length = 500)),
            @AttributeOverride(name = "detail", column = @Column(name = "wifi_detail", length = 500)),
            @AttributeOverride(name = "howToFix", column = @Column(name = "wifi_how_to_fix", length = 500))
    })
    private AuditCategoryResult wifiEncryption;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "status", column = @Column(name = "credentials_status", nullable = false)),
            @AttributeOverride(name = "summary", column = @Column(name = "credentials_summary", length = 500)),
            @AttributeOverride(name = "detail", column = @Column(name = "credentials_detail", length = 500)),
            @AttributeOverride(name = "howToFix", column = @Column(name = "credentials_how_to_fix", length = 500))
    })
    private AuditCategoryResult defaultCredentials;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "status", column = @Column(name = "wps_status", nullable = false)),
            @AttributeOverride(name = "summary", column = @Column(name = "wps_summary", length = 500)),
            @AttributeOverride(name = "detail", column = @Column(name = "wps_detail", length = 500)),
            @AttributeOverride(name = "howToFix", column = @Column(name = "wps_how_to_fix", length = 500))
    })
    private AuditCategoryResult wps;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", nullable = false)
    private CheckStatus overallStatus;

    protected WifiRouterAuditReport() {
        // requerido por JPA
    }

    public WifiRouterAuditReport(
            Instant checkedAt,
            String routerAddress,
            AuditCategoryResult wifiEncryption,
            AuditCategoryResult defaultCredentials,
            AuditCategoryResult wps,
            CheckStatus overallStatus) {
        this.checkedAt = checkedAt;
        this.routerAddress = routerAddress;
        this.wifiEncryption = wifiEncryption;
        this.defaultCredentials = defaultCredentials;
        this.wps = wps;
        this.overallStatus = overallStatus;
    }

    public Long getId() {
        return id;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public String getRouterAddress() {
        return routerAddress;
    }

    public AuditCategoryResult getWifiEncryption() {
        return wifiEncryption;
    }

    public AuditCategoryResult getDefaultCredentials() {
        return defaultCredentials;
    }

    public AuditCategoryResult getWps() {
        return wps;
    }

    public CheckStatus getOverallStatus() {
        return overallStatus;
    }
}
