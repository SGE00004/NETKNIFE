package com.netknife.common.wifi;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import com.netknife.common.util.SystemCommandRunner;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comprueba el tipo de cifrado de la red WiFi a la que esta conectado el equipo.
 *
 * No existe una API multiplataforma sin privilegios elevados para esto, asi que
 * cada sistema operativo se trata por separado:
 * - Windows: "netsh wlan show interfaces" funciona sin permisos de administrador
 *   y expone la autenticacion en la linea "Autenticacion"/"Authentication".
 * - Linux: "nmcli" (NetworkManager) expone la seguridad sin privilegios si esta
 *   instalado; en distribuciones sin NetworkManager no hay alternativa fiable.
 * - macOS: no existe un comando moderno sin sudo (el antiguo "airport" esta
 *   deprecado/retirado en versiones recientes), asi que siempre se declara
 *   no verificable con una guia manual.
 *
 * Vive en platform/common (en vez de en exposure-checker, su modulo de origen)
 * porque tambien lo usa wifi-router-audit: ningun modulo de herramienta depende
 * de otro modulo de herramienta, asi que la logica compartida sube a common.
 */
@Component
public class WifiEncryptionChecker {

    private static final Pattern WINDOWS_STATE_PATTERN =
            Pattern.compile("(?i)^\\s*(Estado|State)\\s*:\\s*(.+)$", Pattern.MULTILINE);
    private static final Pattern WINDOWS_AUTH_PATTERN =
            Pattern.compile("(?i)^\\s*(Autenticaci[oó]n|Authentication)\\s*:\\s*(.+)$", Pattern.MULTILINE);

    private static final String MANUAL_GUIDE =
            "Entra en los ajustes de tu router (normalmente en 192.168.1.1 o 192.168.0.1 desde un "
                    + "navegador) y busca el apartado de seguridad WiFi: deberia decir WPA2 o WPA3. "
                    + "Si dice WEP o 'sin seguridad', cambialo cuanto antes.";

    private final SystemCommandRunner commandRunner;

    public WifiEncryptionChecker(SystemCommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public CheckResult check() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return checkWindows();
        }
        if (os.contains("nux")) {
            return checkLinux();
        }
        // macOS y cualquier SO no cubierto: no hay comando fiable sin privilegios elevados.
        return notVerifiable("No es posible comprobar el cifrado WiFi de forma automatica en este sistema "
                + "operativo sin permisos de administrador.");
    }

    private CheckResult checkWindows() {
        Optional<String> output = commandRunner.run("netsh", "wlan", "show", "interfaces");
        if (output.isEmpty()) {
            return notVerifiable("No se ha podido consultar el estado de la conexion WiFi.");
        }
        Matcher stateMatcher = WINDOWS_STATE_PATTERN.matcher(output.get());
        boolean connected = stateMatcher.find()
                && stateMatcher.group(2).toLowerCase(Locale.ROOT).contains("conect");
        Matcher authMatcher = WINDOWS_AUTH_PATTERN.matcher(output.get());
        String authValue = authMatcher.find() ? authMatcher.group(2).trim() : null;
        return classify(connected, authValue);
    }

    private CheckResult checkLinux() {
        Optional<String> output = commandRunner.run("nmcli", "-t", "-f", "active,security", "dev", "wifi");
        if (output.isEmpty()) {
            return notVerifiable("No se ha podido consultar el estado de la conexion WiFi (se necesita "
                    + "NetworkManager con 'nmcli' instalado).");
        }
        for (String line : output.get().split("\\R")) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2 && parts[0].equalsIgnoreCase("yes")) {
                return classify(true, parts[1].trim());
            }
        }
        return classify(false, null);
    }

    /**
     * Logica de clasificacion pura, sin dependencias del sistema operativo:
     * a partir de si hay una red WiFi activa y el texto de autenticacion/seguridad
     * que reporta el SO, decide el nivel de riesgo. Package-private para poder
     * testearla directamente sin invocar comandos reales.
     */
    static CheckResult classify(boolean connected, String authValue) {
        if (!connected) {
            return notVerifiable("No se ha detectado una conexion WiFi activa; conectate por WiFi para "
                    + "poder comprobar el cifrado, o revisalo manualmente en los ajustes de tu router.");
        }
        if (authValue == null || authValue.isBlank()) {
            return notVerifiable("No se ha podido determinar el tipo de cifrado de tu red WiFi.");
        }
        String normalized = authValue.toUpperCase(Locale.ROOT);
        if (normalized.contains("WPA3")) {
            return CheckResult.ok("Tu red WiFi usa WPA3, el cifrado mas seguro disponible actualmente.");
        }
        if (normalized.contains("WPA2")) {
            return CheckResult.ok("Tu red WiFi usa WPA2, un cifrado adecuado para uso domestico.");
        }
        if (normalized.contains("WEP")) {
            return new CheckResult(CheckStatus.PELIGRO,
                    "Tu red WiFi usa WEP, un cifrado obsoleto que se puede romper en minutos.",
                    "Cifrado detectado: " + authValue,
                    "Entra en los ajustes de tu router y cambia el tipo de seguridad WiFi a WPA2 o WPA3. "
                            + "Casi todos los routers de los ultimos 10 anos lo soportan.");
        }
        if (normalized.contains("NONE") || normalized.contains("OPEN") || normalized.contains("ABIERT")) {
            return new CheckResult(CheckStatus.PELIGRO,
                    "Tu red WiFi no tiene ningun cifrado: cualquiera cerca puede conectarse y ver tu trafico.",
                    "Autenticacion detectada: " + authValue,
                    "Entra en los ajustes de tu router y activa WPA2 o WPA3 con una contrasena fuerte.");
        }
        if (normalized.contains("WPA")) {
            return new CheckResult(CheckStatus.ATENCION,
                    "Tu red WiFi usa WPA (sin el 2), un cifrado mas debil que los estandares actuales.",
                    "Cifrado detectado: " + authValue,
                    "Si tu router lo permite, actualiza la seguridad WiFi a WPA2 o WPA3 en sus ajustes.");
        }
        return notVerifiable("No se ha reconocido el tipo de cifrado reportado por el sistema ("
                + authValue + ").");
    }

    private static CheckResult notVerifiable(String summary) {
        return new CheckResult(CheckStatus.NO_VERIFICABLE, summary, null, MANUAL_GUIDE);
    }
}
