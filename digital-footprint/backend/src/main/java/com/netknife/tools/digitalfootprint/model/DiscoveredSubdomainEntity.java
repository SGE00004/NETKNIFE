package com.netknife.tools.digitalfootprint.model;

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
@Table(name = "discovered_subdomains")
public class DiscoveredSubdomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private DomainFootprintReport report;

    @Column(nullable = false)
    private String subdomain;

    @Column(name = "ip_address")
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckStatus status;

    protected DiscoveredSubdomainEntity() {
        // requerido por JPA
    }

    public DiscoveredSubdomainEntity(String subdomain, String ipAddress, CheckStatus status) {
        this.subdomain = subdomain;
        this.ipAddress = ipAddress;
        this.status = status;
    }

    void setReport(DomainFootprintReport report) {
        this.report = report;
    }

    public Long getId() {
        return id;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public CheckStatus getStatus() {
        return status;
    }
}
