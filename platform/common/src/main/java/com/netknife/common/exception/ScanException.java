package com.netknife.common.exception;

/**
 * Error durante la ejecucion de un escaneo (fallo al detectar la red local,
 * al lanzar el proceso de ping, etc).
 */
public class ScanException extends RuntimeException {

    public ScanException(String message) {
        super(message);
    }

    public ScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
