package com.netknife.tools.exposurechecker.model;

import com.netknife.common.dto.CheckStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Resultado persistido de una categoria del informe de exposicion (cifrado WiFi,
 * puertos, servicios). Se embebe tres veces en ExposureReport con distintos
 * prefijos de columna en vez de usar una tabla separada, ya que el numero de
 * categorias es fijo y pequeno.
 */
@Embeddable
public class CategoryResult {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckStatus status;

    @Column(length = 500)
    private String summary;

    @Column(length = 500)
    private String detail;

    @Column(name = "how_to_fix", length = 500)
    private String howToFix;

    protected CategoryResult() {
        // requerido por JPA
    }

    public CategoryResult(CheckStatus status, String summary, String detail, String howToFix) {
        this.status = status;
        this.summary = summary;
        this.detail = detail;
        this.howToFix = howToFix;
    }

    public CheckStatus getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public String getDetail() {
        return detail;
    }

    public String getHowToFix() {
        return howToFix;
    }
}
