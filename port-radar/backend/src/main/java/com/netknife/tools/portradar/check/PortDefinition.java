package com.netknife.tools.portradar.check;

import com.netknife.common.dto.CheckStatus;

/**
 * Un puerto de la lista curada: numero, protocolo asociado, explicacion en
 * lenguaje llano de por que importa, y el nivel de riesgo que se asigna si el
 * puerto aparece abierto (independientemente de si se consigue banner o no).
 */
public record PortDefinition(
        int port,
        String protocolLabel,
        String plainLanguageRisk,
        CheckStatus riskIfOpen
) {
}
