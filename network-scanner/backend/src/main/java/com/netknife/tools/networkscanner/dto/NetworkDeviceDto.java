package com.netknife.tools.networkscanner.dto;

import com.netknife.tools.networkscanner.model.NetworkDevice;

import java.time.Instant;

/**
 * Representacion de un dispositivo de red pensada para consumo del frontend,
 * ya con el nombre a mostrar resuelto (nombre personalizado > hostname > fabricante).
 */
public record NetworkDeviceDto(
        Long id,
        String macAddress,
        String ipAddress,
        String hostname,
        String vendor,
        String customName,
        String displayName,
        boolean recognized,
        boolean newSinceLastScan,
        boolean blocked,
        Instant blockedAt,
        Instant firstSeen,
        Instant lastSeen
) {
    /**
     * @param lastScanAt instante del ultimo escaneo completado (puede ser null si nunca se ha
     *                    escaneado). Un dispositivo es "nuevo desde el ultimo escaneo" si todavia
     *                    no ha sido reconocido y fue visto por primera vez precisamente en ese
     *                    escaneo; en cuanto el usuario lo reconoce o pasa otro escaneo, deja de serlo.
     */
    public static NetworkDeviceDto fromEntity(NetworkDevice device, Instant lastScanAt) {
        String displayName = resolveDisplayName(device);
        boolean newSinceLastScan = !device.isRecognized()
                && lastScanAt != null
                && lastScanAt.equals(device.getFirstSeen());
        return new NetworkDeviceDto(
                device.getId(),
                device.getMacAddress(),
                device.getIpAddress(),
                device.getHostname(),
                device.getVendor(),
                device.getCustomName(),
                displayName,
                device.isRecognized(),
                newSinceLastScan,
                device.isBlocked(),
                device.getBlockedAt(),
                device.getFirstSeen(),
                device.getLastSeen()
        );
    }

    private static String resolveDisplayName(NetworkDevice device) {
        if (device.getCustomName() != null && !device.getCustomName().isBlank()) {
            return device.getCustomName();
        }
        if (device.getHostname() != null && !device.getHostname().isBlank()) {
            return device.getHostname();
        }
        if (device.getVendor() != null && !device.getVendor().isBlank()) {
            return "Dispositivo " + device.getVendor();
        }
        return "Dispositivo desconocido";
    }
}
