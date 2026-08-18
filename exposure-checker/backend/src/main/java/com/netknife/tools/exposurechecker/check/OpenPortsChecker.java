package com.netknife.tools.exposurechecker.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

/**
 * Comprueba si hay servicios comunmente peligrosos escuchando en este equipo:
 * FTP (21), Telnet (23), RDP (3389) y VNC (5900).
 *
 * Importante: esto detecta servicios escuchando en ESTE equipo via localhost,
 * no si tu router los expone a internet (eso depende de si tienes redireccion
 * de puertos activada, algo que esta app no puede comprobar desde dentro de la
 * red). El texto del resultado lo deja explicito para no sugerir una garantia
 * que esta comprobacion no puede dar.
 */
@Component
public class OpenPortsChecker {

    private static final Map<Integer, String> RISKY_PORTS = new LinkedHashMap<>();
    static {
        RISKY_PORTS.put(21, "FTP");
        RISKY_PORTS.put(23, "Telnet");
        RISKY_PORTS.put(3389, "RDP (Escritorio remoto)");
        RISKY_PORTS.put(5900, "VNC");
    }

    private final int connectTimeoutMs;

    public OpenPortsChecker(@Value("${netknife.exposure.port-check-timeout-ms:300}") int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public CheckResult check() {
        return evaluate(this::isListening);
    }

    /**
     * Logica de evaluacion pura, testeable sin abrir sockets reales.
     */
    static CheckResult evaluate(IntPredicate isListening) {
        List<String> openPorts = RISKY_PORTS.entrySet().stream()
                .filter(entry -> isListening.test(entry.getKey()))
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .collect(Collectors.toList());

        if (openPorts.isEmpty()) {
            return CheckResult.ok("No se ha detectado ningun servicio de riesgo (FTP, Telnet, RDP, VNC) "
                    + "escuchando en este equipo. Esto no confirma si tu router expone puertos a internet.");
        }

        return new CheckResult(
                CheckStatus.PELIGRO,
                "Este equipo tiene servicios potencialmente peligrosos escuchando: "
                        + String.join(", ", openPorts) + ".",
                "Puertos detectados en este equipo (no confirma si estan expuestos a internet): "
                        + String.join(", ", openPorts),
                "Desactiva estos servicios si no los necesitas. Si necesitas acceso remoto, usa una VPN "
                        + "en vez de redirigir estos puertos directamente desde tu router hacia internet.");
    }

    private boolean isListening(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), connectTimeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
