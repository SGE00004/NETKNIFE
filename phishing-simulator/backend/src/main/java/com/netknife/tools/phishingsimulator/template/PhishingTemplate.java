package com.netknife.tools.phishingsimulator.template;

import java.util.List;

/**
 * Contenido fijo de una plantilla de simulacion. bodyHtml contiene el
 * marcador {{TRACKING_URL}}, que el servicio de envio sustituye por el enlace
 * de seguimiento real de cada simulacion antes de mandar el correo. El enlace
 * apunta siempre de vuelta al propio backend de NETKNIFE (nunca a un dominio
 * externo), y el correo indica en todo momento que es una simulacion educativa.
 */
public record PhishingTemplate(
        String id,
        String name,
        String subject,
        String senderLabel,
        String bodyHtml,
        List<String> signals,
        String lesson
) {
}
