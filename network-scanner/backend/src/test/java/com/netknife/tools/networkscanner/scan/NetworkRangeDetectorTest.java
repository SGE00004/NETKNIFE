package com.netknife.tools.networkscanner.scan;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkRangeDetectorTest {

    private final NetworkRangeDetector detector = new NetworkRangeDetector();

    @Test
    void computesAllHostAddressesForASlash24() {
        List<String> hosts = detector.computeHostAddresses("192.168.1.42", (short) 24, 254);

        // Un /24 tiene 254 direcciones de host validas: .1 a .254 (sin red .0 ni broadcast .255)
        assertThat(hosts).hasSize(254);
        assertThat(hosts).contains("192.168.1.1", "192.168.1.254");
        assertThat(hosts).doesNotContain("192.168.1.0", "192.168.1.255");
    }

    @Test
    void respectsMaxHostsLimit() {
        List<String> hosts = detector.computeHostAddresses("10.0.0.5", (short) 24, 10);

        assertThat(hosts).hasSize(10);
    }

    @Test
    void fallsBackToSlash24WhenPrefixIsTooWide() {
        List<String> hosts = detector.computeHostAddresses("172.16.5.10", (short) 8, 254);

        assertThat(hosts).allMatch(ip -> ip.startsWith("172.16.5."));
    }

    @Test
    void computesSmallerSubnetCorrectly() {
        List<String> hosts = detector.computeHostAddresses("192.168.1.10", (short) 30, 254);

        // /30 tiene 4 direcciones: red, 2 hosts, broadcast
        assertThat(hosts).hasSize(2);
    }
}
