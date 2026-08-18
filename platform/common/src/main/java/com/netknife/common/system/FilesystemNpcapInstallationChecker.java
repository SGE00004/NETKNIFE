package com.netknife.common.system;

import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Comprueba la presencia de Npcap por existencia de fichero (System32\Npcap\wpcap.dll
 * y Packet.dll), en vez de intentar inicializar pcap4j directamente: evita arriesgar
 * un UnsatisfiedLinkError ruidoso en el log de arranque solo para detectar disponibilidad.
 */
@Component
public class FilesystemNpcapInstallationChecker implements NpcapInstallationChecker {

    @Override
    public boolean isInstalled() {
        String systemRoot = System.getenv().getOrDefault("WINDIR", "C:\\Windows");
        Path npcapDir = Path.of(systemRoot, "System32", "Npcap");
        return Files.exists(npcapDir.resolve("wpcap.dll")) && Files.exists(npcapDir.resolve("Packet.dll"));
    }
}
