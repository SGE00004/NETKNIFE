package com.netknife.tools.trafficanalyzer.capture;

/** Identifica una conexion activa desde el punto de vista de este equipo (lado local). */
public record ConnectionKey(String protocol, int localPort, String remoteIp, int remotePort) {
}
