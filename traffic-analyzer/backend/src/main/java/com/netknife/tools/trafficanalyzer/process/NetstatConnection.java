package com.netknife.tools.trafficanalyzer.process;

/** Una fila de la salida de "netstat -ano -b" ya parseada. processName es null si no se pudo resolver (sin admin). */
public record NetstatConnection(
        String protocol,
        String localIp,
        int localPort,
        String remoteIp,
        Integer remotePort,
        String state,
        long pid,
        String processName
) {
}
