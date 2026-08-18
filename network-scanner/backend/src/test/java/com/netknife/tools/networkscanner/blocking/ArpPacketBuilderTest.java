package com.netknife.tools.networkscanner.blocking;

import org.junit.jupiter.api.Test;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.MacAddress;

import java.net.Inet4Address;
import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ArpPacketBuilder solo construye paquetes en memoria (no abre ninguna interfaz de
 * red), asi que estos tests son deterministas y no requieren Npcap ni privilegios.
 */
class ArpPacketBuilderTest {

    private final ArpPacketBuilder builder = new ArpPacketBuilder();

    @Test
    void spoofedArpReplyClaimsTheSpoofedAddressToTheTarget() throws Exception {
        MacAddress spoofedMac = MacAddress.getByName("AA:AA:AA:AA:AA:AA");
        Inet4Address spoofedIp = inet4("192.168.1.1");
        MacAddress targetMac = MacAddress.getByName("BB:BB:BB:BB:BB:BB");
        Inet4Address targetIp = inet4("192.168.1.50");

        Packet packet = builder.buildSpoofedArpReply(spoofedMac, spoofedIp, targetMac, targetIp);

        EthernetPacket ethernet = (EthernetPacket) packet;
        assertThat(ethernet.getHeader().getSrcAddr()).isEqualTo(spoofedMac);
        assertThat(ethernet.getHeader().getDstAddr()).isEqualTo(targetMac);
        assertThat(ethernet.getHeader().getType()).isEqualTo(EtherType.ARP);

        ArpPacket arp = (ArpPacket) ethernet.getPayload();
        assertThat(arp.getHeader().getOperation()).isEqualTo(ArpOperation.REPLY);
        assertThat(arp.getHeader().getSrcHardwareAddr()).isEqualTo(spoofedMac);
        assertThat(arp.getHeader().getSrcProtocolAddr()).isEqualTo(spoofedIp);
        assertThat(arp.getHeader().getDstHardwareAddr()).isEqualTo(targetMac);
        assertThat(arp.getHeader().getDstProtocolAddr()).isEqualTo(targetIp);
    }

    @Test
    void gratuitousArpReplyAnnouncesTheRealAddressToBroadcast() throws Exception {
        MacAddress realMac = MacAddress.getByName("CC:CC:CC:CC:CC:CC");
        Inet4Address realIp = inet4("192.168.1.30");

        Packet packet = builder.buildGratuitousArpReply(realMac, realIp);

        EthernetPacket ethernet = (EthernetPacket) packet;
        assertThat(ethernet.getHeader().getSrcAddr()).isEqualTo(realMac);
        assertThat(ethernet.getHeader().getDstAddr()).isEqualTo(MacAddress.ETHER_BROADCAST_ADDRESS);

        ArpPacket arp = (ArpPacket) ethernet.getPayload();
        assertThat(arp.getHeader().getSrcHardwareAddr()).isEqualTo(realMac);
        assertThat(arp.getHeader().getSrcProtocolAddr()).isEqualTo(realIp);
        // Gratuito: el propio host anuncia su direccion, no responde a nadie en concreto.
        assertThat(arp.getHeader().getDstProtocolAddr()).isEqualTo(realIp);
    }

    private static Inet4Address inet4(String ip) throws Exception {
        return (Inet4Address) InetAddress.getByName(ip);
    }
}
