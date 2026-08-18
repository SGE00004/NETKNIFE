package com.netknife.common.dto;

/**
 * Nivel de riesgo comun a todas las herramientas, pensado para alimentar
 * el "semaforo de riesgo" del frontend (verde/amarillo/rojo).
 */
public enum RiskLevel {
    VERDE,
    AMARILLO,
    ROJO;

    /**
     * Traduce el estado de una comprobacion individual (con su cuarto estado
     * NO_VERIFICABLE) al riesgo de tres niveles que pinta el semaforo del
     * frontend. NO_VERIFICABLE se trata como AMARILLO (no como VERDE): no saber
     * si algo esta bien nunca debe presentarse como si todo estuviera en orden.
     */
    public static RiskLevel fromCheckStatus(CheckStatus status) {
        return switch (status) {
            case PELIGRO -> ROJO;
            case ATENCION, NO_VERIFICABLE -> AMARILLO;
            case OK -> VERDE;
        };
    }
}
