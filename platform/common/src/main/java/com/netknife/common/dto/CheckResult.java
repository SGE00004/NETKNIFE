package com.netknife.common.dto;

/**
 * Resultado de una comprobacion individual, antes de persistirse. howToFix es
 * null cuando el estado es OK (no hace falta arreglar nada) y tambien puede
 * serlo en NO_VERIFICABLE si no hay una guia manual clara que ofrecer.
 */
public record CheckResult(
        CheckStatus status,
        String summary,
        String detail,
        String howToFix
) {
    public static CheckResult ok(String summary) {
        return new CheckResult(CheckStatus.OK, summary, null, null);
    }
}
