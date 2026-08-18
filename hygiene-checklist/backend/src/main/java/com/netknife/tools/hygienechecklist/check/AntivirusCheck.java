package com.netknife.tools.hygienechecklist.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import com.netknife.common.util.SystemCommandRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Comprueba si hay un antivirus activo registrado en Windows Security Center.
 * Solo confirma presencia (no si esta 100% actualizado: decodificar el bitmask
 * de productState es fragil y especifico de cada fabricante, asi que no se
 * arriesga a una lectura incorrecta). Mac/Linux no tienen un Centro de
 * seguridad estandar equivalente, asi que se declaran no verificables.
 */
@Component
public class AntivirusCheck implements HygieneCheck {

    private final SystemCommandRunner commandRunner;

    public AntivirusCheck(SystemCommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    @Override
    public String id() {
        return "antivirus";
    }

    @Override
    public String title() {
        return "Antivirus activo";
    }

    @Override
    public String whyItMatters() {
        return "Un antivirus activo detecta y bloquea malware conocido antes de que pueda hacer dano.";
    }

    @Override
    public boolean isAutomatic() {
        return true;
    }

    @Override
    public CheckResult evaluate() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("win")) {
            return new CheckResult(CheckStatus.NO_VERIFICABLE,
                    "No existe un Centro de seguridad estandar en este sistema operativo desde el que "
                            + "comprobar esto automaticamente.",
                    null,
                    "Revisa manualmente que tengas un antivirus instalado, activo y actualizado.");
        }
        Optional<String> output = commandRunner.run("powershell", "-NoProfile", "-Command",
                "Get-CimInstance -Namespace root/SecurityCenter2 -ClassName AntiVirusProduct "
                        + "| Select-Object -ExpandProperty displayName");
        if (output.isEmpty()) {
            return new CheckResult(CheckStatus.NO_VERIFICABLE,
                    "No se ha podido consultar el Centro de seguridad de Windows.",
                    null,
                    "Revisa manualmente en Seguridad de Windows que el antivirus este activo.");
        }
        List<String> productNames = output.get().lines().map(String::trim).filter(line -> !line.isBlank()).toList();
        return classify(productNames);
    }

    /** Logica pura, testeable sin consultar el sistema real. */
    static CheckResult classify(List<String> productNames) {
        if (productNames.isEmpty()) {
            return new CheckResult(CheckStatus.PELIGRO,
                    "No se ha detectado ningun antivirus activo en Windows Security Center.",
                    null,
                    "Activa Microsoft Defender (incluido en Windows) en Seguridad de Windows > Proteccion "
                            + "antivirus y contra amenazas, o instala un antivirus de terceros.");
        }
        return new CheckResult(CheckStatus.OK,
                "Se ha detectado al menos un antivirus activo: " + String.join(", ", productNames)
                        + ". Esto no confirma que este totalmente actualizado.",
                null,
                null);
    }
}
