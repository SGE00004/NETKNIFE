package com.netknife.tools.phishingsimulator.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * consentConfirmed debe llegar como true explicitamente: es la salvaguarda de
 * consentimiento validada en el backend, no solo en la UI. El limite de 5
 * destinatarios tambien se valida aqui, no solo en el formulario.
 */
public record SendSimulationRequest(
        @NotEmpty(message = "Indica al menos un destinatario")
        @Size(max = 5, message = "Como maximo 5 destinatarios por envio")
        List<@Email(message = "Hay una direccion de correo no valida entre los destinatarios") String> recipients,

        @NotBlank(message = "Indica una plantilla")
        String templateId,

        @AssertTrue(message = "Debes confirmar que tienes consentimiento para enviar esta simulacion")
        boolean consentConfirmed
) {
}
