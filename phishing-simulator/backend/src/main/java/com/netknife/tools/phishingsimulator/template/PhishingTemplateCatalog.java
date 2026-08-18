package com.netknife.tools.phishingsimulator.template;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Catalogo fijo (no persistido) de las 3 plantillas disponibles. No hay
 * funcionalidad para crear plantillas nuevas ni para dirigirlas a terceros sin
 * consentimiento: es deliberadamente una lista cerrada y curada.
 */
@Component
public class PhishingTemplateCatalog {

    private static final String BANNER =
            "<div style=\"background:#fef3c7;color:#92400e;padding:12px;font-weight:bold;"
                    + "font-family:sans-serif;border:2px solid #f59e0b;margin-bottom:16px;\">"
                    + "⚠️ Esto es una SIMULACION EDUCATIVA enviada por NETKNIFE. Ningun servicio real te ha escrito. "
                    + "No se ha recopilado ningun dato tuyo: solo si haces clic o no en el enlace de abajo."
                    + "</div>";

    private final Map<String, PhishingTemplate> templates = new LinkedHashMap<>();

    public PhishingTemplateCatalog() {
        register(bankTemplate());
        register(prizeTemplate());
        register(accountUpdateTemplate());
    }

    public List<PhishingTemplate> listAll() {
        return List.copyOf(templates.values());
    }

    public Optional<PhishingTemplate> findById(String id) {
        return Optional.ofNullable(templates.get(id));
    }

    private void register(PhishingTemplate template) {
        templates.put(template.id(), template);
    }

    private PhishingTemplate bankTemplate() {
        return new PhishingTemplate(
                "aviso-banco-falso",
                "Aviso falso de banco",
                "Accion urgente requerida: verifica tu cuenta ahora",
                "Seguridad de tu Banco <seguridad@tu-banco-verificacion-segura.example>",
                BANNER + """
                        <div style="font-family:sans-serif;max-width:480px;">
                          <h2>Hemos detectado actividad inusual en tu cuenta</h2>
                          <p>Por tu seguridad, hemos bloqueado temporalmente el acceso a tu cuenta online. Debes
                          verificar tu identidad en las <strong>proximas 24 horas</strong> o tu cuenta sera
                          suspendida de forma permanente.</p>
                          <p><a href="{{TRACKING_URL}}" style="background:#dc2626;color:#fff;padding:10px 20px;
                          text-decoration:none;display:inline-block;">Verificar mi cuenta ahora</a></p>
                          <p style="font-size:12px;color:#666;">Este es un mensaje automatico, por favor no respondas.</p>
                        </div>
                        """,
                List.of(
                        "Urgencia artificial: 'en las proximas 24 horas', 'sera suspendida de forma permanente'",
                        "Remitente con un dominio que imita a un banco pero no es el oficial (fijate en el .example)",
                        "Pide 'verificar tu cuenta' haciendo clic en un enlace, algo que los bancos reales rara vez piden por email",
                        "Boton llamativo en rojo disenado para que hagas clic sin pensar"
                ),
                "Los bancos reales casi nunca piden 'verificar tu cuenta' con urgencia por email. Ante un mensaje "
                        + "asi, no hagas clic en ningun enlace: entra directamente en la web o app oficial de tu "
                        + "banco escribiendo tu la direccion, y llama a tu banco si tienes dudas."
        );
    }

    private PhishingTemplate prizeTemplate() {
        return new PhishingTemplate(
                "premio-falso",
                "Premio falso",
                "¡Has sido seleccionado/a! Reclama tu premio antes de que expire",
                "Sorteos y Premios <premios@sorteo-oficial-ganadores.example>",
                BANNER + """
                        <div style="font-family:sans-serif;max-width:480px;">
                          <h2>🎉 ¡Felicidades! Has ganado un premio</h2>
                          <p>Tu direccion de correo ha sido seleccionada al azar entre miles de participantes.
                          Tienes <strong>solo 48 horas</strong> para reclamar tu premio antes de que se le asigne
                          a otro ganador.</p>
                          <p><a href="{{TRACKING_URL}}" style="background:#16a34a;color:#fff;padding:10px 20px;
                          text-decoration:none;display:inline-block;">Reclamar mi premio</a></p>
                          <p style="font-size:12px;color:#666;">No compartas este enlace, es unico para ti.</p>
                        </div>
                        """,
                List.of(
                        "Algo demasiado bueno para ser verdad: un premio que no recuerdas haber participado en ganar",
                        "Urgencia artificial con una cuenta atras ('solo 48 horas')",
                        "Remitente generico sin relacion con ninguna empresa real que conozcas",
                        "Te anima a no compartir el enlace, para que no lo verifiques con nadie antes de hacer clic"
                ),
                "Si no recuerdas haber participado en un sorteo, no has podido ganarlo. Los premios legitimos no "
                        + "suelen anunciarse por email frio con plazos artificiales. Desconfia siempre de mensajes "
                        + "que combinan 'has ganado' con prisa por hacer clic."
        );
    }

    private PhishingTemplate accountUpdateTemplate() {
        return new PhishingTemplate(
                "actualizacion-cuenta-falsa",
                "Actualizacion de cuenta falsa",
                "Tu cuenta sera suspendida: actualiza tus datos",
                "Soporte de Cuenta <soporte@actualiza-tu-cuenta-ya.example>",
                BANNER + """
                        <div style="font-family:sans-serif;max-width:480px;">
                          <h2>Necesitamos que actualices tus datos</h2>
                          <p>Nuestros terminos de servicio han cambiado. Si no confirmas tus datos en los proximos
                          dias, tu cuenta quedara <strong>suspendida automaticamente</strong> y perderas el acceso
                          a tu contenido.</p>
                          <p><a href="{{TRACKING_URL}}" style="background:#2563eb;color:#fff;padding:10px 20px;
                          text-decoration:none;display:inline-block;">Actualizar mis datos</a></p>
                          <p style="font-size:12px;color:#666;">Equipo de soporte.</p>
                        </div>
                        """,
                List.of(
                        "Amenaza vaga de suspension sin especificar de que servicio se trata realmente",
                        "Remitente con un dominio que no coincide con ningun servicio real que uses",
                        "Pide 'actualizar tus datos' sin decir cuales, para que rellenes lo que sea en la pagina falsa",
                        "No se dirige a ti por tu nombre, un saludo generico tipico de envios masivos"
                ),
                "Antes de 'actualizar tus datos' en cualquier email, comprueba a que servicio dice pertenecer y "
                        + "entra directamente en tu cuenta desde la web oficial, nunca desde el enlace del correo."
        );
    }
}
