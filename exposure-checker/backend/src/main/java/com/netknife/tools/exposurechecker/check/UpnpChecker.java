package com.netknife.tools.exposurechecker.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Comprueba si hay un servicio UPnP (Universal Plug and Play) respondiendo en la
 * red local, mediante un unico paquete SSDP M-SEARCH multicast estandar (el mismo
 * mecanismo de descubrimiento que usan consolas y apps legitimas, no una tecnica
 * de intrusion). UPnP en el router puede abrir puertos automaticamente sin que el
 * usuario lo sepa, por eso se marca como "atencion" y no como "peligro": no es
 * necesariamente inseguro, pero conviene revisarlo.
 */
@Component
public class UpnpChecker {

    private static final Logger log = LoggerFactory.getLogger(UpnpChecker.class);
    private static final String SSDP_MULTICAST_ADDRESS = "239.255.255.250";
    private static final int SSDP_PORT = 1900;
    private static final String M_SEARCH_REQUEST =
            "M-SEARCH * HTTP/1.1\r\n"
                    + "HOST: 239.255.255.250:1900\r\n"
                    + "MAN: \"ssdp:discover\"\r\n"
                    + "MX: 2\r\n"
                    + "ST: ssdp:all\r\n\r\n";

    private final int timeoutMs;

    public UpnpChecker(@Value("${netknife.exposure.upnp-timeout-ms:2500}") int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public CheckResult check() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(timeoutMs);
            byte[] requestBytes = M_SEARCH_REQUEST.getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(
                    requestBytes, requestBytes.length, InetAddress.getByName(SSDP_MULTICAST_ADDRESS), SSDP_PORT));

            byte[] buffer = new byte[2048];
            socket.receive(new DatagramPacket(buffer, buffer.length));
            return classify(true, false);
        } catch (SocketTimeoutException e) {
            return classify(false, false);
        } catch (IOException e) {
            log.debug("No se pudo comprobar UPnP: {}", e.getMessage());
            return classify(false, true);
        }
    }

    /**
     * Logica de clasificacion pura, testeable sin abrir sockets reales.
     */
    static CheckResult classify(boolean upnpResponded, boolean ioError) {
        if (ioError) {
            return new CheckResult(CheckStatus.NO_VERIFICABLE,
                    "No se ha podido comprobar si UPnP esta activo en este sistema.",
                    null,
                    "Revisa manualmente en los ajustes de tu router si UPnP esta activado, y "
                            + "desactivalo si no lo necesitas.");
        }
        if (upnpResponded) {
            return new CheckResult(CheckStatus.ATENCION,
                    "Se ha detectado UPnP activo en tu red: tu router puede abrir puertos "
                            + "automaticamente para programas y dispositivos sin que lo sepas.",
                    null,
                    "Si no lo necesitas (la mayoria de usuarios domesticos no lo necesitan), "
                            + "desactiva UPnP en los ajustes de tu router y revisa manualmente las "
                            + "reglas de reenvio de puertos que tenga activas.");
        }
        return CheckResult.ok("No se ha detectado UPnP respondiendo en tu red.");
    }
}
