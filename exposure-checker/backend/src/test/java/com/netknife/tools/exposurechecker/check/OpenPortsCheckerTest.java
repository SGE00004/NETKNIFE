package com.netknife.tools.exposurechecker.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPortsCheckerTest {

    @Test
    void noPortsListeningIsOk() {
        CheckResult result = OpenPortsChecker.evaluate(port -> false);
        assertThat(result.status()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void telnetListeningIsDangerAndMentionsIt() {
        CheckResult result = OpenPortsChecker.evaluate(port -> port == 23);
        assertThat(result.status()).isEqualTo(CheckStatus.PELIGRO);
        assertThat(result.summary()).contains("Telnet");
        assertThat(result.summary()).doesNotContain("FTP", "RDP", "VNC");
    }

    @Test
    void multiplePortsListeningAreAllReported() {
        CheckResult result = OpenPortsChecker.evaluate(port -> port == 21 || port == 3389);
        assertThat(result.status()).isEqualTo(CheckStatus.PELIGRO);
        assertThat(result.summary()).contains("FTP").contains("RDP");
    }

    @Test
    void safePortIsNeverFlagged() {
        // 80/443 no estan en la lista de puertos peligrosos: un predicado que solo
        // responde true para ellos no debe disparar ninguna alerta.
        CheckResult result = OpenPortsChecker.evaluate(port -> port == 80 || port == 443);
        assertThat(result.status()).isEqualTo(CheckStatus.OK);
    }
}
