package com.netknife.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Ejecuta un comando del sistema operativo y captura su salida, con timeout.
 * Usado por las comprobaciones de seguridad/higiene que necesitan preguntar al
 * SO (netsh, nmcli, fdesetup...) sin requerir permisos elevados. Nunca lanza:
 * cualquier fallo (comando no encontrado, timeout, sin permisos) se traduce en
 * Optional.empty(), que cada comprobacion debe interpretar como "no verificable",
 * nunca como un resultado positivo.
 */
@Component
public class SystemCommandRunner {

    private static final Logger log = LoggerFactory.getLogger(SystemCommandRunner.class);

    private final int timeoutSeconds;

    public SystemCommandRunner(@Value("${netknife.system-command-timeout-seconds:5}") int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Optional<String> run(String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            String output = readAll(process);
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.debug("El comando {} no termino a tiempo", String.join(" ", command));
                return Optional.empty();
            }
            return Optional.of(output);
        } catch (IOException e) {
            log.debug("No se pudo ejecutar {}: {}", String.join(" ", command), e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private String readAll(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }
        return output.toString();
    }
}
