package com.netknife.tools.wifirouteraudit.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultCredentialsCheckTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void blankRouterAddressIsNotVerifiable() {
        DefaultCredentialsCheck check = new DefaultCredentialsCheck(1000);

        CheckResult result = check.check(null);

        assertThat(result.status()).isEqualTo(CheckStatus.NO_VERIFICABLE);
    }

    @Test
    void basicAuthPanelAcceptingAKnownDefaultCredentialIsDanger() throws IOException {
        // El primer par de CommonRouterCredentials.ALL es admin/admin: el servidor de prueba solo acepta ese.
        String validAuthHeader = "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));
        server = startFakeRouterPanel(validAuthHeader);

        DefaultCredentialsCheck check = new DefaultCredentialsCheck(2000);
        CheckResult result = check.check("127.0.0.1:" + server.getAddress().getPort());

        assertThat(result.status()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void basicAuthPanelRejectingAllKnownCredentialsIsOk() throws IOException {
        // Ninguna cabecera enviada por el check coincidira nunca con esta.
        String onlyValidAuthHeader = "Basic " + Base64.getEncoder().encodeToString("nunca:coincide".getBytes(StandardCharsets.UTF_8));
        server = startFakeRouterPanel(onlyValidAuthHeader);

        DefaultCredentialsCheck check = new DefaultCredentialsCheck(2000);
        CheckResult result = check.check("127.0.0.1:" + server.getAddress().getPort());

        assertThat(result.status()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void unreachableRouterIsNotVerifiable() {
        DefaultCredentialsCheck check = new DefaultCredentialsCheck(500);

        // Puerto improbable de tener nada escuchando en localhost durante el test.
        CheckResult result = check.check("127.0.0.1:1");

        assertThat(result.status()).isEqualTo(CheckStatus.NO_VERIFICABLE);
    }

    private HttpServer startFakeRouterPanel(String validAuthHeader) throws IOException {
        HttpServer testServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        testServer.createContext("/", exchange -> {
            List<String> authHeaders = exchange.getRequestHeaders().get("Authorization");
            String authHeader = authHeaders == null || authHeaders.isEmpty() ? null : authHeaders.get(0);
            if (validAuthHeader.equals(authHeader)) {
                exchange.sendResponseHeaders(200, -1);
            } else {
                exchange.getResponseHeaders().add("WWW-Authenticate", "Basic realm=\"router\"");
                exchange.sendResponseHeaders(401, -1);
            }
            exchange.close();
        });
        testServer.start();
        return testServer;
    }
}
