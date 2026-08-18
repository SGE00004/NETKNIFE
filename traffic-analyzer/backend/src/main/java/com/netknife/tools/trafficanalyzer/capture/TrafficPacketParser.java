package com.netknife.tools.trafficanalyzer.capture;

import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Extrae de un paquete capturado por pcap4j lo que necesita el analizador de
 * trafico: IPs y puertos origen/destino, protocolo de transporte, y si "parece"
 * cifrado. Puro y sin estado: no abre ningun handle, testeable con paquetes
 * sinteticos construidos con los builders de pcap4j.
 *
 * "Va cifrado" se decide por si el puerto de origen o destino es uno bien conocido
 * de un protocolo cifrado (HTTPS, DoT, IMAPS, POP3S, SMTPS, LDAPS...), una
 * heuristica deliberadamente simple: NO se inspecciona el contenido del paquete
 * (p.ej. el ClientHello de un handshake TLS), asi que un servicio cifrado en un
 * puerto no estandar se clasificaria como "sin cifrar". Limitacion conocida y
 * aceptada, documentada aqui para quien la revise despues.
 */
@Component
public class TrafficPacketParser {

    private static final Set<Integer> ENCRYPTED_PORTS = Set.of(443, 853, 993, 995, 465, 636);

    public Optional<ParsedPacket> parse(Packet packet) {
        IpV4Packet ipv4 = packet.get(IpV4Packet.class);
        IpV6Packet ipv6 = packet.get(IpV6Packet.class);
        if (ipv4 == null && ipv6 == null) {
            return Optional.empty();
        }
        String srcIp = ipv4 != null
                ? ipv4.getHeader().getSrcAddr().getHostAddress()
                : ipv6.getHeader().getSrcAddr().getHostAddress();
        String dstIp = ipv4 != null
                ? ipv4.getHeader().getDstAddr().getHostAddress()
                : ipv6.getHeader().getDstAddr().getHostAddress();

        TcpPacket tcp = packet.get(TcpPacket.class);
        UdpPacket udp = packet.get(UdpPacket.class);
        if (tcp == null && udp == null) {
            // ICMP y otros protocolos de transporte quedan fuera de alcance: no tienen
            // puerto, y "quien habla con quien" se expresa mejor a nivel de conexion TCP/UDP.
            return Optional.empty();
        }

        int srcPort = tcp != null ? tcp.getHeader().getSrcPort().valueAsInt() : udp.getHeader().getSrcPort().valueAsInt();
        int dstPort = tcp != null ? tcp.getHeader().getDstPort().valueAsInt() : udp.getHeader().getDstPort().valueAsInt();
        String protocol = tcp != null ? "TCP" : "UDP";
        boolean encrypted = ENCRYPTED_PORTS.contains(dstPort) || ENCRYPTED_PORTS.contains(srcPort);

        return Optional.of(new ParsedPacket(srcIp, srcPort, dstIp, dstPort, protocol, encrypted, packet.length()));
    }
}
