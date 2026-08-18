package com.netknife.common.system;

/**
 * Comprueba si el proceso actual tiene privilegios de administrador. Extraido a una
 * interfaz para poder testear logica que depende de esta comprobacion sin depender
 * del sistema operativo real. Vive en platform/common porque tanto el bloqueo de
 * dispositivos (network-scanner) como la captura de trafico (traffic-analyzer)
 * necesitan la misma comprobacion.
 */
public interface AdminPrivilegeChecker {

    boolean hasAdministratorPrivileges();
}
