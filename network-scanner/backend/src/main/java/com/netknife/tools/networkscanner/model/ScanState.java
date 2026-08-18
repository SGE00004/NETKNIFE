package com.netknife.tools.networkscanner.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Fila unica (id fijo) que registra el instante del ultimo escaneo completado.
 * Permite saber, incluso tras recargar la pantalla sin volver a escanear, que
 * dispositivos fueron descubiertos por primera vez en el escaneo mas reciente
 * (comparando su firstSeen contra este valor) sin depender de MAX(lastSeen),
 * que no avanzaria si un escaneo no detecta ningun host.
 */
@Entity
@Table(name = "scan_state")
public class ScanState {

    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "last_scan_at", nullable = false)
    private Instant lastScanAt;

    protected ScanState() {
        // requerido por JPA
    }

    public ScanState(Instant lastScanAt) {
        this.id = SINGLETON_ID;
        this.lastScanAt = lastScanAt;
    }

    public Long getId() {
        return id;
    }

    public Instant getLastScanAt() {
        return lastScanAt;
    }

    public void setLastScanAt(Instant lastScanAt) {
        this.lastScanAt = lastScanAt;
    }
}
