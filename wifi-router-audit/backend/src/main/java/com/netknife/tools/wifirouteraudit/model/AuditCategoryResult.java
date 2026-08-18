package com.netknife.tools.wifirouteraudit.model;

import com.netknife.common.dto.CheckStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Resultado persistido de una categoria del informe de auditoria (cifrado
 * WiFi, credenciales por defecto, WPS). Duplica deliberadamente la forma de
 * CategoryResult de exposure-checker: es una clase de 4 campos, no vale la
 * pena compartirla via common (eso obligaria a anadir JPA a un modulo que hoy
 * no lo necesita).
 */
@Embeddable
public class AuditCategoryResult {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckStatus status;

    @Column(length = 500)
    private String summary;

    @Column(length = 500)
    private String detail;

    @Column(name = "how_to_fix", length = 500)
    private String howToFix;

    protected AuditCategoryResult() {
        // requerido por JPA
    }

    public AuditCategoryResult(CheckStatus status, String summary, String detail, String howToFix) {
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
