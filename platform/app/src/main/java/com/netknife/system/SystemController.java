package com.netknife.system;

import com.netknife.common.lifecycle.ShutdownHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Apagado ordenado del backend, invocado por Tauri antes de matar el proceso hijo.
 * Es el mecanismo PRINCIPAL de restauracion de conectividad al cerrar la app: en
 * Windows, matar el proceso ({@code TerminateProcess}, que es lo que hace
 * {@code Child::kill()} de Tauri) no dispara los shutdown hooks de la JVM, asi que no
 * basta con el {@code @PreDestroy} de cada {@link ShutdownHook} como unica red de
 * seguridad.
 *
 * Este modulo (el ensamblador de la aplicacion) recolecta todos los {@link ShutdownHook}
 * que publique cualquier herramienta via inyeccion de Spring, sin depender directamente
 * de ninguna herramienta concreta.
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    private static final Logger log = LoggerFactory.getLogger(SystemController.class);

    private final List<ShutdownHook> shutdownHooks;
    private final ApplicationContext applicationContext;

    public SystemController(List<ShutdownHook> shutdownHooks, ApplicationContext applicationContext) {
        this.shutdownHooks = shutdownHooks;
        this.applicationContext = applicationContext;
    }

    @PostMapping("/shutdown")
    public ResponseEntity<Void> shutdown() {
        log.info("Apagado ordenado solicitado: ejecutando {} shutdown hook(s) antes de cerrar", shutdownHooks.size());
        shutdownHooks.forEach(ShutdownHook::onShutdown);

        // La respuesta debe llegar a Tauri antes de que la JVM muera; el cierre real
        // se hace en un hilo aparte, con un pequeno margen para que la respuesta HTTP
        // termine de escribirse.
        Thread shutdownThread = new Thread(() -> {
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            SpringApplication.exit(applicationContext, () -> 0);
            System.exit(0);
        }, "netknife-shutdown");
        shutdownThread.setDaemon(true);
        shutdownThread.start();

        return ResponseEntity.accepted().build();
    }
}
