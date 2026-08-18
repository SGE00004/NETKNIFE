package com.netknife.tools.trafficanalyzer.process;

import com.netknife.common.util.SystemCommandRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Ejecuta "netstat -ano -b" (Windows, requiere admin para resolver nombres de
 * proceso) y empareja una conexion capturada por pcap4j con el proceso que la
 * origino. Se ejecuta en cada consulta sin cache adicional: el propio polling del
 * frontend (2-3s) ya limita la frecuencia, y netstat es rapido.
 */
@Component
public class NetstatProcessConnectionMapper {

    private final SystemCommandRunner commandRunner;

    public NetstatProcessConnectionMapper(SystemCommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public List<NetstatConnection> currentSnapshot() {
        return commandRunner.run("netstat", "-ano", "-b").map(NetstatOutputParser::parse).orElse(List.of());
    }

    /**
     * Primero intenta un match exacto (protocolo, puerto local, IP y puerto remotos);
     * si no lo encuentra, cae a solo (protocolo, puerto local) — netstat y pcap4j
     * pueden capturar la conexion en instantes ligeramente distintos, y el puerto
     * local es la clave mas estable de las dos.
     */
    public Optional<NetstatConnection> findOwner(
            String protocol, int localPort, String remoteIp, Integer remotePort, List<NetstatConnection> snapshot) {
        Optional<NetstatConnection> exact = snapshot.stream()
                .filter(c -> c.protocol().equalsIgnoreCase(protocol)
                        && c.localPort() == localPort
                        && c.remoteIp().equals(remoteIp)
                        && Objects.equals(c.remotePort(), remotePort))
                .findFirst();
        if (exact.isPresent()) {
            return exact;
        }
        return snapshot.stream()
                .filter(c -> c.protocol().equalsIgnoreCase(protocol) && c.localPort() == localPort)
                .findFirst();
    }
}
