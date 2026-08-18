package com.netknife.tools.networkscanner.dto;

/**
 * Peticion de actualizacion parcial de un dispositivo. Ambos campos son opcionales:
 * si "recognized" es null, no se modifica el estado de reconocimiento.
 */
public record UpdateDeviceRequest(
        Boolean recognized,
        String customName
) {
}
