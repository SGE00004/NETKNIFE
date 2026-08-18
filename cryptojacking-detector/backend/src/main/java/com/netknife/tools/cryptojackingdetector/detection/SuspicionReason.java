package com.netknife.tools.cryptojackingdetector.detection;

public enum SuspicionReason {
    /** El nombre del ejecutable coincide con software de minado conocido: senal de alta confianza. */
    KNOWN_MINER_NAME,
    /** CPU alta y sostenida en un proceso sin ventana visible: el patron mas tipico de mineria oculta. */
    SUSTAINED_HIGH_CPU_NO_WINDOW,
    /** CPU alta y sostenida en un proceso CON ventana visible: puede ser legitimo (una app en uso activo). */
    SUSTAINED_HIGH_CPU
}
