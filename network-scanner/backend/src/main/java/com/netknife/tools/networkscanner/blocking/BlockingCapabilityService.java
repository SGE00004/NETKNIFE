package com.netknife.tools.networkscanner.blocking;

import com.netknife.common.system.AdminPrivilegeChecker;
import com.netknife.common.system.NpcapInstallationChecker;
import jakarta.annotation.PostConstruct;
import org.pcap4j.core.Pcaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Detecta en caliente si el bloqueo de dispositivos por ARP spoofing es viable en
 * este equipo, y cachea el resultado (se recalcula al arrancar y bajo demanda con
 * {@link #refresh()}, p.ej. justo antes de intentar un bloqueo, por si el usuario
 * instalo Npcap con la app ya abierta).
 */
@Service
public class BlockingCapabilityService {

    private static final Logger log = LoggerFactory.getLogger(BlockingCapabilityService.class);

    public enum Reason {
        NONE,
        UNSUPPORTED_OS,
        NPCAP_NOT_INSTALLED,
        MISSING_ADMIN_PRIVILEGES,
        INITIALIZATION_ERROR
    }

    public record Capability(boolean available, Reason reason, String message) {
        static Capability ok() {
            return new Capability(true, Reason.NONE, null);
        }

        static Capability unavailable(Reason reason, String message) {
            return new Capability(false, reason, message);
        }
    }

    private final AdminPrivilegeChecker adminPrivilegeChecker;
    private final NpcapInstallationChecker npcapInstallationChecker;

    private volatile Capability cached;

    public BlockingCapabilityService(AdminPrivilegeChecker adminPrivilegeChecker,
                                      NpcapInstallationChecker npcapInstallationChecker) {
        this.adminPrivilegeChecker = adminPrivilegeChecker;
        this.npcapInstallationChecker = npcapInstallationChecker;
    }

    @PostConstruct
    void detectOnStartup() {
        cached = detect();
        if (!cached.available()) {
            log.info("Bloqueo de dispositivos no disponible ({}): {}", cached.reason(), cached.message());
        }
    }

    public Capability current() {
        return cached;
    }

    public Capability refresh() {
        cached = detect();
        return cached;
    }

    private Capability detect() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("win")) {
            return Capability.unavailable(Reason.UNSUPPORTED_OS,
                    "El bloqueo automatico de dispositivos solo esta disponible en Windows por ahora.");
        }
        if (!adminPrivilegeChecker.hasAdministratorPrivileges()) {
            return Capability.unavailable(Reason.MISSING_ADMIN_PRIVILEGES,
                    "NETKNIFE necesita ejecutarse como administrador para poder cortar la conexion de un "
                            + "dispositivo. Cierra la aplicacion y vuelve a abrirla con \"Ejecutar como administrador\".");
        }
        if (!npcapInstallationChecker.isInstalled()) {
            return Capability.unavailable(Reason.NPCAP_NOT_INSTALLED,
                    "Para poder cortar la conexion de un dispositivo necesitas instalar Npcap (npcap.com), marcando "
                            + "la opcion \"WinPcap API-compatible Mode\" durante la instalacion.");
        }
        try {
            Pcaps.findAllDevs();
        } catch (Throwable t) {
            log.warn("pcap4j no pudo inicializarse pese a que Npcap parece estar instalado", t);
            return Capability.unavailable(Reason.INITIALIZATION_ERROR,
                    "No se ha podido inicializar la captura de paquetes de red. Reinicia NETKNIFE o reinstala Npcap.");
        }
        return Capability.ok();
    }
}
