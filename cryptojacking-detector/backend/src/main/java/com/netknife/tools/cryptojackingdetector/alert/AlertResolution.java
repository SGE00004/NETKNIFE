package com.netknife.tools.cryptojackingdetector.alert;

public enum AlertResolution {
    /** El usuario pulso "Finalizar proceso" desde NETKNIFE. */
    PROCESS_ENDED_BY_USER,
    /** El proceso ya no aparece en la lista de procesos del sistema (termino por su cuenta). */
    PROCESS_EXITED_ON_ITS_OWN,
    /** El proceso sigue en ejecucion pero su CPU volvio a niveles normales. */
    CPU_DROPPED
}
