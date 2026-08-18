package com.netknife.tools.trafficanalyzer.capture;

import com.netknife.common.exception.ActionRejectedException;
import com.netknife.common.lifecycle.ShutdownHook;
import jakarta.annotation.PreDestroy;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Instant;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona el ciclo de vida de la captura de trafico: abrir la interfaz, escuchar
 * en un hilo dedicado, agregar las conexiones activas en memoria, y cerrar todo al
 * detener o al apagar la app. No reutiliza PcapHandleProvider (network-scanner):
 * ese provider abre un handle compartido solo para ENVIAR paquetes ARP con un
 * ciclo de vida "abrir una vez, compartir, cerrar cuando no hay bloqueos activos";
 * este manager necesita un ciclo de vida distinto (un hilo de LECTURA continua
 * mientras la captura este activa, con start()/stop() explicitos del usuario).
 *
 * Se abre en modo promiscuo (igual que PcapHandleProvider) porque en algunos
 * setups de Npcap en Windows hace falta para ver de forma fiable el trafico
 * saliente del propio equipo, pero se filtra explicitamente cualquier paquete
 * cuyo IP origen/destino no sea una direccion local: el objetivo es "con quien
 * habla tu ordenador", no todo el trafico de la LAN visible en modo promiscuo.
 */
@Component
public class TrafficCaptureManager implements ShutdownHook {

    private static final Logger log = LoggerFactory.getLogger(TrafficCaptureManager.class);
    private static final int SNAPSHOT_LENGTH = 65536;
    private static final int READ_TIMEOUT_MS = 100;

    private final LocalInterfaceSelector interfaceSelector;
    private final TrafficPacketParser packetParser;

    private volatile PcapHandle handle;
    private volatile Thread captureThread;
    private volatile Set<String> localAddresses = Set.of();
    private final Map<ConnectionKey, ActiveConnection> activeConnections = new ConcurrentHashMap<>();

    public TrafficCaptureManager(LocalInterfaceSelector interfaceSelector, TrafficPacketParser packetParser) {
        this.interfaceSelector = interfaceSelector;
        this.packetParser = packetParser;
    }

    public synchronized void start() {
        if (isRunning()) {
            return;
        }
        PcapNetworkInterface nif = interfaceSelector.selectDefault()
                .orElseThrow(() -> new ActionRejectedException(
                        "No se ha encontrado una interfaz de red por la que capturar trafico."));
        try {
            handle = nif.openLive(SNAPSHOT_LENGTH, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIMEOUT_MS);
        } catch (PcapNativeException e) {
            throw new ActionRejectedException(
                    "No se ha podido abrir la interfaz de red para analizar el trafico: " + e.getMessage());
        }
        localAddresses = collectLocalAddresses();
        activeConnections.clear();
        captureThread = new Thread(this::runCaptureLoop, "netknife-traffic-capture");
        captureThread.setDaemon(true);
        captureThread.start();
    }

    public synchronized void stop() {
        PcapHandle handleToClose = handle;
        Thread threadToStop = captureThread;
        handle = null;
        captureThread = null;
        if (handleToClose != null && handleToClose.isOpen()) {
            try {
                handleToClose.breakLoop();
            } catch (NotOpenException e) {
                log.debug("El handle ya estaba cerrado al intentar detener la captura: {}", e.getMessage());
            }
        }
        if (threadToStop != null) {
            threadToStop.interrupt();
        }
        if (handleToClose != null) {
            handleToClose.close();
        }
        activeConnections.clear();
    }

    public boolean isRunning() {
        PcapHandle current = handle;
        return current != null && current.isOpen();
    }

    public Collection<ActiveConnection> snapshot() {
        return List.copyOf(activeConnections.values());
    }

    private void runCaptureLoop() {
        PcapHandle localHandle = handle;
        if (localHandle == null) {
            return;
        }
        try {
            localHandle.loop(-1, (PacketListener) this::onPacket);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (PcapNativeException | NotOpenException e) {
            log.debug("Captura de trafico interrumpida: {}", e.getMessage());
        }
    }

    private void onPacket(Packet packet) {
        packetParser.parse(packet).ifPresent(this::updateActiveConnections);
    }

    private void updateActiveConnections(ParsedPacket parsed) {
        boolean srcIsLocal = localAddresses.contains(parsed.srcIp());
        boolean dstIsLocal = localAddresses.contains(parsed.dstIp());
        if (!srcIsLocal && !dstIsLocal) {
            return;
        }
        String remoteIp = srcIsLocal ? parsed.dstIp() : parsed.srcIp();
        int remotePort = srcIsLocal ? parsed.dstPort() : parsed.srcPort();
        int localPort = srcIsLocal ? parsed.srcPort() : parsed.dstPort();

        ConnectionKey key = new ConnectionKey(parsed.protocol(), localPort, remoteIp, remotePort);
        Instant now = Instant.now();
        activeConnections.compute(key, (k, existing) -> {
            if (existing == null) {
                return new ActiveConnection(k, parsed.encrypted(), now);
            }
            existing.recordPacket(parsed.encrypted(), parsed.lengthBytes(), now);
            return existing;
        });
    }

    private Set<String> collectLocalAddresses() {
        Set<String> addresses = new HashSet<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                Enumeration<InetAddress> addrs = interfaces.nextElement().getInetAddresses();
                while (addrs.hasMoreElements()) {
                    addresses.add(addrs.nextElement().getHostAddress());
                }
            }
        } catch (SocketException e) {
            log.debug("No se pudieron enumerar las direcciones IP locales: {}", e.getMessage());
        }
        return addresses;
    }

    @Override
    public void onShutdown() {
        stop();
    }

    @PreDestroy
    public void stopOnDestroy() {
        stop();
    }
}
