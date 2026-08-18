package com.netknife.tools.networkscanner.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Dispositivo visto en la red local durante uno o varios escaneos.
 * La identidad primaria de "es el mismo dispositivo" es la direccion MAC
 * cuando esta disponible; si no lo esta, se trata como un dispositivo nuevo
 * en cada escaneo (no hay forma fiable de correlacionarlo).
 */
@Entity
@Table(name = "network_devices", uniqueConstraints = @UniqueConstraint(columnNames = "mac_address"))
public class NetworkDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "hostname")
    private String hostname;

    @Column(name = "vendor")
    private String vendor;

    @Column(name = "custom_name")
    private String customName;

    @Column(name = "recognized", nullable = false)
    private boolean recognized = false;

    @Column(name = "blocked", nullable = false)
    private boolean blocked = false;

    @Column(name = "blocked_at")
    private Instant blockedAt;

    @Column(name = "first_seen", nullable = false)
    private Instant firstSeen;

    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;

    protected NetworkDevice() {
        // requerido por JPA
    }

    public NetworkDevice(String macAddress, String ipAddress, String hostname, String vendor, Instant seenAt) {
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.hostname = hostname;
        this.vendor = vendor;
        this.recognized = false;
        this.firstSeen = seenAt;
        this.lastSeen = seenAt;
    }

    public Long getId() {
        return id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public boolean isRecognized() {
        return recognized;
    }

    public void setRecognized(boolean recognized) {
        this.recognized = recognized;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Instant getBlockedAt() {
        return blockedAt;
    }

    public void setBlockedAt(Instant blockedAt) {
        this.blockedAt = blockedAt;
    }

    public Instant getFirstSeen() {
        return firstSeen;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }
}
