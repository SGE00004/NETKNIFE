package com.netknife.tools.networkscanner.blocking;

import org.pcap4j.core.PcapHandle;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Una instancia por dispositivo bloqueado: en cada ejecucion (programada de forma
 * periodica por {@link DeviceBlockingManager}) envia el par de ARP falsos que
 * mantiene el bloqueo activo. Los fallos de envio se registran como aviso y no
 * deben propagarse: una excepcion aqui no debe detener el scheduler, ya que el
 * siguiente ciclo lo reintentara.
 */
final class ArpPoisonTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ArpPoisonTask.class);

    private final HostAddress victim;
    private final HostAddress gateway;
    private final MacAddress ownMac;
    private final PcapHandle handle;
    private final ArpPacketBuilder packetBuilder;

    ArpPoisonTask(HostAddress victim, HostAddress gateway, MacAddress ownMac, PcapHandle handle,
                  ArpPacketBuilder packetBuilder) {
        this.victim = victim;
        this.gateway = gateway;
        this.ownMac = ownMac;
        this.handle = handle;
        this.packetBuilder = packetBuilder;
    }

    @Override
    public void run() {
        try {
            // A la victima: "el gateway esta en nuestra MAC" (no en la real).
            Packet toVictim = packetBuilder.buildSpoofedArpReply(
                    ownMac, toInet4(gateway.ipAddress()), macOf(victim), toInet4(victim.ipAddress()));
            handle.sendPacket(toVictim);

            // Al gateway: "la victima esta en nuestra MAC" (no en la real).
            Packet toGateway = packetBuilder.buildSpoofedArpReply(
                    ownMac, toInet4(victim.ipAddress()), macOf(gateway), toInet4(gateway.ipAddress()));
            handle.sendPacket(toGateway);
        } catch (Exception e) {
            log.warn("Fallo al enviar un paquete ARP de bloqueo (se reintentara en el siguiente ciclo): {}", e.getMessage());
        }
    }

    private static MacAddress macOf(HostAddress host) {
        return MacAddress.getByName(host.macAddress());
    }

    private static Inet4Address toInet4(String ip) throws UnknownHostException {
        return (Inet4Address) InetAddress.getByName(ip);
    }
}
