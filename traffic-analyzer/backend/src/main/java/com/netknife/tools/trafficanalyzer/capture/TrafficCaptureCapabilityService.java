package com.netknife.tools.trafficanalyzer.capture;

import com.netknife.common.system.AdminPrivilegeChecker;
import com.netknife.common.system.NpcapInstallationChecker;
import jakarta.annotation.PostConstruct;
import org.pcap4j.core.Pcaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Detecta en caliente si la captura de trafico es viable en este equipo. Mismo
 * esqueleto que BlockingCapabilityService (network-scanner), con su propio Reason
 * y mensajes: son features distintas (ver el trafico propio vs. cortar la conexion
 * de otro dispositivo) y merecen texto propio orientado a lo que el usuario esta
 * intentando hacer en cada caso.
 */
@Service
public class TrafficCaptureCapabilityService {

    private static final Logger log = LoggerFactory.getLogger(TrafficCaptureCapabilityService.class);

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

    public TrafficCaptureCapabilityService(AdminPrivilegeChecker adminPrivilegeChecker,
                                            NpcapInstallationChecker npcapInstallationChecker) {
        this.adminPrivilegeChecker = adminPrivilegeChecker;
        this.npcapInstallationChecker = npcapInstallationChecker;
    }

    @PostConstruct
    void detectOnStartup() {
        cached = detect();
        if (!cached.available()) {
            log.info("Analizador de trafico no disponible ({}): {}", cached.reason(), cached.message());
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
                    "El analizador de trafico solo esta disponible en Windows por ahora.");
        }
        if (!adminPrivilegeChecker.hasAdministratorPrivileges()) {
            return Capability.unavailable(Reason.MISSING_ADMIN_PRIVILEGES,
                    "NETKNIFE necesita ejecutarse como administrador para poder ver con que se esta "
                            + "comunicando tu ordenador. Cierra la aplicacion y vuelve a abrirla con "
                            + "\"Ejecutar como administrador\".");
        }
        if (!npcapInstallationChecker.isInstalled()) {
            return Capability.unavailable(Reason.NPCAP_NOT_INSTALLED,
                    "Para poder analizar tu trafico de red necesitas instalar Npcap (npcap.com), marcando "
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
