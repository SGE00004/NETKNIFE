package com.netknife.common.findings;

import com.netknife.common.dto.CheckStatus;

import java.time.Instant;

/**
 * Un hallazgo individual, tal como lo expone un modulo del equipo rojo para que
 * el Informe Automatico de Hallazgos lo agregue. No lleva RiskLevel: ese calculo
 * (fromCheckStatus) es responsabilidad de quien consume el hallazgo, no de quien
 * lo produce.
 */
public record Finding(
        String sourceModuleId,
        String sourceModuleLabel,
        String id,
        String title,
        CheckStatus status,
        String summary,
        String detail,
        String howToFix,
        Instant detectedAt
) {
}
