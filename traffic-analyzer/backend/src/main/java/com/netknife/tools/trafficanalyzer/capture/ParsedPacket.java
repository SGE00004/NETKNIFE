package com.netknife.tools.trafficanalyzer.capture;

/** Un paquete capturado, reducido a lo que necesita el analizador de trafico. */
public record ParsedPacket(
        String srcIp,
        int srcPort,
        String dstIp,
        int dstPort,
        String protocol,
        boolean encrypted,
        int lengthBytes
) {
}
