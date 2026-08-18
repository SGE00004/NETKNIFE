package com.netknife.tools.portradar.check;

import com.netknife.common.dto.CheckStatus;

/**
 * Resultado de sondear un puerto concreto contra un host: solo se generan
 * instancias para puertos que respondieron a la conexion (abiertos). El banner
 * es null si el puerto abrio pero no se recibio ninguna respuesta legible en
 * el tiempo de espera.
 */
public record PortScanOutcome(
        int port,
        String protocolLabel,
        String banner,
        CheckStatus status,
        String summary,
        String howToFix
) {
}
