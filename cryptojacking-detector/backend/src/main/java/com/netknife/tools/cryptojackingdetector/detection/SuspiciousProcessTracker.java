package com.netknife.tools.cryptojackingdetector.detection;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cuenta lecturas de CPU alta consecutivas por PID, en memoria (se reinicia si
 * NETKNIFE se reinicia, igual que los bloqueos activos de network-scanner). Exigir
 * varias lecturas seguidas por encima del umbral, en vez de alertar en la primera,
 * evita falsos positivos por picos puntuales de CPU (una compilacion, un arranque
 * de app pesada...).
 */
@Component
public class SuspiciousProcessTracker {

    private final Map<Long, Integer> consecutiveHighReadings = new ConcurrentHashMap<>();

    /** Devuelve true si este PID lleva ya {@code requiredConsecutiveReadings} lecturas altas seguidas. */
    public boolean recordReadingAndCheckSustained(long pid, boolean aboveThreshold, int requiredConsecutiveReadings) {
        if (!aboveThreshold) {
            consecutiveHighReadings.remove(pid);
            return false;
        }
        int count = consecutiveHighReadings.merge(pid, 1, Integer::sum);
        return count >= requiredConsecutiveReadings;
    }

    public void forget(long pid) {
        consecutiveHighReadings.remove(pid);
    }

    /** Olvida cualquier PID que ya no exista, para no acumular memoria de procesos muertos. */
    public void retainOnly(Set<Long> currentPids) {
        consecutiveHighReadings.keySet().retainAll(currentPids);
    }
}
