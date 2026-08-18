package com.netknife.tools.trafficanalyzer.process;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsea la salida de "netstat -ano -b" (Windows). El formato es dos lineas por
 * conexion: la primera con protocolo/direcciones/estado/PID, la segunda (opcional,
 * indentada, entre corchetes) con el nombre del ejecutable dueño de esa conexion
 * -b requiere privilegios de administrador para resolver ese nombre; sin admin,
 * esa segunda linea simplemente no aparece, y aqui se interpreta como processName
 * null sin consumir la siguiente linea (que sera la conexion siguiente).
 *
 * Logica pura, sin ejecutar el comando (lo hace {@link NetstatProcessConnectionMapper}
 * via SystemCommandRunner): permite testear con fixtures de texto fijo, mismo patron
 * que GatewayResolver.parseSingleIpOutput() en platform/common.
 */
public final class NetstatOutputParser {

    private static final Pattern PROCESS_LINE = Pattern.compile("^\\s*\\[(.+)]\\s*$");

    private NetstatOutputParser() {
    }

    public static List<NetstatConnection> parse(String rawOutput) {
        List<String> lines = rawOutput.lines().toList();
        List<NetstatConnection> result = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String[] tokens = lines.get(i).trim().split("\\s+");
            if (tokens.length < 4 || !("TCP".equalsIgnoreCase(tokens[0]) || "UDP".equalsIgnoreCase(tokens[0]))) {
                continue;
            }
            NetstatConnection parsed = parseConnectionTokens(tokens);
            if (parsed == null) {
                continue;
            }

            String processName = null;
            if (i + 1 < lines.size()) {
                Matcher processMatcher = PROCESS_LINE.matcher(lines.get(i + 1));
                if (processMatcher.matches()) {
                    processName = processMatcher.group(1).trim();
                    i++;
                }
            }
            result.add(withProcessName(parsed, processName));
        }
        return result;
    }

    /**
     * TCP tiene 5 columnas (Proto, local, remoto, Estado, PID); UDP tiene 4 (sin
     * Estado, el remoto siempre es *:*). Se distingue por numero de tokens en vez de
     * una unica regex compleja, para tolerar mejor las diferencias de idioma en el
     * texto del estado y el ancho variable de las columnas.
     */
    private static NetstatConnection parseConnectionTokens(String[] tokens) {
        boolean isTcp = "TCP".equalsIgnoreCase(tokens[0]);
        try {
            if (isTcp && tokens.length >= 5) {
                HostPort local = splitHostPort(tokens[1]);
                HostPort remote = splitHostPort(tokens[2]);
                String state = tokens[3];
                long pid = Long.parseLong(tokens[4]);
                return new NetstatConnection("TCP", local.host(), local.portAsInt(),
                        remote.host(), remote.portAsIntOrNull(), state, pid, null);
            }
            if (!isTcp && tokens.length >= 4) {
                HostPort local = splitHostPort(tokens[1]);
                HostPort remote = splitHostPort(tokens[2]);
                long pid = Long.parseLong(tokens[3]);
                return new NetstatConnection("UDP", local.host(), local.portAsInt(),
                        remote.host(), remote.portAsIntOrNull(), null, pid, null);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private static NetstatConnection withProcessName(NetstatConnection connection, String processName) {
        return new NetstatConnection(connection.protocol(), connection.localIp(), connection.localPort(),
                connection.remoteIp(), connection.remotePort(), connection.state(), connection.pid(), processName);
    }

    private record HostPort(String host, String port) {
        int portAsInt() {
            return Integer.parseInt(port);
        }

        Integer portAsIntOrNull() {
            return "*".equals(port) ? null : Integer.parseInt(port);
        }
    }

    /**
     * Separa "host:puerto". Las direcciones IPv6 van entre corchetes
     * ("[::]:135", "[fe80::1%12]:54321") porque contienen ":" en el propio host;
     * IPv4 y nombres de host no lo hacen, asi que basta con partir por el ultimo ":".
     */
    private static HostPort splitHostPort(String token) {
        if (token.startsWith("[")) {
            int closeBracket = token.indexOf(']');
            String host = token.substring(1, closeBracket);
            String port = token.substring(closeBracket + 2);
            return new HostPort(host, port);
        }
        int lastColon = token.lastIndexOf(':');
        return new HostPort(token.substring(0, lastColon), token.substring(lastColon + 1));
    }
}
