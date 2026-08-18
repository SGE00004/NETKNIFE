package com.netknife.tools.hygienechecklist.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import com.netknife.common.util.SystemCommandRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comprueba si el firewall del sistema esta activo.
 * - Windows: "netsh advfirewall show allprofiles state" (sin elevacion).
 * - macOS: "socketfilterfw --getglobalstate" (sin sudo).
 * - Linux: la mayoria de front-ends (ufw, firewalld) exigen root incluso para
 *   consultar el estado, asi que se declara no verificable en vez de arriesgarse
 *   a un resultado incorrecto.
 */
@Component
public class FirewallCheck implements HygieneCheck {

    private static final Pattern WINDOWS_STATE_LINE =
            Pattern.compile("(?i)^\\s*(State|Estado)\\s+(\\S+)\\s*$", Pattern.MULTILINE);

    private final SystemCommandRunner commandRunner;

    public FirewallCheck(SystemCommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    @Override
    public String id() {
        return "firewall";
    }

    @Override
    public String title() {
        return "Firewall del sistema activo";
    }

    @Override
    public String whyItMatters() {
        return "El firewall bloquea conexiones entrantes no autorizadas hacia tu equipo.";
    }

    @Override
    public boolean isAutomatic() {
        return true;
    }

    @Override
    public CheckResult evaluate() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return checkWindows();
        }
        if (os.contains("mac")) {
            return checkMac();
        }
        return notVerifiable("En Linux, comprobar el firewall normalmente requiere permisos de administrador, "
                + "asi que no se puede hacer de forma automatica y fiable desde esta app.");
    }

    private CheckResult checkWindows() {
        Optional<String> output = commandRunner.run("netsh", "advfirewall", "show", "allprofiles", "state");
        if (output.isEmpty()) {
            return notVerifiable("No se ha podido consultar el estado del firewall de Windows.");
        }
        List<String> states = new ArrayList<>();
        Matcher matcher = WINDOWS_STATE_LINE.matcher(output.get());
        while (matcher.find()) {
            states.add(matcher.group(2));
        }
        return classifyWindows(states);
    }

    private CheckResult checkMac() {
        Optional<String> output = commandRunner.run(
                "/usr/libexec/ApplicationFirewall/socketfilterfw", "--getglobalstate");
        if (output.isEmpty()) {
            return notVerifiable("No se ha podido consultar el estado del firewall de macOS.");
        }
        return classifyMac(output.get());
    }

    /** Logica pura, testeable sin ejecutar comandos reales. */
    static CheckResult classifyWindows(List<String> profileStates) {
        if (profileStates.isEmpty()) {
            return notVerifiable("No se ha podido interpretar el estado del firewall de Windows.");
        }
        long off = profileStates.stream().filter(state -> !isOnValue(state)).count();
        if (off == 0) {
            return CheckResult.ok("El firewall de Windows esta activo en todos los perfiles de red.");
        }
        return new CheckResult(CheckStatus.PELIGRO,
                "El firewall de Windows esta desactivado en al menos un perfil de red.",
                "Perfiles con firewall desactivado: " + off + " de " + profileStates.size(),
                "Activa el firewall en Ajustes > Privacidad y seguridad > Firewall de Windows Defender "
                        + "para todos los perfiles de red.");
    }

    static CheckResult classifyMac(String output) {
        String normalized = output.toLowerCase(Locale.ROOT);
        if (normalized.contains("enabled")) {
            return CheckResult.ok("El firewall de macOS esta activo.");
        }
        if (normalized.contains("disabled")) {
            return new CheckResult(CheckStatus.PELIGRO,
                    "El firewall de macOS esta desactivado.",
                    null,
                    "Activalo en Ajustes del Sistema > Red > Firewall.");
        }
        return notVerifiable("No se ha podido interpretar el estado del firewall de macOS.");
    }

    private static boolean isOnValue(String value) {
        String normalized = value.toUpperCase(Locale.ROOT);
        return normalized.equals("ON") || normalized.startsWith("ACTIV");
    }

    private static CheckResult notVerifiable(String summary) {
        return new CheckResult(CheckStatus.NO_VERIFICABLE, summary, null,
                "Revisa manualmente los ajustes de firewall/seguridad de tu sistema operativo.");
    }
}
