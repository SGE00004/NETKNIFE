package com.netknife.tools.portradar.check;

import com.netknife.common.dto.CheckStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PortScannerTest {

    private final PortScanner scanner = new PortScanner(300, 400, 4);

    @Test
    void detectsAnOpenPortAndReportsItsRisk() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int openPort = serverSocket.getLocalPort();
            PortDefinition def = new PortDefinition(openPort, "TEST", "riesgo de prueba", CheckStatus.ATENCION);

            Optional<PortScanOutcome> outcome = scanner.scanPort("127.0.0.1", def);

            assertThat(outcome).isPresent();
            assertThat(outcome.get().port()).isEqualTo(openPort);
            assertThat(outcome.get().status()).isEqualTo(CheckStatus.ATENCION);
        }
    }

    @Test
    void aClosedPortIsNotReported() throws IOException {
        int closedPort;
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            closedPort = serverSocket.getLocalPort();
        }
        // El socket ya se cerro: el puerto vuelve a estar libre/cerrado.
        PortDefinition def = new PortDefinition(closedPort, "TEST", "riesgo de prueba", CheckStatus.ATENCION);

        Optional<PortScanOutcome> outcome = scanner.scanPort("127.0.0.1", def);

        assertThat(outcome).isEmpty();
    }
}
