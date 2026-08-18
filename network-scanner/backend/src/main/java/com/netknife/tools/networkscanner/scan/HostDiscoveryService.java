package com.netknife.tools.networkscanner.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Descubre hosts activos en la red local mediante un barrido de ping (para forzar
 * a que el sistema operativo resuelva sus direcciones MAC) seguido de la lectura
 * de la tabla ARP del propio sistema operativo.
 *
 * No se usan librerias nativas de captura de paquetes (pcap4j) ni herramientas
 * externas como nmap: solo el comando "ping" del sistema (presente en Windows,
 * macOS, Linux y en la imagen Alpine/BusyBox usada en Docker) y la tabla ARP,
 * leida directamente de /proc/net/arp en Linux o parseando "arp -a" como fallback
 * multiplataforma.
 */
@Component
public class HostDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(HostDiscoveryService.class);
    private static final Path PROC_NET_ARP = Path.of("/proc/net/arp");
    private static final Pattern ARP_LINE_PATTERN =
            Pattern.compile("\\(?(\\d{1,3}(?:\\.\\d{1,3}){3})\\)?\\s+.*?((?:[0-9a-fA-F]{2}[:-]){5}[0-9a-fA-F]{2})");

    private final int pingTimeoutMs;
    private final int scanConcurrency;

    public HostDiscoveryService(
            @Value("${netknife.scanner.ping-timeout-ms:800}") int pingTimeoutMs,
            @Value("${netknife.scanner.scan-concurrency:32}") int scanConcurrency) {
        this.pingTimeoutMs = pingTimeoutMs;
        this.scanConcurrency = scanConcurrency;
    }

    public List<DiscoveredHost> discoverHosts(List<String> candidateIps) {
        pingSweep(candidateIps);
        Map<String, String> arpTable = readArpTable();

        List<DiscoveredHost> hosts = new ArrayList<>();
        for (Map.Entry<String, String> entry : arpTable.entrySet()) {
            String ip = entry.getKey();
            if (!candidateIps.contains(ip)) {
                continue;
            }
            String mac = entry.getValue();
            String hostname = resolveHostname(ip);
            hosts.add(new DiscoveredHost(ip, mac, hostname));
        }
        return hosts;
    }

    /**
     * Resuelve la direccion MAC actual de una unica IP: le hace ping (para forzar
     * al SO a resolverla via ARP si no la tenia ya en cache) y lee su entrada en
     * la tabla ARP. Se usa para reconfirmar la MAC de una victima y para resolver
     * la MAC del gateway justo antes de iniciar un bloqueo, sin depender de que
     * un escaneo completo se haya ejecutado recientemente.
     */
    public Optional<String> resolveMacAddress(String ip) {
        pingOnce(ip);
        return Optional.ofNullable(readArpTable().get(ip));
    }

    private void pingSweep(List<String> ips) {
        ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, scanConcurrency));
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (String ip : ips) {
                futures.add(CompletableFuture.runAsync(() -> pingOnce(ip), executor));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(Math.max(5, ips.size() * pingTimeoutMs / Math.max(1, scanConcurrency) + 5000L), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("El barrido de ping no completo todos los hosts a tiempo, se continua con los datos disponibles", e);
        } finally {
            executor.shutdownNow();
        }
    }

    private void pingOnce(String ip) {
        try {
            List<String> command = buildPingCommand(ip);
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(pingTimeoutMs + 500L, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.debug("No se pudo hacer ping a {}: {}", ip, e.getMessage());
        }
    }

    private List<String> buildPingCommand(String ip) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return List.of("ping", "-n", "1", "-w", String.valueOf(pingTimeoutMs), ip);
        }
        // Linux/BusyBox/macOS: -c 1 (un paquete), -W timeout en segundos
        int timeoutSeconds = Math.max(1, pingTimeoutMs / 1000);
        return List.of("ping", "-c", "1", "-W", String.valueOf(timeoutSeconds), ip);
    }

    /**
     * Lee la tabla ARP del sistema operativo. En Linux se usa /proc/net/arp
     * directamente (sin depender de que exista el binario "arp"); en otros
     * sistemas se parsea la salida de "arp -a".
     */
    private Map<String, String> readArpTable() {
        if (Files.exists(PROC_NET_ARP)) {
            return readArpTableFromProcFs();
        }
        return readArpTableFromCommand();
    }

    private Map<String, String> readArpTableFromProcFs() {
        Map<String, String> table = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(PROC_NET_ARP, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // cabecera
            while ((line = reader.readLine()) != null) {
                String[] columns = line.trim().split("\\s+");
                if (columns.length >= 4) {
                    String ip = columns[0];
                    String mac = columns[3];
                    if (!mac.equals("00:00:00:00:00:00")) {
                        table.put(ip, mac.toUpperCase(Locale.ROOT));
                    }
                }
            }
        } catch (IOException e) {
            log.warn("No se pudo leer /proc/net/arp: {}", e.getMessage());
        }
        return table;
    }

    private Map<String, String> readArpTableFromCommand() {
        Map<String, String> table = new HashMap<>();
        try {
            Process process = new ProcessBuilder("arp", "-a").redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = ARP_LINE_PATTERN.matcher(line);
                    if (matcher.find()) {
                        String ip = matcher.group(1);
                        String mac = matcher.group(2).replace('-', ':').toUpperCase(Locale.ROOT);
                        table.put(ip, mac);
                    }
                }
            }
            process.waitFor(5, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("No se pudo ejecutar 'arp -a': {}", e.getMessage());
        }
        return table;
    }

    private String resolveHostname(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            String canonical = address.getCanonicalHostName();
            if (canonical != null && !canonical.equals(ip)) {
                return canonical;
            }
        } catch (Exception e) {
            log.debug("No se pudo resolver hostname para {}: {}", ip, e.getMessage());
        }
        return null;
    }
}
