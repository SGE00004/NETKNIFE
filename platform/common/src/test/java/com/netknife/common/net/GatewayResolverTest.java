package com.netknife.common.net;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayResolverTest {

    @Test
    void parsesThePowerShellGatewayOutput() {
        Optional<String> gateway = GatewayResolver.parseSingleIpOutput("192.168.1.1\r\n");

        assertThat(gateway).contains("192.168.1.1");
    }

    @Test
    void ignoresAnEmptyOrUnroutableGatewayOutput() {
        assertThat(GatewayResolver.parseSingleIpOutput("")).isEmpty();
        assertThat(GatewayResolver.parseSingleIpOutput("0.0.0.0")).isEmpty();
    }

    @Test
    void parsesMacosRouteOutput() {
        String output = """
                   route to: default
                destination: default
                       mask: default
                    gateway: 192.168.1.1
                  interface: en0
                """;

        assertThat(GatewayResolver.parseMacosRoute(output)).contains("192.168.1.1");
    }

    @Test
    void parsesProcNetRouteDefaultGatewayLine() {
        // Gateway "0101A8C0" en hexadecimal little-endian = 192.168.1.1; flags "0003" = UP|GATEWAY.
        String line = "wlan0\t00000000\t0101A8C0\t0003\t0\t0\t0\t00000000\t0\t0\t0";

        assertThat(GatewayResolver.parseProcNetRouteLine(line)).contains("192.168.1.1");
    }

    @Test
    void ignoresNonDefaultRoutesInProcNetRoute() {
        // Destination distinto de 00000000: no es la ruta por defecto.
        String line = "wlan0\t0001A8C0\t00000000\t0001\t0\t0\t0\t00FFFFFF\t0\t0\t0";

        assertThat(GatewayResolver.parseProcNetRouteLine(line)).isEmpty();
    }

    @Test
    void ignoresDefaultDestinationWithoutTheGatewayFlag() {
        // Destination 00000000 pero flags "0001" (solo UP, sin GATEWAY): no es una ruta via gateway.
        String line = "wlan0\t00000000\t0101A8C0\t0001\t0\t0\t0\t00000000\t0\t0\t0";

        assertThat(GatewayResolver.parseProcNetRouteLine(line)).isEmpty();
    }
}
