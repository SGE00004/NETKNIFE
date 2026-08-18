package com.netknife.common.findings;

import java.util.List;

/**
 * Contrato comun para que un modulo del equipo rojo aporte hallazgos al Informe
 * Automatico de Hallazgos. Implementa esta interfaz y registrala como
 * @Component: el agregador (findings-report) la recoge sola (Spring inyecta
 * List&lt;FindingsSource&gt; con todas las implementaciones existentes en el
 * classpath), sin que este modulo ni el agregador se conozcan entre si. Mismo
 * patron de auto-registro que HygieneCheck en hygiene-checklist, generalizado
 * aqui a nivel de plataforma porque lo implementan varios modulos distintos.
 */
public interface FindingsSource {

    /** Identificador estable del modulo origen (ej. "port-radar"), usado como ToolId en el frontend. */
    String moduleId();

    /** Nombre en espanol del modulo origen, para mostrar en la interfaz. */
    String moduleLabel();

    /**
     * Hallazgos de la ultima ejecucion persistida de este modulo. Lista vacia si
     * el usuario todavia no ha ejecutado nunca esta herramienta (el agregador lo
     * interpreta como "modulo sin datos todavia", no como "todo en orden").
     */
    List<Finding> latestFindings();
}
