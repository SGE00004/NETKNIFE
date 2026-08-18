package com.netknife.tools.wifirouteraudit.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

/**
 * Comprueba si el panel de administracion del router acepta alguna credencial
 * de fabrica comun. Solo puede verificarlo de forma fiable cuando el panel usa
 * autenticacion HTTP Basic (responde 401 con cabecera WWW-Authenticate: Basic):
 * la inmensa mayoria de routers domesticos modernos usan un formulario de login
 * en HTML en su lugar, que no se puede probar de forma generica sin arriesgarse
 * a falsos negativos/positivos, asi que en ese caso se declara NO_VERIFICABLE
 * con instrucciones para comprobarlo a mano.
 */
@Component
public class DefaultCredentialsCheck {

    private static final String MANUAL_GUIDE =
            "Entra en http://{ip}/ desde el navegador y prueba a iniciar sesion con admin/admin o busca en "
                    + "internet 'contrasena por defecto' seguido de la marca y modelo de tu router. Si sigue "
                    + "siendo la de fabrica, cambiala desde el propio panel.";

    private final HttpClient httpClient;
    private final int timeoutMs;

    public DefaultCredentialsCheck(@Value("${netknife.wifi-router-audit.http-timeout-ms:4000}") int timeoutMs) {
        this.timeoutMs = timeoutMs;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    public CheckResult check(String routerAddress) {
        if (routerAddress == null || routerAddress.isBlank()) {
            return new CheckResult(CheckStatus.NO_VERIFICABLE,
                    "No se ha detectado la IP de tu router automaticamente.",
                    null,
                    "Indica la IP de tu router manualmente (normalmente 192.168.1.1 o 192.168.0.1) y vuelve a "
                            + "intentarlo.");
        }

        Optional<HttpResponse<Void>> initialResponse = get(routerAddress, null);
        if (initialResponse.isEmpty()) {
            return new CheckResult(CheckStatus.NO_VERIFICABLE,
                    "No se ha podido contactar con el panel de tu router en " + routerAddress + ".",
                    null,
                    manualGuideFor(routerAddress));
        }

        HttpResponse<Void> response = initialResponse.get();
        if (response.statusCode() != 401 || !usesBasicAuth(response)) {
            return new CheckResult(CheckStatus.NO_VERIFICABLE,
                    "El panel de tu router en " + routerAddress + " usa un formulario de acceso propio, que no "
                            + "se puede comprobar automaticamente.",
                    null,
                    manualGuideFor(routerAddress));
        }

        for (CommonRouterCredentials.Credential credential : CommonRouterCredentials.ALL) {
            Optional<HttpResponse<Void>> attempt = get(routerAddress, credential);
            if (attempt.isPresent() && attempt.get().statusCode() / 100 == 2) {
                return new CheckResult(CheckStatus.PELIGRO,
                        "El panel de tu router en " + routerAddress + " acepta la credencial de fabrica '"
                                + credential.username() + "/" + credential.password() + "'.",
                        "Credencial de fabrica valida detectada.",
                        "Cambia la contrasena de acceso al panel del router ahora mismo desde sus propios "
                                + "ajustes de administracion.");
            }
        }

        return CheckResult.ok("El panel de tu router en " + routerAddress
                + " no acepta ninguna de las credenciales de fabrica mas comunes.");
    }

    private boolean usesBasicAuth(HttpResponse<Void> response) {
        return response.headers().firstValue("WWW-Authenticate")
                .map(value -> value.toLowerCase(Locale.ROOT).contains("basic"))
                .orElse(false);
    }

    private Optional<HttpResponse<Void>> get(String routerAddress, CommonRouterCredentials.Credential credential) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + routerAddress + "/"))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .GET();
            if (credential != null) {
                String token = Base64.getEncoder().encodeToString(
                        (credential.username() + ":" + credential.password()).getBytes());
                builder.header("Authorization", "Basic " + token);
            }
            return Optional.of(httpClient.send(builder.build(), HttpResponse.BodyHandlers.discarding()));
        } catch (IOException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private static String manualGuideFor(String routerAddress) {
        return MANUAL_GUIDE.replace("{ip}", routerAddress);
    }
}
