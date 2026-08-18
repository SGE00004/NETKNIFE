package com.netknife.tools.wifirouteraudit.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import org.springframework.stereotype.Component;

/**
 * Comprueba si WPS (Wi-Fi Protected Setup) esta activado en el router. No
 * existe una forma fiable de consultarlo desde software sin privilegios
 * elevados o hardware de radio especializado en ningun sistema operativo
 * principal, asi que este resultado es siempre NO_VERIFICABLE con una guia de
 * como comprobarlo y desactivarlo a mano: WPS es un vector de ataque conocido
 * (el PIN por defecto de 8 digitos es fuerza-bruteable en horas), y la
 * recomendacion es desactivarlo salvo que se use activamente.
 */
@Component
public class WpsStatusCheck {

    public CheckResult check() {
        return new CheckResult(
                CheckStatus.NO_VERIFICABLE,
                "No se puede comprobar automaticamente si WPS esta activado en tu router.",
                null,
                "Entra en los ajustes de tu router y busca el apartado WPS (Wi-Fi Protected Setup). Si no lo "
                        + "usas para conectar dispositivos, desactivalo: su PIN por defecto se puede averiguar "
                        + "por fuerza bruta en pocas horas.");
    }
}
