package com.netknife.tools.networkscanner.blocking;

import com.netknife.common.exception.DeviceBlockingException;
import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Abre y reutiliza un unico {@link PcapHandle} (modo promiscuo) sobre la interfaz de
 * red local, compartido por todas las tareas de bloqueo activas: abrir un handle por
 * dispositivo bloqueado seria innecesario, ya que todos envian por la misma NIC.
 *
 * La interfaz se localiza por una de sus direcciones IP (no por nombre): pcap4j usa
 * en Windows nombres internos ("\Device\NPF_{GUID}") que no coinciden con el
 * "display name" que devuelve {@code java.net.NetworkInterface}, así que emparejar
 * por IP es el criterio fiable multiplataforma.
 */
@Component
public class PcapHandleProvider {

    private static final Logger log = LoggerFactory.getLogger(PcapHandleProvider.class);
    private static final int SNAPSHOT_LENGTH = 65536;
    private static final int READ_TIMEOUT_MS = 50;

    private PcapHandle handle;

    public synchronized PcapHandle getOrOpenHandle(String localIpAddress) {
        if (handle != null && handle.isOpen()) {
            return handle;
        }
        try {
            PcapNetworkInterface nif = findInterfaceByAddress(localIpAddress);
            if (nif == null) {
                throw new DeviceBlockingException(
                        "No se ha encontrado la interfaz de red para la direccion " + localIpAddress);
            }
            handle = nif.openLive(SNAPSHOT_LENGTH, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIMEOUT_MS);
            log.debug("Handle pcap abierto en modo promiscuo sobre la interfaz con IP {}", localIpAddress);
            return handle;
        } catch (PcapNativeException e) {
            throw new DeviceBlockingException("No se ha podido abrir la interfaz de red para el bloqueo: " + e.getMessage(), e);
        }
    }

    public synchronized void closeHandleIfUnused(boolean anyActiveBlocks) {
        if (anyActiveBlocks || handle == null) {
            return;
        }
        handle.close();
        handle = null;
        log.debug("Handle pcap cerrado: no quedan bloqueos activos que lo necesiten");
    }

    private PcapNetworkInterface findInterfaceByAddress(String ip) throws PcapNativeException {
        for (PcapNetworkInterface nif : Pcaps.findAllDevs()) {
            for (PcapAddress addr : nif.getAddresses()) {
                if (addr.getAddress() != null && ip.equals(addr.getAddress().getHostAddress())) {
                    return nif;
                }
            }
        }
        return null;
    }
}
