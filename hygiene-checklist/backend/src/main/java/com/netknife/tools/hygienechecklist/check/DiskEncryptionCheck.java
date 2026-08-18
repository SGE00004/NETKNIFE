package com.netknife.tools.hygienechecklist.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import com.netknife.common.util.SystemCommandRunner;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Comprueba si el cifrado de disco esta activo.
 * - macOS: "fdesetup status" funciona sin sudo y es fiable.
 * - Windows: "manage-bde -status" normalmente requiere permisos de administrador
 *   para dar una lectura fiable; si falla, se declara no verificable en vez de
 *   arriesgarse a un falso resultado.
 * - Linux: detectar LUKS sin privilegios es poco fiable (un heuristico por
 *   lsblk no confirma que la particion de arranque este cifrada), asi que se
 *   declara no verificable con una guia manual.
 */
@Component
public class DiskEncryptionCheck implements HygieneCheck {

    private final SystemCommandRunner commandRunner;

    public DiskEncryptionCheck(SystemCommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    @Override
    public String id() {
        return "disk-encryption";
    }

    @Override
    public String title() {
        return "Cifrado de disco activo";
    }

    @Override
    public String whyItMatters() {
        return "Si pierdes o te roban el equipo, el cifrado de disco impide leer tus archivos sin tu contrasena.";
    }

    @Override
    public boolean isAutomatic() {
        return true;
    }

    @Override
    public CheckResult evaluate() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("mac")) {
            return checkMac();
        }
        if (os.contains("win")) {
            return checkWindows();
        }
        return notVerifiable("Comprobar el cifrado de disco (LUKS) de forma fiable sin permisos de "
                + "administrador no es posible en Linux desde esta app.",
                "Ejecuta 'lsblk -f' en una terminal y busca 'crypto_LUKS' junto a tu particion principal.");
    }

    private CheckResult checkMac() {
        Optional<String> output = commandRunner.run("fdesetup", "status");
        if (output.isEmpty()) {
            return notVerifiable("No se ha podido consultar el estado de FileVault.", null);
        }
        return classifyMac(output.get());
    }

    private CheckResult checkWindows() {
        Optional<String> output = commandRunner.run("manage-bde", "-status");
        if (output.isEmpty()) {
            return notVerifiable("No se ha podido consultar el estado de BitLocker (normalmente requiere "
                    + "permisos de administrador).", "Ejecuta 'manage-bde -status' como administrador, o revisa "
                    + "en Panel de control > Cifrado de unidad BitLocker.");
        }
        return classifyWindows(output.get());
    }

    /** Logica pura, testeable sin ejecutar comandos reales. */
    static CheckResult classifyMac(String output) {
        String normalized = output.toLowerCase(Locale.ROOT);
        if (normalized.contains("filevault is on")) {
            return CheckResult.ok("FileVault esta activo: tu disco esta cifrado.");
        }
        if (normalized.contains("filevault is off")) {
            return new CheckResult(CheckStatus.PELIGRO,
                    "FileVault esta desactivado: tu disco no esta cifrado.",
                    null,
                    "Activalo en Ajustes del Sistema > Privacidad y seguridad > FileVault.");
        }
        return notVerifiable("No se ha podido interpretar el estado de FileVault.", null);
    }

    static CheckResult classifyWindows(String output) {
        String normalized = output.toLowerCase(Locale.ROOT);
        if (normalized.contains("protection on") || normalized.contains("protección activada")) {
            return CheckResult.ok("BitLocker esta activo: tu disco esta cifrado.");
        }
        if (normalized.contains("protection off") || normalized.contains("protección desactivada")) {
            return new CheckResult(CheckStatus.PELIGRO,
                    "BitLocker esta desactivado: tu disco no esta cifrado.",
                    null,
                    "Activalo desde Panel de control > Cifrado de unidad BitLocker.");
        }
        return notVerifiable("No se ha podido interpretar el estado de BitLocker.", null);
    }

    private static CheckResult notVerifiable(String summary, String howToFix) {
        return new CheckResult(CheckStatus.NO_VERIFICABLE, summary, null, howToFix);
    }
}
