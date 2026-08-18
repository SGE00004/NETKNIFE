package com.netknife.tools.hygienechecklist.check;

import com.netknife.common.dto.CheckResult;

/**
 * Contrato comun para cada comprobacion de higiene digital. Permite anadir
 * nuevas comprobaciones sin tocar el resto del modulo: basta con implementar
 * esta interfaz y registrarla como @Component para que HygieneChecklistService
 * la recoja automaticamente (Spring inyecta List&lt;HygieneCheck&gt; con todas
 * las implementaciones existentes).
 */
public interface HygieneCheck {

    /** Clave estable del item (ej. "firewall"), usada tambien para el estado manual persistido. */
    String id();

    /** Titulo corto en espanol para mostrar en la interfaz. */
    String title();

    /** Explicacion de una frase de por que importa este item. */
    String whyItMatters();

    /**
     * true si evaluate() intenta una comprobacion automatica del sistema; false
     * si es una pregunta que solo el usuario puede responder (ej. "¿tienes copia
     * de seguridad reciente?"). Los items manuales tambien implementan evaluate()
     * pero siempre devuelven NO_VERIFICABLE invitando a responder a mano: la
     * respuesta real se guarda aparte via el PATCH del checklist.
     */
    boolean isAutomatic();

    CheckResult evaluate();
}
