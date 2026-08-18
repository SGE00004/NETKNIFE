package com.netknife.tools.hygienechecklist.model;

import com.netknife.common.dto.CheckStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Ultima respuesta del usuario a un item de higiene no automatizable (ej.
 * "backup"). La clave primaria es el id estable del HygieneCheck, no un
 * identificador generado: solo existe una respuesta vigente por item.
 */
@Entity
@Table(name = "hygiene_manual_status")
public class HygieneManualStatus {

    @Id
    @Column(name = "item_id")
    private String itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckStatus status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected HygieneManualStatus() {
        // requerido por JPA
    }

    public HygieneManualStatus(String itemId, CheckStatus status, Instant updatedAt) {
        this.itemId = itemId;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public String getItemId() {
        return itemId;
    }

    public CheckStatus getStatus() {
        return status;
    }

    public void setStatus(CheckStatus status) {
        this.status = status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
