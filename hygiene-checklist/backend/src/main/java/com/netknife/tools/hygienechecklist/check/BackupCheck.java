package com.netknife.tools.hygienechecklist.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import org.springframework.stereotype.Component;

/**
 * La existencia de una copia de seguridad reciente no es detectable de forma
 * fiable desde el sistema operativo (podria estar en un disco externo, en la
 * nube, desconectada...), asi que es una pregunta manual desde el principio.
 */
@Component
public class BackupCheck implements HygieneCheck {

    @Override
    public String id() {
        return "backup";
    }

    @Override
    public String title() {
        return "Copia de seguridad reciente";
    }

    @Override
    public String whyItMatters() {
        return "Una copia de seguridad de menos de 30 dias es tu red de seguridad frente a un ransomware, "
                + "un robo o un fallo de disco.";
    }

    @Override
    public boolean isAutomatic() {
        return false;
    }

    @Override
    public CheckResult evaluate() {
        return new CheckResult(CheckStatus.NO_VERIFICABLE,
                "Esto no se puede comprobar automaticamente desde el sistema.",
                null,
                "Responde manualmente: ¿tienes una copia de seguridad de tus archivos importantes de los "
                        + "ultimos 30 dias?");
    }
}
