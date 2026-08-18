package com.netknife.common.net;

import com.netknife.common.util.SystemCommandRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resuelve la direccion IP del gateway (router) de la red local activa. Vive en
 * platform/common (en vez de en network-scanner, su modulo de origen, donde lo
 * usa el bloqueo por ARP spoofing) porque wifi-router-audit tambien lo necesita
 * para saber a que IP dirigir la auditoria del router: ningun modulo de
 * herramienta depende de otro modulo de herramienta, asi que la logica
 * compartida sube a common.
 *
 * Se evita deliberadamente cualquier libreria nativa: solo se leen ficheros/
 * comandos ya disponibles en el sistema operativo. El parseo de cada formato
 * de salida esta separado en un metodo que recibe el texto ya leido (en vez de
 * ejecutar el comando el mismo), para poder testearlo con fixtures fijos sin
 * depender del sistema operativo real.
 */
@Component
public class GatewayResolver {

    private static final Logger log = LoggerFactory.getLogger(GatewayResolver.class);

    private static final Path PROC_NET_ROUTE = Path.of("/proc/net/route");

    /**
     * Comando de PowerShell que imprime unicamente la IP del gateway de la ruta por
     * defecto (la de menor metrica), sin nada mas. Se usa PowerShell con nombres de
     * propiedad de .NET (NextHop, RouteMetric) en vez de parsear "ipconfig": la
     * salida de "ipconfig" se localiza segun el idioma del sistema operativo (p.ej.
     * "Puerta de enlace predeterminada" en Windows en español, no "Default Gateway"),
     * mientras que los nombres de propiedad de un cmdlet no cambian con el idioma.
     */
    private static final String[] WINDOWS_GATEWAY_COMMAND = {
            "powershell", "-NoProfile", "-NonInteractive", "-Command",
            "(Get-NetRoute -DestinationPrefix '0.0.0.0/0' -ErrorAction SilentlyContinue | "
                    + "Sort-Object -Property RouteMetric | Select-Object -First 1 -ExpandProperty NextHop)"
    };

    private static final Pattern IPV4_PATTERN = Pattern.compile("(\\d{1,3}(?:\\.\\d{1,3}){3})");

    /** "gateway: 192.168.1.1" (salida de "route -n get default" en macOS/BSD, no localizada). */
    private static final Pattern MACOS_GATEWAY_LINE =
            Pattern.compile("gateway:\\s*(\\d{1,3}(?:\\.\\d{1,3}){3})");

    private final SystemCommandRunner commandRunner;

    public GatewayResolver(SystemCommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public Optional<String> resolveGatewayIp() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (Files.exists(PROC_NET_ROUTE)) {
            return resolveFromProcNetRoute();
        }
        if (os.contains("win")) {
            return commandRunner.run(WINDOWS_GATEWAY_COMMAND)
                    .flatMap(GatewayResolver::parseSingleIpOutput);
        }
        return commandRunner.run("route", "-n", "get", "default")
                .flatMap(GatewayResolver::parseMacosRoute);
    }

    private Optional<String> resolveFromProcNetRoute() {
        try {
            for (String line : Files.readAllLines(PROC_NET_ROUTE, StandardCharsets.UTF_8)) {
                Optional<String> gateway = parseProcNetRouteLine(line);
                if (gateway.isPresent()) {
                    return gateway;
                }
            }
        } catch (IOException e) {
            log.debug("No se pudo leer /proc/net/route: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Cada linea de /proc/net/route tiene, separadas por tabuladores: Iface, Destination,
     * Gateway, Flags... La ruta por defecto tiene Destination "00000000" y el flag RTF_GATEWAY
     * (0x2) activo en el campo Flags. Destination y Gateway estan en hexadecimal little-endian.
     */
    static Optional<String> parseProcNetRouteLine(String line) {
        String[] columns = line.trim().split("\\s+");
        if (columns.length < 4 || !"00000000".equals(columns[1])) {
            return Optional.empty();
        }
        try {
            int flags = Integer.parseInt(columns[3], 16);
            if ((flags & 0x2) == 0) {
                return Optional.empty();
            }
            return Optional.of(hexLittleEndianToIp(columns[2]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static String hexLittleEndianToIp(String hex) {
        long value = Long.parseLong(hex, 16);
        return (value & 0xFF) + "." + ((value >> 8) & 0xFF) + "." + ((value >> 16) & 0xFF) + "." + ((value >> 24) & 0xFF);
    }

    /**
     * La salida esperada del comando de PowerShell es unicamente la IP (y quizas
     * espacios en blanco alrededor); se valida con una expresion regular en vez de
     * confiar ciegamente en que no haya nada mas por si PowerShell añade avisos.
     */
    static Optional<String> parseSingleIpOutput(String output) {
        Matcher matcher = IPV4_PATTERN.matcher(output);
        if (matcher.find()) {
            String candidate = matcher.group(1);
            return candidate.equals("0.0.0.0") ? Optional.empty() : Optional.of(candidate);
        }
        return Optional.empty();
    }

    static Optional<String> parseMacosRoute(String output) {
        Matcher matcher = MACOS_GATEWAY_LINE.matcher(output);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }
}
