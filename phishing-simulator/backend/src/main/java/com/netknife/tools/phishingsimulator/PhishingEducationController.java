package com.netknife.tools.phishingsimulator;

import com.netknife.tools.phishingsimulator.template.PhishingTemplate;
import com.netknife.tools.phishingsimulator.template.PhishingTemplateCatalog;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pagina educativa a la que se redirige tras un clic en una simulacion. Vive
 * fuera de /api y se sirve como HTML plano generado por el propio backend (sin
 * depender del frontend Vite/Tauri): quien la abre es el destinatario de la
 * simulacion, que puede estar en cualquier dispositivo de la red, no
 * necesariamente uno con NETKNIFE instalado.
 */
@RestController
public class PhishingEducationController {

    private final PhishingTemplateCatalog templateCatalog;

    public PhishingEducationController(PhishingTemplateCatalog templateCatalog) {
        this.templateCatalog = templateCatalog;
    }

    @GetMapping(value = "/phishing/learn/{templateId}", produces = MediaType.TEXT_HTML_VALUE)
    public String learn(@PathVariable String templateId) {
        PhishingTemplate template = templateCatalog.findById(templateId).orElse(null);
        return renderPage(template);
    }

    private String renderPage(PhishingTemplate template) {
        String signalsHtml = template == null
                ? ""
                : template.signals().stream()
                        .map(signal -> "<li>" + escape(signal) + "</li>")
                        .reduce("", String::concat);
        String lesson = template == null
                ? "Este correo formaba parte de una simulacion educativa de NETKNIFE para practicar como "
                        + "reconocer intentos de phishing."
                : escape(template.lesson());

        return """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>Simulacion educativa - NETKNIFE</title>
                  <style>
                    body { font-family: system-ui, sans-serif; background: #08080b; color: #e7e7e2; margin: 0; padding: 24px; }
                    .card { max-width: 560px; margin: 40px auto; border: 1px solid #2a2a33; padding: 32px; background: #101014; }
                    h1 { color: #f5d90a; font-size: 1.4rem; }
                    .badge { display: inline-block; background: rgba(245,217,10,0.1); border: 1px solid #f5d90a; color: #f5d90a;
                              padding: 8px 12px; font-weight: bold; margin-bottom: 20px; }
                    ul { line-height: 1.7; }
                    p { line-height: 1.6; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <div class="badge">Has hecho clic en una simulacion de phishing de NETKNIFE</div>
                    <h1>Tranquilo, no ha pasado nada</h1>
                    <p>Este era un ejercicio educativo. No se ha recopilado ningun dato tuyo aparte de que has
                    hecho clic en este enlace y cuando. Ningun enlace ni archivo real de esta pagina puede
                    hacerte dano.</p>
                    <h2>Senales que deberias haber notado</h2>
                    <ul>%s</ul>
                    <h2>Que te llevas de esto</h2>
                    <p>%s</p>
                  </div>
                </body>
                </html>
                """.formatted(signalsHtml, lesson);
    }

    private String escape(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
