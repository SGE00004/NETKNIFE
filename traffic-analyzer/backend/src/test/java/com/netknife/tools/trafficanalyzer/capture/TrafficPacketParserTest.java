package com.netknife.tools.trafficanalyzer.capture;

import org.junit.jupiter.api.Test;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV4Rfc1349Tos;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.UnknownPacket;
import org.pcap4j.packet.namednumber.IpNumber;
import org.pcap4j.packet.namednumber.IpVersion;
import org.pcap4j.packet.namednumber.TcpPort;
import org.pcap4j.packet.namednumber.UdpPort;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TrafficPacketParserTest {

    private final TrafficPacketParser parser = new TrafficPacketParser();

    private static Inet4Address inet4(String ip) throws UnknownHostException {
        return (Inet4Address) InetAddress.getByName(ip);
    }

    private static Packet buildTcpPacket(String srcIp, int srcPort, String dstIp, int dstPort) throws UnknownHostException {
        UnknownPacket.Builder payloadBuilder = new UnknownPacket.Builder().rawData(new byte[] {1, 2, 3});

        Inet4Address src = inet4(srcIp);
        Inet4Address dst = inet4(dstIp);

        TcpPacket.Builder tcpBuilder = new TcpPacket.Builder()
                .srcPort(TcpPort.getInstance((short) srcPort))
                .dstPort(TcpPort.getInstance((short) dstPort))
                .srcAddr(src)
                .dstAddr(dst)
                .sequenceNumber(0)
                .acknowledgmentNumber(0)
                .dataOffset((byte) 5)
                .reserved((byte) 0)
                .urg(false).ack(true).psh(false).rst(false).syn(false).fin(false)
                .window((short) 0)
                .urgentPointer((short) 0)
                .payloadBuilder(payloadBuilder)
                .correctChecksumAtBuild(false)
                .correctLengthAtBuild(true);

        IpV4Packet.Builder ipBuilder = new IpV4Packet.Builder()
                .version(IpVersion.IPV4)
                .tos(IpV4Rfc1349Tos.newInstance((byte) 0))
                .identification((short) 1)
                .ttl((byte) 64)
                .protocol(IpNumber.TCP)
                .srcAddr(src)
                .dstAddr(dst)
                .payloadBuilder(tcpBuilder)
                .correctChecksumAtBuild(false)
                .correctLengthAtBuild(true);

        return ipBuilder.build();
    }

    private static Packet buildUdpPacket(String srcIp, int srcPort, String dstIp, int dstPort) throws UnknownHostException {
        UnknownPacket.Builder payloadBuilder = new UnknownPacket.Builder().rawData(new byte[] {1, 2, 3});

        Inet4Address src = inet4(srcIp);
        Inet4Address dst = inet4(dstIp);

        UdpPacket.Builder udpBuilder = new UdpPacket.Builder()
                .srcPort(UdpPort.getInstance((short) srcPort))
                .dstPort(UdpPort.getInstance((short) dstPort))
                .srcAddr(src)
                .dstAddr(dst)
                .payloadBuilder(payloadBuilder)
                .correctChecksumAtBuild(false)
                .correctLengthAtBuild(true);

        IpV4Packet.Builder ipBuilder = new IpV4Packet.Builder()
                .version(IpVersion.IPV4)
                .tos(IpV4Rfc1349Tos.newInstance((byte) 0))
                .identification((short) 1)
                .ttl((byte) 64)
                .protocol(IpNumber.UDP)
                .srcAddr(src)
                .dstAddr(dst)
                .payloadBuilder(udpBuilder)
                .correctChecksumAtBuild(false)
                .correctLengthAtBuild(true);

        return ipBuilder.build();
    }

    @Test
    void parsesATcpPacketToAKnownEncryptedPort() throws UnknownHostException {
        Packet packet = buildTcpPacket("192.168.1.10", 54321, "93.184.216.34", 443);

        Optional<ParsedPacket> result = parser.parse(packet);

        assertThat(result).isPresent();
        ParsedPacket parsed = result.get();
        assertThat(parsed.srcIp()).isEqualTo("192.168.1.10");
        assertThat(parsed.dstIp()).isEqualTo("93.184.216.34");
        assertThat(parsed.dstPort()).isEqualTo(443);
        assertThat(parsed.protocol()).isEqualTo("TCP");
        assertThat(parsed.encrypted()).isTrue();
    }

    @Test
    void aTcpPacketToAPlainHttpPortIsNotEncrypted() throws UnknownHostException {
        Packet packet = buildTcpPacket("192.168.1.10", 54322, "1.2.3.4", 80);

        Optional<ParsedPacket> result = parser.parse(packet);

        assertThat(result).isPresent();
        assertThat(result.get().encrypted()).isFalse();
    }

    @Test
    void parsesAUdpPacket() throws UnknownHostException {
        Packet packet = buildUdpPacket("192.168.1.10", 53000, "8.8.8.8", 53);

        Optional<ParsedPacket> result = parser.parse(packet);

        assertThat(result).isPresent();
        assertThat(result.get().protocol()).isEqualTo("UDP");
        assertThat(result.get().dstPort()).isEqualTo(53);
    }
}
