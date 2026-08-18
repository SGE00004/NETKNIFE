package com.netknife.tools.networkscanner.blocking;

import com.netknife.common.lifecycle.ShutdownHook;
import jakarta.annotation.PreDestroy;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestiona el ciclo de vida de los bloqueos activos: una tarea programada
 * ({@link ArpPoisonTask}) por dispositivo bloqueado, reenviada cada
 * {@code poison-interval-seconds} mientras el bloqueo dure.
 *
 * Solo vive en memoria: si el backend se reinicia, todos los bloqueos activos
 * desaparecen junto con sus hilos (la cache ARP de los dispositivos afectados se
 * autocorrige sola en su propio timeout). Por eso {@code DeviceBlockingService}
 * resetea el flag "blocked" de cualquier dispositivo al arrancar: ya no hay ninguna
 * tarea de esta ejecucion manteniendolo bloqueado.
 */
@Component
public class DeviceBlockingManager implements ShutdownHook {

    private static final Logger log = LoggerFactory.getLogger(DeviceBlockingManager.class);
    private static final long CORRECTION_BURST_DELAY_MS = 300;

    private record ActiveBlock(HostAddress victim, HostAddress gateway, MacAddress ownMac, String ownIpAddress,
                                ScheduledFuture<?> future) {
    }

    private final PcapHandleProvider pcapHandleProvider;
    private final ArpPacketBuilder packetBuilder;
    private final int poisonIntervalSeconds;
    private final int unblockCorrectionBursts;
    private final ScheduledExecutorService scheduler;
    private final Map<Long, ActiveBlock> activeBlocks = new ConcurrentHashMap<>();

    public DeviceBlockingManager(
            PcapHandleProvider pcapHandleProvider,
            ArpPacketBuilder packetBuilder,
            @Value("${netknife.scanner.blocking.poison-interval-seconds:2}") int poisonIntervalSeconds,
            @Value("${netknife.scanner.blocking.unblock-correction-bursts:3}") int unblockCorrectionBursts) {
        this.pcapHandleProvider = pcapHandleProvider;
        this.packetBuilder = packetBuilder;
        this.poisonIntervalSeconds = poisonIntervalSeconds;
        this.unblockCorrectionBursts = unblockCorrectionBursts;
        this.scheduler = Executors.newScheduledThreadPool(4, daemonThreadFactory());
    }

    private static ThreadFactory daemonThreadFactory() {
        AtomicInteger counter = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, "netknife-arp-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    public boolean isBlocking(Long deviceId) {
        return activeBlocks.containsKey(deviceId);
    }

    public synchronized void startBlocking(Long deviceId, HostAddress victim, HostAddress gateway,
                                            String ownMac, String ownIpAddress) {
        if (activeBlocks.containsKey(deviceId)) {
            return;
        }
        PcapHandle handle = pcapHandleProvider.getOrOpenHandle(ownIpAddress);
        MacAddress ownMacAddress = MacAddress.getByName(ownMac);
        ArpPoisonTask task = new ArpPoisonTask(victim, gateway, ownMacAddress, handle, packetBuilder);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, 0, poisonIntervalSeconds, TimeUnit.SECONDS);
        activeBlocks.put(deviceId, new ActiveBlock(victim, gateway, ownMacAddress, ownIpAddress, future));
    }

    public synchronized void stopBlocking(Long deviceId) {
        ActiveBlock block = activeBlocks.remove(deviceId);
        if (block == null) {
            return;
        }
        block.future().cancel(true);
        sendCorrectiveArp(block);
        pcapHandleProvider.closeHandleIfUnused(!activeBlocks.isEmpty());
    }

    /**
     * Manda una rafaga de ARP gratuitos con las direcciones reales, tanto a la
     * victima como al gateway, para restaurar la conectividad de inmediato en vez
     * de esperar a que expire la cache ARP del sistema operativo por si sola.
     */
    private void sendCorrectiveArp(ActiveBlock block) {
        try {
            PcapHandle handle = pcapHandleProvider.getOrOpenHandle(block.ownIpAddress());
            MacAddress victimMac = MacAddress.getByName(block.victim().macAddress());
            MacAddress gatewayMac = MacAddress.getByName(block.gateway().macAddress());
            Inet4Address victimIp = toInet4(block.victim().ipAddress());
            Inet4Address gatewayIp = toInet4(block.gateway().ipAddress());

            for (int i = 0; i < unblockCorrectionBursts; i++) {
                // A quien escuche: "el gateway esta realmente en su propia MAC" y
                // "la victima esta realmente en su propia MAC".
                Packet gatewayAnnouncement = packetBuilder.buildGratuitousArpReply(gatewayMac, gatewayIp);
                Packet victimAnnouncement = packetBuilder.buildGratuitousArpReply(victimMac, victimIp);
                handle.sendPacket(gatewayAnnouncement);
                handle.sendPacket(victimAnnouncement);
                if (i < unblockCorrectionBursts - 1) {
                    Thread.sleep(CORRECTION_BURST_DELAY_MS);
                }
            }
        } catch (PcapNativeException | NotOpenException e) {
            log.warn("No se pudo enviar la correccion ARP al desbloquear el dispositivo: {}", e.getMessage());
        } catch (UnknownHostException e) {
            log.warn("Direccion IP invalida al enviar la correccion ARP: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static Inet4Address toInet4(String ip) throws UnknownHostException {
        return (Inet4Address) InetAddress.getByName(ip);
    }

    /**
     * Implementacion de {@link ShutdownHook}: la invoca el endpoint
     * {@code POST /api/system/shutdown} (a traves de la lista de hooks que recolecta
     * el modulo ensamblador, sin conocer esta clase concreta), que es el mecanismo
     * PRINCIPAL de restauracion al cerrar la app de escritorio, ya que en Windows
     * matar el proceso hijo de Tauri no dispara el shutdown hook de la JVM de abajo.
     */
    @Override
    public void onShutdown() {
        stopAll();
    }

    /**
     * Red de seguridad secundaria para cierres que si disparan el ciclo de vida
     * normal de Spring (p.ej. Ctrl+C en desarrollo).
     */
    @PreDestroy
    public void stopAll() {
        if (!activeBlocks.isEmpty()) {
            log.info("Restaurando la conectividad de {} dispositivo(s) bloqueado(s) antes de apagar", activeBlocks.size());
            for (Long deviceId : activeBlocks.keySet().toArray(new Long[0])) {
                stopBlocking(deviceId);
            }
        }
        scheduler.shutdownNow();
    }
}
