package com.netknife.tools.cryptojackingdetector.monitor;

import java.util.Locale;
import java.util.Set;

/**
 * Nombres de ejecutables de software de minado de criptomonedas conocidos y
 * publicamente documentados. Encontrar uno de estos procesos en ejecucion es una
 * senal de alta confianza (no necesita esperar a lecturas de CPU sostenidas para
 * alertar), a diferencia de la deteccion por comportamiento (CPU alta + sin ventana),
 * que es una heuristica y puede tener falsos positivos.
 */
public final class KnownMinerProcessNames {

    private static final Set<String> NAMES = Set.of(
            "xmrig", "xmrig-nvidia", "xmrig-amd", "minerd", "ethminer", "cpuminer",
            "cgminer", "bfgminer", "nicehash", "nicehashminer", "t-rex", "phoenixminer",
            "teamredminer", "xmr-stak", "ccminer", "srbminer", "lolminer", "gminer"
    );

    private KnownMinerProcessNames() {
    }

    public static boolean isKnownMinerName(String processName) {
        if (processName == null) {
            return false;
        }
        String normalized = processName.toLowerCase(Locale.ROOT);
        if (normalized.endsWith(".exe")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }
        return NAMES.contains(normalized);
    }
}
