package com.netknife.common.exception;

/**
 * Fallo al enviar un correo (SMTP no configurado, credenciales invalidas,
 * servidor inalcanzable...). Usado por el Simulador de Phishing.
 */
public class MailDeliveryException extends RuntimeException {

    public MailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
