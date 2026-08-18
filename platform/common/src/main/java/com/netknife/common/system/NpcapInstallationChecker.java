package com.netknife.common.system;

/**
 * Comprueba si Npcap esta instalado en el sistema. Extraido a una interfaz por el
 * mismo motivo que {@link AdminPrivilegeChecker}: testear logica de combinacion sin
 * tocar el filesystem real. Vive en platform/common porque tanto el bloqueo de
 * dispositivos (network-scanner) como la captura de trafico (traffic-analyzer)
 * necesitan la misma comprobacion.
 */
public interface NpcapInstallationChecker {

    boolean isInstalled();
}
