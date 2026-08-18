package com.netknife.common.dto;

import java.util.Collection;
import java.util.List;

/**
 * Resultado de una comprobacion individual de seguridad, usado por cualquier
 * herramienta que evalue el sistema (Comprobador de Exposicion, Checklist de
 * Higiene...). Se separa de RiskLevel porque aqui existe un cuarto estado
 * legitimo: NO_VERIFICABLE nunca debe presentarse como si fuera OK.
 */
public enum CheckStatus {
    OK,
    ATENCION,
    PELIGRO,
    NO_VERIFICABLE;

    /**
     * Orden de gravedad de peor a mejor. NO_VERIFICABLE se considera mas grave
     * que OK deliberadamente: no saber si algo esta bien nunca debe presentarse
     * como un "todo en orden".
     */
    private static final List<CheckStatus> SEVERITY_ORDER = List.of(PELIGRO, ATENCION, NO_VERIFICABLE, OK);

    public static CheckStatus worstOf(CheckStatus... statuses) {
        for (CheckStatus candidate : SEVERITY_ORDER) {
            for (CheckStatus status : statuses) {
                if (status == candidate) {
                    return candidate;
                }
            }
        }
        return OK;
    }

    public static CheckStatus worstOf(Collection<CheckStatus> statuses) {
        return worstOf(statuses.toArray(new CheckStatus[0]));
    }
}
