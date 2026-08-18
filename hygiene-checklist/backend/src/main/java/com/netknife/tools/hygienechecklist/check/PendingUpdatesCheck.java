package com.netknife.tools.hygienechecklist.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import org.springframework.stereotype.Component;

/**
 * No existe una forma fiable y multiplataforma de consultar actualizaciones
 * criticas pendientes sin permisos elevados o modulos adicionales, asi que
 * este item es manual desde el principio.
 */
@Component
public class PendingUpdatesCheck implements HygieneCheck {

    @Override
    public String id() {
        return "pending-updates";
    }

    @Override
    public String title() {
        return "Actualizaciones de seguridad al dia";
    }

    @Override
    public String whyItMatters() {
        return "Las actualizaciones corrigen fallos de seguridad ya conocidos; retrasarlas deja puertas "
                + "abiertas que los atacantes ya saben explotar.";
    }

    @Override
    public boolean isAutomatic() {
        return false;
    }

    @Override
    public CheckResult evaluate() {
        return new CheckResult(CheckStatus.NO_VERIFICABLE,
                "No se puede comprobar automaticamente si tienes actualizaciones pendientes.",
                null,
                "Revisa manualmente los ajustes de actualizaciones de tu sistema operativo y confirma "
                        + "aqui si estas al dia.");
    }
}
