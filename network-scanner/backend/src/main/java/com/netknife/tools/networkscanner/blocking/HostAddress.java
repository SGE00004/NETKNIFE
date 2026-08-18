package com.netknife.tools.networkscanner.blocking;

/** Par IP/MAC de un host de la red, usado tanto para la victima como para el gateway. */
public record HostAddress(String ipAddress, String macAddress) {
}
