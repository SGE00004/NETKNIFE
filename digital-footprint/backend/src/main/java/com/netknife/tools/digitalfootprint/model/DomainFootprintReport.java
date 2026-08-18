package com.netknife.tools.digitalfootprint.model;

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
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "domain_footprint_reports")
public class DomainFootprintReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;

    @Column(nullable = false)
    private String domain;

    @Column(name = "whois_registrar")
    private String whoisRegistrar;

    @Column(name = "whois_created_date")
    private String whoisCreatedDate;

    @Column(name = "whois_expires_date")
    private String whoisExpiresDate;

    @Column(name = "geo_ip")
    private String geoIp;

    @Column(name = "geo_country")
    private String geoCountry;

    @Column(name = "geo_city")
    private String geoCity;

    @Column(name = "geo_isp")
    private String geoIsp;

    @Column(name = "geo_lat")
    private Double geoLat;

    @Column(name = "geo_lon")
    private Double geoLon;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", nullable = false)
    private CheckStatus overallStatus;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DiscoveredSubdomainEntity> subdomains = new ArrayList<>();

    protected DomainFootprintReport() {
        // requerido por JPA
    }

    public DomainFootprintReport(
            Instant analyzedAt,
            String domain,
            String whoisRegistrar,
            String whoisCreatedDate,
            String whoisExpiresDate,
            String geoIp,
            String geoCountry,
            String geoCity,
            String geoIsp,
            Double geoLat,
            Double geoLon,
            CheckStatus overallStatus) {
        this.analyzedAt = analyzedAt;
        this.domain = domain;
        this.whoisRegistrar = whoisRegistrar;
        this.whoisCreatedDate = whoisCreatedDate;
        this.whoisExpiresDate = whoisExpiresDate;
        this.geoIp = geoIp;
        this.geoCountry = geoCountry;
        this.geoCity = geoCity;
        this.geoIsp = geoIsp;
        this.geoLat = geoLat;
        this.geoLon = geoLon;
        this.overallStatus = overallStatus;
    }

    public void addSubdomain(DiscoveredSubdomainEntity subdomain) {
        subdomain.setReport(this);
        subdomains.add(subdomain);
    }

    public Long getId() {
        return id;
    }

    public Instant getAnalyzedAt() {
        return analyzedAt;
    }

    public String getDomain() {
        return domain;
    }

    public String getWhoisRegistrar() {
        return whoisRegistrar;
    }

    public String getWhoisCreatedDate() {
        return whoisCreatedDate;
    }

    public String getWhoisExpiresDate() {
        return whoisExpiresDate;
    }

    public String getGeoIp() {
        return geoIp;
    }

    public String getGeoCountry() {
        return geoCountry;
    }

    public String getGeoCity() {
        return geoCity;
    }

    public String getGeoIsp() {
        return geoIsp;
    }

    public Double getGeoLat() {
        return geoLat;
    }

    public Double getGeoLon() {
        return geoLon;
    }

    public CheckStatus getOverallStatus() {
        return overallStatus;
    }

    public List<DiscoveredSubdomainEntity> getSubdomains() {
        return subdomains;
    }
}
