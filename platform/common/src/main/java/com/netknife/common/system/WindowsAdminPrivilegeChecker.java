package com.netknife.common.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Comprueba privilegios de administrador en Windows ejecutando "net session": sin
 * privilegios elevados este comando falla con "Acceso denegado" y codigo de salida
 * distinto de cero; con privilegios elevados devuelve 0 (aunque no haya ninguna
 * sesion de red activa). Es el truco estandar para esta comprobacion sin depender
 * de JNI/JNA.
 */
@Component
public class WindowsAdminPrivilegeChecker implements AdminPrivilegeChecker {

    private static final Logger log = LoggerFactory.getLogger(WindowsAdminPrivilegeChecker.class);

    @Override
    public boolean hasAdministratorPrivileges() {
        try {
            Process process = new ProcessBuilder("net", "session")
                    .redirectErrorStream(true)
                    .start();
            process.getInputStream().readAllBytes();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (IOException e) {
            log.debug("No se pudo comprobar los privilegios de administrador: {}", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
