package com.netknife.common.exception;

/**
 * El bloqueo o desbloqueo de un dispositivo no se pudo realizar: falta capacidad
 * del sistema (Npcap/privilegios), un guardarraiel de seguridad lo rechaza (bloquear
 * el propio equipo o el gateway), o falta informacion necesaria (MAC del dispositivo,
 * del gateway...).
 */
public class DeviceBlockingException extends RuntimeException {

    public DeviceBlockingException(String message) {
        super(message);
    }

    public DeviceBlockingException(String message, Throwable cause) {
        super(message, cause);
    }
}
