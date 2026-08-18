package com.netknife.tools.networkscanner.blocking;

import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpHardwareType;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.MacAddress;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;

/**
 * Construye los paquetes ARP (envueltos en Ethernet) usados por el bloqueo de
 * dispositivos. No conoce nada de scheduling ni de red real: solo construye
 * paquetes en memoria, lo que la hace 100% testeable sin abrir ninguna interfaz.
 */
@Component
public class ArpPacketBuilder {

    /**
     * ARP reply falso dirigido especificamente a un host: "la IP {spoofedIp} esta en
     * la MAC {spoofedMac}". Es la pieza basica del envenenamiento: se usa tanto para
     * decirle a la victima que el gateway esta en nuestra MAC, como para decirle al
     * gateway que la victima esta en nuestra MAC.
     */
    public Packet buildSpoofedArpReply(MacAddress spoofedMac, Inet4Address spoofedIp,
                                        MacAddress targetMac, Inet4Address targetIp) {
        return buildArpReply(spoofedMac, spoofedIp, targetMac, targetIp);
    }

    /**
     * ARP reply gratuito (broadcast) anunciando la correspondencia real IP-MAC de un
     * host. Se usa para restaurar de inmediato las tablas ARP de la victima y del
     * gateway al desbloquear un dispositivo, en vez de esperar a que su cache expire
     * por si sola.
     */
    public Packet buildGratuitousArpReply(MacAddress realMac, Inet4Address realIp) {
        return buildArpReply(realMac, realIp, MacAddress.ETHER_BROADCAST_ADDRESS, realIp);
    }

    private Packet buildArpReply(MacAddress srcMac, Inet4Address srcIp, MacAddress dstMac, Inet4Address dstIp) {
        ArpPacket.Builder arpBuilder = new ArpPacket.Builder();
        arpBuilder.hardwareType(ArpHardwareType.ETHERNET)
                .protocolType(EtherType.IPV4)
                .hardwareAddrLength((byte) MacAddress.SIZE_IN_BYTES)
                .protocolAddrLength((byte) 4)
                .operation(ArpOperation.REPLY)
                .srcHardwareAddr(srcMac)
                .srcProtocolAddr(srcIp)
                .dstHardwareAddr(dstMac)
                .dstProtocolAddr(dstIp);

        EthernetPacket.Builder etherBuilder = new EthernetPacket.Builder();
        etherBuilder.dstAddr(dstMac)
                .srcAddr(srcMac)
                .type(EtherType.ARP)
                .payloadBuilder(arpBuilder)
                .paddingAtBuild(true);

        return etherBuilder.build();
    }
}
