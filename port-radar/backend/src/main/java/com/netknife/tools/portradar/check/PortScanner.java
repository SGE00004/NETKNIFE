package com.netknife.tools.portradar.check;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Conecta contra cada puerto de la lista curada y, si esta abierto, intenta
 * capturar un banner (lo que el servicio dice de si mismo al conectarse).
 * Muchos protocolos (SSH, FTP, SMTP...) envian un banner de forma pasiva nada
 * mas conectar; los que no (HTTP y similares) requieren mandar una peticion
 * minima antes de que respondan algo legible.
 */
@Component
public class PortScanner {

    /** Puertos donde el servicio no habla primero: hay que mandar una peticion HTTP minima. */
    private static final Set<Integer> HTTP_LIKE_PORTS = Set.of(80, 8080, 8888, 3000, 5000, 9200, 9090);

    private final int connectTimeoutMs;
    private final int bannerTimeoutMs;
    private final int scanConcurrency;

    public PortScanner(
            @Value("${netknife.port-radar.connect-timeout-ms:400}") int connectTimeoutMs,
            @Value("${netknife.port-radar.banner-timeout-ms:600}") int bannerTimeoutMs,
            @Value("${netknife.port-radar.scan-concurrency:16}") int scanConcurrency) {
        this.connectTimeoutMs = connectTimeoutMs;
        this.bannerTimeoutMs = bannerTimeoutMs;
        this.scanConcurrency = scanConcurrency;
    }

    public List<PortScanOutcome> scan(String target) {
        ExecutorService executor = Executors.newFixedThreadPool(scanConcurrency);
        try {
            List<Future<Optional<PortScanOutcome>>> futures = CuratedPortCatalog.all().stream()
                    .map(def -> executor.submit(() -> scanPort(target, def)))
                    .toList();

            return futures.stream()
                    .map(this::resolve)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } finally {
            executor.shutdown();
        }
    }

    private Optional<PortScanOutcome> resolve(Future<Optional<PortScanOutcome>> future) {
        try {
            return future.get(connectTimeoutMs + bannerTimeoutMs + 2000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    Optional<PortScanOutcome> scanPort(String target, PortDefinition def) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(target, def.port()), connectTimeoutMs);
            String banner = grabBanner(socket, def).orElse(null);
            return Optional.of(new PortScanOutcome(
                    def.port(), def.protocolLabel(), banner, def.riskIfOpen(),
                    def.plainLanguageRisk(), howToFixFor(def)));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Optional<String> grabBanner(Socket socket, PortDefinition def) {
        try {
            socket.setSoTimeout(bannerTimeoutMs);
            if (HTTP_LIKE_PORTS.contains(def.port())) {
                OutputStream out = socket.getOutputStream();
                out.write(("HEAD / HTTP/1.0\r\nHost: probe\r\nConnection: close\r\n\r\n")
                        .getBytes(StandardCharsets.US_ASCII));
                out.flush();
            }
            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[512];
            int read = in.read(buffer);
            if (read <= 0) {
                return Optional.empty();
            }
            String raw = new String(buffer, 0, read, StandardCharsets.ISO_8859_1).trim();
            String firstLine = raw.split("\\R", 2)[0];
            return firstLine.isBlank() ? Optional.empty() : Optional.of(truncate(firstLine, 200));
        } catch (IOException e) {
            // Timeout leyendo o conexion cerrada por el otro lado: el puerto esta abierto
            // (la conexion tuvo exito) pero no se pudo capturar un banner legible.
            return Optional.empty();
        }
    }

    private static String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...";
    }

    private static String howToFixFor(PortDefinition def) {
        return switch (def.riskIfOpen()) {
            case PELIGRO -> "Si no necesitas este servicio accesible desde fuera de tu equipo, desactivalo o "
                    + "bloquealo en el firewall. Si lo necesitas, restringe quien puede acceder (VPN, lista de IPs "
                    + "permitidas) y usa una contrasena fuerte.";
            case ATENCION -> "Revisa si realmente necesitas este servicio accesible y, si es asi, asegurate de "
                    + "que pide una contrasena fuerte y esta actualizado.";
            default -> null;
        };
    }
}
