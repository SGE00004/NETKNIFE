package com.netknife.tools.trafficanalyzer.capture;

import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * Elige la interfaz de red por defecto para capturar. Primero intenta la interfaz
 * asociada a la IP local del equipo (InetAddress.getLocalHost()); si eso falla,
 * recorre todas las interfaces y usa la primera activa (no loopback) con una
 * direccion IPv4.
 *
 * Limitacion conocida: en equipos con varias interfaces de red activas a la vez
 * (ej. WiFi + Ethernet + una VPN), podria elegir una que no sea la que el usuario
 * espera. No hay selector de interfaz expuesto en el frontend en esta iteracion.
 */
@Component
public class LocalInterfaceSelector {

    private static final Logger log = LoggerFactory.getLogger(LocalInterfaceSelector.class);

    public Optional<PcapNetworkInterface> selectDefault() {
        Optional<PcapNetworkInterface> byLocalAddress = selectByLocalAddress();
        if (byLocalAddress.isPresent()) {
            return byLocalAddress;
        }
        return selectFirstActiveNonLoopback();
    }

    private Optional<PcapNetworkInterface> selectByLocalAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return Optional.ofNullable(Pcaps.getDevByAddress(localHost));
        } catch (UnknownHostException | PcapNativeException e) {
            log.debug("No se pudo resolver la interfaz por la IP local: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<PcapNetworkInterface> selectFirstActiveNonLoopback() {
        try {
            for (PcapNetworkInterface nif : Pcaps.findAllDevs()) {
                if (nif.isLoopBack()) {
                    continue;
                }
                for (PcapAddress address : nif.getAddresses()) {
                    if (address.getAddress() instanceof Inet4Address) {
                        return Optional.of(nif);
                    }
                }
            }
        } catch (PcapNativeException e) {
            log.debug("No se pudieron listar las interfaces de red: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
