package com.netknife.tools.networkscanner.scan;

import com.netknife.common.exception.ScanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Detecta la subred IPv4 local del host a partir de sus interfaces de red,
 * y calcula el rango de direcciones a escanear.
 *
 * Se evita deliberadamente cualquier libreria nativa (pcap4j, JNI...): solo
 * se usa java.net, que es portable sin dependencias extra.
 */
@Component
public class NetworkRangeDetector {

    private static final Logger log = LoggerFactory.getLogger(NetworkRangeDetector.class);

    /**
     * Nombres de adaptador que casi nunca son "la red a la que estas conectado":
     * hipervisores, VPNs, contenedores... Se comparan en minusculas contra el
     * nombre y la descripcion del adaptador.
     */
    private static final List<String> VIRTUAL_ADAPTER_PATTERNS = List.of(
            "virtualbox", "vmware", "hyper-v", "virtual", "vethernet", "docker",
            "npcap", "tap-windows", "tailscale", "zerotier", "wsl", "bluetooth",
            "ppp", "isatap", "teredo", "vpn"
    );

    /**
     * Nombres que sugieren un adaptador de red fisico "normal": se usan como
     * criterio de desempate cuando no se puede confiar en la deteccion de ruta
     * de salida.
     */
    private static final List<String> REAL_ADAPTER_HINTS = List.of(
            "wi-fi", "wifi", "wireless", "wlan", "ethernet", "lan"
    );

    private record CandidateInterface(String ipAddress, short prefixLength, String displayName) {
        LocalNetworkInfo toLocalNetworkInfo() {
            return new LocalNetworkInfo(ipAddress, prefixLength, ipAddress + "/" + prefixLength, displayName);
        }
    }

    /**
     * Detecta la red local activa: la que el propio sistema operativo usaria
     * para salir a internet (Wi-Fi, Ethernet...) ahora mismo.
     *
     * Un equipo suele tener varias interfaces "activas" simultaneamente (Wi-Fi,
     * Ethernet, adaptadores virtuales de Docker/VPN/hipervisores...), y quedarse
     * con "la primera que aparezca" es poco fiable. Se combinan dos senales:
     * 1) se pregunta al sistema operativo cual es la interfaz de salida real
     *    (mediante un socket UDP "conectado" a una IP publica, que no llega a
     *    enviar ningun paquete pero fuerza al SO a resolver la ruta de salida);
     * 2) se descartan explicitamente los adaptadores virtuales conocidos
     *    (VirtualBox, VMware, Hyper-V, Docker...), porque en maquinas con
     *    hipervisores instalados la deteccion de ruta de salida puede
     *    equivocarse y devolver un adaptador "host-only" sin salida real.
     */
    public LocalNetworkInfo detectLocalNetwork() {
        List<CandidateInterface> candidates = collectCandidateInterfaces();
        if (candidates.isEmpty()) {
            throw new ScanException(
                    "No se ha detectado ninguna red local activa. Comprueba que el dispositivo esta conectado a una red.");
        }

        String primaryIp = detectPrimaryOutboundAddress();
        if (primaryIp != null) {
            Optional<CandidateInterface> match = candidates.stream()
                    .filter(c -> c.ipAddress().equals(primaryIp))
                    .findFirst();
            if (match.isPresent()) {
                return match.get().toLocalNetworkInfo();
            }
            log.debug("La IP de salida detectada ({}) no coincide con ningun adaptador fisico reconocido", primaryIp);
        }

        return candidates.stream()
                .filter(c -> looksLikeRealAdapter(c.displayName()))
                .findFirst()
                .orElse(candidates.get(0))
                .toLocalNetworkInfo();
    }

    /**
     * Pregunta al sistema operativo que direccion local usaria para salir a
     * internet, sin enviar realmente ningun paquete (los sockets UDP no
     * establecen conexion al "conectarse"). Es una senal fuerte pero no
     * infalible: en maquinas con hipervisores puede apuntar a un adaptador
     * virtual, por eso su resultado se valida despues contra la lista de
     * adaptadores candidatos (no virtuales).
     */
    private String detectPrimaryOutboundAddress() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 53);
            InetAddress local = socket.getLocalAddress();
            if (local != null && !local.isAnyLocalAddress() && !local.isLoopbackAddress()) {
                return local.getHostAddress();
            }
        } catch (Exception e) {
            log.debug("No se pudo determinar la interfaz de salida via socket UDP: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Enumera las interfaces de red IPv4 activas, no loopback y que no parezcan
     * adaptadores virtuales (hipervisores, VPNs, contenedores...).
     */
    private List<CandidateInterface> collectCandidateInterfaces() {
        List<CandidateInterface> candidates = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || isKnownVirtualAdapter(iface)) {
                    continue;
                }
                for (InterfaceAddress ifaceAddress : iface.getInterfaceAddresses()) {
                    InetAddress address = ifaceAddress.getAddress();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        candidates.add(new CandidateInterface(
                                address.getHostAddress(),
                                ifaceAddress.getNetworkPrefixLength(),
                                iface.getDisplayName()));
                    }
                }
            }
        } catch (SocketException e) {
            throw new ScanException("No se ha podido leer las interfaces de red del sistema", e);
        }
        return candidates;
    }

    private boolean isKnownVirtualAdapter(NetworkInterface iface) {
        String name = (iface.getName() + " " + iface.getDisplayName()).toLowerCase(Locale.ROOT);
        return VIRTUAL_ADAPTER_PATTERNS.stream().anyMatch(name::contains);
    }

    private boolean looksLikeRealAdapter(String displayName) {
        if (displayName == null) {
            return false;
        }
        String name = displayName.toLowerCase(Locale.ROOT);
        return REAL_ADAPTER_HINTS.stream().anyMatch(name::contains);
    }

    /**
     * Resuelve la direccion MAC de la interfaz de red local (identificada por su
     * "display name", tal y como lo devuelve {@link #detectLocalNetwork()}), formateada
     * en mayusculas separada por dos puntos. Se usa para construir los paquetes ARP
     * falsificados del bloqueo de dispositivos: sin la MAC propia no se puede decir
     * "yo soy el gateway/la victima".
     */
    public Optional<String> resolveOwnMacAddress(String interfaceDisplayName) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.getDisplayName().equals(interfaceDisplayName)) {
                    continue;
                }
                byte[] hardwareAddress = iface.getHardwareAddress();
                if (hardwareAddress == null) {
                    return Optional.empty();
                }
                return Optional.of(formatMacAddress(hardwareAddress));
            }
        } catch (SocketException e) {
            log.debug("No se pudo leer la direccion MAC de la interfaz {}: {}", interfaceDisplayName, e.getMessage());
        }
        return Optional.empty();
    }

    private String formatMacAddress(byte[] hardwareAddress) {
        StringBuilder sb = new StringBuilder();
        for (byte b : hardwareAddress) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(String.format(Locale.ROOT, "%02X", b));
        }
        return sb.toString();
    }

    /**
     * Calcula la lista de direcciones IPv4 host dentro del rango dado por ip+prefixLength,
     * excluyendo la direccion de red y la de broadcast, y limitando el total a maxHosts
     * (empezando por el mismo /24 que contiene a la IP local si el rango es mas amplio).
     */
    public List<String> computeHostAddresses(String ip, short prefixLength, int maxHosts) {
        if (prefixLength < 16) {
            // Rango demasiado amplio (ej. /8): nos ceñimos al /24 que contiene la IP local
            // para evitar barrer millones de direcciones.
            prefixLength = 24;
        }

        long ipAsLong = ipToLong(ip);
        long mask = prefixLength == 0 ? 0 : (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
        long network = ipAsLong & mask;
        long broadcast = network | (~mask & 0xFFFFFFFFL);

        List<String> hosts = new ArrayList<>();
        for (long current = network + 1; current < broadcast && hosts.size() < maxHosts; current++) {
            hosts.add(longToIp(current));
        }
        return hosts;
    }

    private long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new ScanException("Direccion IPv4 invalida: " + ip);
        }
        long result = 0;
        for (String part : parts) {
            result = (result << 8) | Integer.parseInt(part);
        }
        return result;
    }

    private String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
    }
}
