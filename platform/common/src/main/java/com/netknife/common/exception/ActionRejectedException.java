package com.netknife.common.exception;

/**
 * Una accion potencialmente peligrosa fue rechazada por un guardarrail de seguridad
 * (p.ej. finalizar un proceso que no esta en la lista de sospechosos activos, o un
 * PID protegido). El mensaje esta listo para mostrarse tal cual al usuario.
 * Generaliza el mismo patron que DeviceBlockingException para acciones fuera del
 * ambito de bloqueo de dispositivos.
 */
public class ActionRejectedException extends RuntimeException {

    public ActionRejectedException(String message) {
        super(message);
    }
}
