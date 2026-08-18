package com.netknife.tools.cryptojackingdetector.monitor;

import java.time.Instant;

/** Una lectura de CPU de un proceso en un instante concreto. */
public record ProcessCpuSnapshot(
        long pid,
        String name,
        String path,
        String commandLine,
        double cpuPercent,
        Instant sampledAt
) {
}
