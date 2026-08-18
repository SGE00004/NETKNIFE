package com.netknife.common.lifecycle;

/**
 * Implementada por cualquier bean que necesite hacer limpieza sincrona antes de que
 * la aplicacion se apague (invocada desde el endpoint de apagado ordenado que llama
 * Tauri antes de matar el proceso). Vive en {@code common} para que el modulo
 * ensamblador (que expone ese endpoint) pueda recolectar todos los hooks de todas
 * las herramientas via inyeccion de Spring ({@code List<ShutdownHook>}) sin depender
 * directamente de ninguna herramienta concreta.
 */
public interface ShutdownHook {

    void onShutdown();
}
