package com.netknife.tools.cryptojackingdetector.monitor;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lee el uso de CPU por proceso con OSHI. OSHI da contadores de ticks acumulados,
 * no un porcentaje instantaneo: hay que comparar dos lecturas del mismo proceso
 * separadas en el tiempo ({@link OSProcess#getProcessCpuLoadBetweenTicks(OSProcess)}),
 * asi que este componente guarda la lectura anterior de cada PID y calcula el
 * porcentaje contra ella en cada poll. La primera vez que se ve un PID no hay
 * lectura anterior con la que comparar, asi que se reporta 0% hasta el siguiente poll.
 *
 * El porcentaje se divide entre el numero de nucleos logicos: OSHI suma los ticks de
 * todos los procesadores, por lo que un proceso multi-hilo puede superar el 100% sin
 * normalizar; dividiendo se obtiene el mismo criterio que usa el Administrador de
 * tareas de Windows, mas intuitivo para el usuario final.
 */
@Component
public class CpuUsageMonitor {

    private final SystemInfo systemInfo = new SystemInfo();
    private final int logicalProcessorCount = systemInfo.getHardware().getProcessor().getLogicalProcessorCount();

    private volatile Map<Integer, OSProcess> previousByPid = Map.of();

    public synchronized List<ProcessCpuSnapshot> poll() {
        List<OSProcess> current = systemInfo.getOperatingSystem().getProcesses();
        Instant now = Instant.now();

        List<ProcessCpuSnapshot> result = new ArrayList<>();
        for (OSProcess process : current) {
            OSProcess baseline = previousByPid.get(process.getProcessID());
            double cpuPercent = baseline == null
                    ? 0.0
                    : (process.getProcessCpuLoadBetweenTicks(baseline) * 100.0) / logicalProcessorCount;
            result.add(new ProcessCpuSnapshot(
                    process.getProcessID(), process.getName(), process.getPath(),
                    process.getCommandLine(), cpuPercent, now));
        }

        Map<Integer, OSProcess> snapshot = new HashMap<>();
        for (OSProcess process : current) {
            snapshot.put(process.getProcessID(), process);
        }
        previousByPid = snapshot;

        return result;
    }
}
