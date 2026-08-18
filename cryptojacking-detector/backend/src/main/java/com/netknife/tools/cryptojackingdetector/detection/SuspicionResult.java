package com.netknife.tools.cryptojackingdetector.detection;

public record SuspicionResult(
        long pid,
        String processName,
        String processPath,
        SuspicionReason reason,
        double cpuPercent
) {
}
