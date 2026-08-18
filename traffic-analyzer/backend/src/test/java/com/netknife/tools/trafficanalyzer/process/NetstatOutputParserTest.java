package com.netknife.tools.trafficanalyzer.process;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NetstatOutputParserTest {

    @Test
    void parsesATcpConnectionWithAResolvedProcessName() {
        String output = """
                Conexiones activas

                  Proto  Dir. local          Dir. remota          Estado          PID
                  TCP    192.168.1.10:54321  93.184.216.34:443    ESTABLECIDA     9999
                 [chrome.exe]
                """;

        List<NetstatConnection> connections = NetstatOutputParser.parse(output);

        assertThat(connections).hasSize(1);
        NetstatConnection connection = connections.get(0);
        assertThat(connection.protocol()).isEqualTo("TCP");
        assertThat(connection.localIp()).isEqualTo("192.168.1.10");
        assertThat(connection.localPort()).isEqualTo(54321);
        assertThat(connection.remoteIp()).isEqualTo("93.184.216.34");
        assertThat(connection.remotePort()).isEqualTo(443);
        assertThat(connection.pid()).isEqualTo(9999);
        assertThat(connection.processName()).isEqualTo("chrome.exe");
    }

    @Test
    void aConnectionWithoutAResolvedProcessNameHasNullProcessNameAndDoesNotConsumeTheNextLine() {
        // Sin privilegios de administrador, "-b" no imprime la linea "[proceso.exe]".
        String output = """
                  TCP    192.168.1.10:54321  93.184.216.34:443    ESTABLECIDA     9999
                  TCP    192.168.1.10:54322  1.2.3.4:80           ESTABLECIDA     8888
                 [firefox.exe]
                """;

        List<NetstatConnection> connections = NetstatOutputParser.parse(output);

        assertThat(connections).hasSize(2);
        assertThat(connections.get(0).processName()).isNull();
        assertThat(connections.get(1).processName()).isEqualTo("firefox.exe");
    }

    @Test
    void parsesAUdpConnectionWithAWildcardRemoteAddress() {
        String output = """
                  UDP    0.0.0.0:5353         *:*                                  4321
                 [chrome.exe]
                """;

        List<NetstatConnection> connections = NetstatOutputParser.parse(output);

        assertThat(connections).hasSize(1);
        NetstatConnection connection = connections.get(0);
        assertThat(connection.protocol()).isEqualTo("UDP");
        assertThat(connection.remoteIp()).isEqualTo("*");
        assertThat(connection.remotePort()).isNull();
        assertThat(connection.state()).isNull();
    }

    @Test
    void handlesBracketedIpv6Addresses() {
        String output = """
                  TCP    [::1]:5040           [::]:0               ESCUCHANDO      1234
                 [svchost.exe]
                """;

        List<NetstatConnection> connections = NetstatOutputParser.parse(output);

        assertThat(connections).hasSize(1);
        assertThat(connections.get(0).localIp()).isEqualTo("::1");
        assertThat(connections.get(0).localPort()).isEqualTo(5040);
    }

    @Test
    void ignoresTheHeaderAndBlankLines() {
        String output = """
                Conexiones activas

                  Proto  Dir. local          Dir. remota        Estado           PID
                """;

        assertThat(NetstatOutputParser.parse(output)).isEmpty();
    }
}
